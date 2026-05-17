package appointment_service.domain;

import java.util.Objects;
import java.util.UUID;

public record Patient(
		UUID id,
		UUID userId,
		String fullName) {

	public Patient {
		Objects.requireNonNull(id, "Patient id is required");
		Objects.requireNonNull(userId, "User id is required");
		if (fullName == null || fullName.isBlank()) {
			throw new AppointmentDomainException("Patient full name is required");
		}

		fullName = fullName.trim();
	}
}
