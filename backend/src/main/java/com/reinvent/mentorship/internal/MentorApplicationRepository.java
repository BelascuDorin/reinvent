package com.reinvent.mentorship.internal;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface MentorApplicationRepository extends JpaRepository<MentorApplication, UUID> {

	/**
	 * A User's whole application history. Callers pick the one that counts with
	 * {@link MentorApplication#current(Collection)} rather than ordering it themselves.
	 */
	List<MentorApplication> findByApplicantUserId(UUID applicantUserId);

	/** How many times this User has applied — the next application is the one after. */
	int countByApplicantUserId(UUID applicantUserId);

	boolean existsByApplicantUserIdAndStatusIn(UUID applicantUserId, Collection<ApplicationStatus> statuses);

	List<MentorApplication> findByStatusInOrderByCreatedAtAsc(Collection<ApplicationStatus> statuses);

	/**
	 * The histories of several Users at once, so a caller asking about a batch doesn't
	 * pay a query per User. Grouped and reduced by the caller, same rule as above.
	 */
	List<MentorApplication> findByApplicantUserIdIn(Collection<UUID> applicantUserIds);
}
