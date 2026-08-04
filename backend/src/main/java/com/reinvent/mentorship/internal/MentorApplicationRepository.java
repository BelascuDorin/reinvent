package com.reinvent.mentorship.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface MentorApplicationRepository extends JpaRepository<MentorApplication, UUID> {

	/**
	 * The one application that says where a User stands, or empty if they never applied.
	 * The rule for picking it lives on {@link MentorApplication#current(Collection)}; this
	 * pairs it with the query so no caller has to remember to do both.
	 */
	default Optional<MentorApplication> findCurrentByApplicantUserId(UUID applicantUserId) {
		return MentorApplication.current(findByApplicantUserId(applicantUserId));
	}

	/** A User's whole application history, rejections included. */
	List<MentorApplication> findByApplicantUserId(UUID applicantUserId);

	/** How many times this User has applied — the next application is the one after. */
	int countByApplicantUserId(UUID applicantUserId);

	boolean existsByApplicantUserIdAndStatusIn(UUID applicantUserId, Collection<ApplicationStatus> statuses);

	List<MentorApplication> findByStatusInOrderByCreatedAtAscIdAsc(Collection<ApplicationStatus> statuses);

	/**
	 * The histories of several Users at once, so a caller asking about a batch doesn't
	 * pay a query per User. Grouped and reduced by the caller, same rule as above.
	 */
	List<MentorApplication> findByApplicantUserIdIn(Collection<UUID> applicantUserIds);
}
