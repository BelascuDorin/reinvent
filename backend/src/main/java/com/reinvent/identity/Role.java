package com.reinvent.identity;

/**
 * A role a {@code User} can hold. Everyone is a {@link #MENTEE} by default;
 * {@link #MENTOR} is granted only on Reviewer approval and {@link #REVIEWER} is
 * an internal, trusted role. A User may hold both MENTEE and MENTOR.
 */
public enum Role {
	MENTEE,
	MENTOR,
	REVIEWER
}
