package appointment_service.api.dto;

import appointment_service.application.AppointmentView;

public record AppointmentResponse(
		String appointmentId,
		String patientId,
		String professionalId,
		String professionalName,
		String scheduledAt,
		String status,
		String notes,
		String createdAt,
		String updatedAt) {

	public static AppointmentResponse from(AppointmentView appointment) {
		return new AppointmentResponse(
				appointment.id().toString(),
				appointment.patientId().toString(),
				appointment.professionalId().toString(),
				appointment.professionalName(),
				appointment.scheduledAt().toString(),
				appointment.status().name(),
				appointment.notes(),
				appointment.createdAt().toString(),
				appointment.updatedAt().toString());
	}
}
