package com.reinvent.consent.internal;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface GuardianConsentRepository extends JpaRepository<GuardianConsent, UUID> {
}
