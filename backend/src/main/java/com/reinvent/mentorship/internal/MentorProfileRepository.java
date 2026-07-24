package com.reinvent.mentorship.internal;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface MentorProfileRepository extends JpaRepository<MentorProfile, UUID> {
}
