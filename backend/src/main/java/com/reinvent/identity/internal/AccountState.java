package com.reinvent.identity.internal;

import java.util.List;

import com.reinvent.identity.GuardianConsentStatus;
import com.reinvent.identity.Role;

/**
 * A User's own view of their account: the roles they hold and, for a minor,
 * where they stand on Guardian consent.
 */
record AccountState(String email, List<Role> roles, boolean minor, GuardianConsentStatus guardianConsentStatus) {
}
