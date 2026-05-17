package appointment_service.infrastructure.cassandra;

import appointment_service.domain.Appointment;
import appointment_service.domain.AppointmentStatus;
import java.time.Instant;
import java.util.UUID;

class PatientAppointmentRow {

	private final UUID patientId;
	private final Instant scheduledAt;
	private final UUID appointmentId;
	private final UUID professionalId;
	private final String professionalName;
	private final String status;
	private final String notes;
	private final Instant createdAt;
	private final Instant updatedAt;

	PatientAppointmentRow(
			UUID patientId,
			Instant scheduledAt,
			UUID appointmentId,
			UUID professionalId,
			String professionalName,
			String status,
			String notes,
			Instant createdAt,
			Instant updatedAt) {
		this.patientId = patientId;
		this.scheduledAt = scheduledAt;
		this.appointmentId = appointmentId;
		this.professionalId = professionalId;
		this.professionalName = professionalName;
		this.status = status;
		this.notes = notes;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	static PatientAppointmentRow from(Appointment appointment) {
		return new PatientAppointmentRow(
				appointment.getPatientId(),
				appointment.getScheduledAt(),
				appointment.getId(),
				appointment.getProfessionalId(),
				appointment.getProfessionalName(),
				appointment.getStatus().name(),
				appointment.getNotes(),
				appointment.getCreatedAt(),
				appointment.getUpdatedAt());
	}

	Appointment toAppointment() {
		return Appointment.restore(
				appointmentId,
				patientId,
				professionalId,
				professionalName,
				scheduledAt,
				AppointmentStatus.valueOf(status),
				notes,
				createdAt,
				updatedAt);
	}

	UUID patientId() {
		return patientId;
	}

	Instant scheduledAt() {
		return scheduledAt;
	}

	UUID appointmentId() {
		return appointmentId;
	}

	UUID professionalId() {
		return professionalId;
	}

	String professionalName() {
		return professionalName;
	}

	String status() {
		return status;
	}

	String notes() {
		return notes;
	}

	Instant createdAt() {
		return createdAt;
	}

	Instant updatedAt() {
		return updatedAt;
	}
}
