package com.reinvent.mentorship.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;

import com.reinvent.TestcontainersConfiguration;
import com.reinvent.identity.CurrentUser;
import com.reinvent.identity.Role;
import com.reinvent.platform.Clock;
import com.reinvent.platform.MentorApproved;
import com.reinvent.platform.PaymentGateway;
import com.reinvent.platform.testing.FakeClock;
import com.reinvent.platform.testing.InMemoryPaymentGateway;
import com.reinvent.testsupport.FixedClockConfiguration;

/**
 * Exercises the {@code mentorship} module through its service seam (Spring Modulith
 * bootstraps only this module) against a real Postgres, with the {@link PaymentGateway}
 * and {@link Clock} ports faked. Reads as the lifecycle stories: a User applies, a
 * Reviewer works the application, only legal transitions are allowed, approval is
 * announced, and the bookability predicate holds. Authorization is a controller
 * concern tested at the HTTP boundary, so {@link CurrentUser} is only stubbed here to
 * satisfy the controller beans in the module context.
 */
@ApplicationModuleTest
@Import(TestcontainersConfiguration.class)
class MentorshipModuleTest {

	@TestConfiguration(proxyBeanMethods = false)
	static class Fakes {

		@Bean
		FakeClock clock() {
			return new FakeClock(FixedClockConfiguration.FIXED);
		}

		@Bean
		InMemoryPaymentGateway paymentGateway() {
			return new InMemoryPaymentGateway();
		}

		@Bean
		CurrentUser currentUser() {
			return new CurrentUser() {
				@Override
				public UUID requireUserId() {
					throw new UnsupportedOperationException("not used in the service seam");
				}

				@Override
				public void requireRole(Role role) {
					throw new UnsupportedOperationException("not used in the service seam");
				}
			};
		}
	}

	@Autowired
	MentorshipService mentorship;

	@Autowired
	InMemoryPaymentGateway payments;

	@Test
	void applyingStartsInApplied() {
		UUID applicant = UUID.randomUUID();

		MentorApplicationView view = mentorship.apply(applicant);

		assertThat(view.status()).isEqualTo(ApplicationStatus.APPLIED);
		assertThat(view.bookable()).isFalse();
		assertThat(mentorship.myApplication(applicant).id()).isEqualTo(view.id());
	}

	@Test
	void aUserCannotHaveTwoOpenApplications() {
		UUID applicant = UUID.randomUUID();
		mentorship.apply(applicant);

		assertThatExceptionOfType(DuplicateApplicationException.class)
				.isThrownBy(() -> mentorship.apply(applicant));
	}

	@Test
	void approvingAnApplicationAnnouncesItSoTheRoleCanBeGranted(Scenario scenario) {
		UUID applicant = UUID.randomUUID();
		UUID applicationId = mentorship.apply(applicant).id();
		mentorship.startReview(applicationId);

		scenario.stimulate(() -> mentorship.approve(applicationId))
				.andWaitForEventOfType(MentorApproved.class)
				.matchingMappedValue(MentorApproved::userId, applicant)
				.toArriveAndVerify(event -> assertThat(event.userId()).isEqualTo(applicant));

		assertThat(mentorship.myApplication(applicant).status()).isEqualTo(ApplicationStatus.APPROVED);
	}

	@Test
	void rejectingRecordsTheReason() {
		UUID applicant = UUID.randomUUID();
		UUID applicationId = mentorship.apply(applicant).id();
		mentorship.startReview(applicationId);

		MentorApplicationView rejected = mentorship.reject(applicationId, "Insufficient experience.");

		assertThat(rejected.status()).isEqualTo(ApplicationStatus.REJECTED);
		assertThat(rejected.rejectionReason()).isEqualTo("Insufficient experience.");
	}

	@Test
	void onlyLegalTransitionsArePermitted() {
		UUID applicant = UUID.randomUUID();
		UUID applicationId = mentorship.apply(applicant).id();

		// Cannot approve or reject an application that is still merely APPLIED.
		assertThatExceptionOfType(IllegalApplicationTransitionException.class)
				.isThrownBy(() -> mentorship.approve(applicationId));
		assertThatExceptionOfType(IllegalApplicationTransitionException.class)
				.isThrownBy(() -> mentorship.reject(applicationId, "nope"));

		mentorship.startReview(applicationId);
		mentorship.reject(applicationId, "Rejected.");

		// Cannot approve an application already rejected.
		assertThatExceptionOfType(IllegalApplicationTransitionException.class)
				.isThrownBy(() -> mentorship.approve(applicationId));
	}

	@Test
	void aRejectedApplicantMayApplyAgain() {
		UUID applicant = UUID.randomUUID();
		UUID first = mentorship.apply(applicant).id();
		mentorship.startReview(first);
		mentorship.reject(first, "Try later.");

		MentorApplicationView second = mentorship.apply(applicant);

		assertThat(second.status()).isEqualTo(ApplicationStatus.APPLIED);
		assertThat(second.id()).isNotEqualTo(first);
	}

	@Test
	void onlyAnApprovedMentorCanBeSuspended() {
		UUID applicant = UUID.randomUUID();
		UUID applicationId = mentorship.apply(applicant).id();

		assertThatExceptionOfType(IllegalApplicationTransitionException.class)
				.isThrownBy(() -> mentorship.suspend(applicationId));

		mentorship.startReview(applicationId);
		mentorship.approve(applicationId);
		MentorApplicationView suspended = mentorship.suspend(applicationId);
		assertThat(suspended.suspended()).isTrue();

		// A Mentor cannot be suspended twice.
		assertThatExceptionOfType(IllegalApplicationTransitionException.class)
				.isThrownBy(() -> mentorship.suspend(applicationId));
	}

	@Test
	void bookableOnlyWhenApprovedNotSuspendedAndOnboarded() {
		UUID applicant = UUID.randomUUID();
		UUID applicationId = mentorship.apply(applicant).id();
		mentorship.startReview(applicationId);
		mentorship.approve(applicationId);

		// Approved but not yet payment-onboarded → not bookable (ADR-0003).
		assertThat(mentorship.isBookable(applicant)).isFalse();

		payments.markOnboarded(applicant);
		assertThat(mentorship.isBookable(applicant)).isTrue();

		// Suspension removes bookability even when onboarded.
		mentorship.suspend(applicationId);
		assertThat(mentorship.isBookable(applicant)).isFalse();
	}
}
