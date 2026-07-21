package com.reinvent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Reinvent modular monolith.
 *
 * <p>Application modules live in the direct sub-packages of this class
 * ({@code identity}, {@code mentorship}, {@code consent}) with {@code platform}
 * as the shared kernel. Boundaries are enforced by Spring Modulith; see
 * {@code ModularityTests}.
 */
@SpringBootApplication
public class ReinventApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReinventApplication.class, args);
	}
}
