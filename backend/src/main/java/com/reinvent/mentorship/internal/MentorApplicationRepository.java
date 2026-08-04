package com.reinvent.mentorship.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface MentorApplicationRepository extends JpaRepository<MentorApplication, UUID> {

	/** A User may reapply after a rejection, so keep history and read the latest. */
	Optional<MentorApplication> findFirstByApplicantUserIdOrderByCreatedAtDesc(UUID applicantUserId);

	boolean existsByApplicantUserIdAndStatusIn(UUID applicantUserId, Collection<ApplicationStatus> statuses);

	List<MentorApplication> findByStatusInOrderByCreatedAtAsc(Collection<ApplicationStatus> statuses);

	/**
	 * The applications of several Users at once, newest first — so a caller asking about
	 * a batch of Users can pick each one's latest without a query per User. Same
	 * "read the latest, keep the history" rule as the single-User lookup above.
	 */
	List<MentorApplication> findByApplicantUserIdInOrderByCreatedAtDesc(Collection<UUID> applicantUserIds);
}
