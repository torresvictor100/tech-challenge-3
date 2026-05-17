package appointment_service.application;

import appointment_service.domain.Appointment;
import appointment_service.domain.AppointmentStatus;
import java.time.Instant;
import java.util.UUID;

public record AppointmentView(
		UUID id,
		UUID patientId,
		UUID professionalId,
		String professionalName,
		Instant scheduledAt,
		AppointmentStatus status,
		String notes,
		Instant createdAt,
		Instant updatedAt) {

	public static AppointmentView from(Appointment appointment) {
		return new AppointmentView(
				appointment.getId(),
				appointment.getPatientId(),
				appointment.getProfessionalId(),
				appointment.getProfessionalName(),
				appointment.getScheduledAt(),
				appointment.getStatus(),
				appointment.getNotes(),
				appointment.getCreatedAt(),
				appointment.getUpdatedAt());
	}
}
