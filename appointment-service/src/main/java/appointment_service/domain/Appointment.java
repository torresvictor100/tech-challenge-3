package appointment_service.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Appointment {

	private final UUID id;
	private final UUID patientId;
	private final UUID professionalId;
	private final String professionalName;
	private final String notes;
	private final Instant createdAt;
	private Instant scheduledAt;
	private AppointmentStatus status;
	private Instant updatedAt;

	private Appointment(
			UUID id,
			UUID patientId,
			UUID professionalId,
			String professionalName,
			Instant scheduledAt,
			AppointmentStatus status,
			String notes,
			Instant createdAt,
			Instant updatedAt) {
		this.id = require(id, "Appointment id is required");
		this.patientId = require(patientId, "Patient id is required");
		this.professionalId = require(professionalId, "Professional id is required");
		this.professionalName = requireText(professionalName, "Professional name is required");
		this.scheduledAt = require(scheduledAt, "Appointment date is required");
		this.status = require(status, "Appointment status is required");
		this.notes = normalize(notes);
		this.createdAt = require(createdAt, "Creation date is required");
		this.updatedAt = require(updatedAt, "Update date is required");
	}

	public static Appointment schedule(
			UUID patientId,
			UUID professionalId,
			String professionalName,
			Instant scheduledAt,
			String notes) {
		return schedule(patientId, professionalId, professionalName, scheduledAt, AppointmentStatus.SCHEDULED, notes);
	}

	public static Appointment schedule(
			UUID patientId,
			UUID professionalId,
			String professionalName,
			Instant scheduledAt,
			AppointmentStatus status,
			String notes) {
		validateFutureDate(scheduledAt);
		Instant now = Instant.now();
		return new Appointment(
				UUID.randomUUID(),
				patientId,
				professionalId,
				professionalName,
				scheduledAt,
				status == null ? AppointmentStatus.SCHEDULED : status,
				notes,
				now,
				now);
	}

	public static Appointment restore(
			UUID id,
			UUID patientId,
			UUID professionalId,
			String professionalName,
			Instant scheduledAt,
			AppointmentStatus status,
			String notes,
			Instant createdAt,
			Instant updatedAt) {
		return new Appointment(
				id,
				patientId,
				professionalId,
				professionalName,
				scheduledAt,
				status,
				notes,
				createdAt,
				updatedAt);
	}

	public Appointment update(
			UUID professionalId,
			String professionalName,
			Instant scheduledAt,
			AppointmentStatus status,
			String notes) {
		Instant updatedSchedule = scheduledAt == null ? this.scheduledAt : scheduledAt;
		validateFutureDate(updatedSchedule);
		return new Appointment(
				id,
				patientId,
				professionalId == null ? this.professionalId : professionalId,
				professionalName == null || professionalName.isBlank() ? this.professionalName : professionalName,
				updatedSchedule,
				status == null ? this.status : status,
				notes,
				createdAt,
				Instant.now());
	}

	public void confirm() {
		if (status != AppointmentStatus.SCHEDULED) {
			throw new AppointmentDomainException("Only scheduled appointments can be confirmed");
		}

		status = AppointmentStatus.CONFIRMED;
		touch();
	}

	public void cancel() {
		if (status == AppointmentStatus.CANCELED) {
			throw new AppointmentDomainException("Appointment is already canceled");
		}

		status = AppointmentStatus.CANCELED;
		touch();
	}

	public UUID getId() {
		return id;
	}

	public UUID getPatientId() {
		return patientId;
	}

	public UUID getProfessionalId() {
		return professionalId;
	}

	public String getProfessionalName() {
		return professionalName;
	}

	public Instant getScheduledAt() {
		return scheduledAt;
	}

	public AppointmentStatus getStatus() {
		return status;
	}

	public String getNotes() {
		return notes;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	private void touch() {
		updatedAt = Instant.now();
	}

	private static void validateFutureDate(Instant scheduledAt) {
		if (scheduledAt == null || scheduledAt.isBefore(Instant.now())) {
			throw new AppointmentDomainException("Appointment date must be in the future");
		}
	}

	private static <T> T require(T value, String message) {
		return Objects.requireNonNull(value, message);
	}

	private static String requireText(String value, String message) {
		String normalized = normalize(value);
		if (normalized == null) {
			throw new AppointmentDomainException(message);
		}

		return normalized;
	}

	private static String normalize(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}
}
