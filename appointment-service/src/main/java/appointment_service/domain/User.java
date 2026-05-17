package appointment_service.domain;

import java.util.Objects;
import java.util.UUID;

public record User(
		UUID id,
		String email,
		String fullName,
		String passwordHash,
		UserRole role) {

	public User {
		Objects.requireNonNull(id, "User id is required");
		email = requireText(email, "Email is required").toLowerCase();
		fullName = requireText(fullName, "Full name is required");
		passwordHash = requireText(passwordHash, "Password hash is required");
		Objects.requireNonNull(role, "Role is required");
	}

	private static String requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new AppointmentDomainException(message);
		}

		return value.trim();
	}
}
