package appointment_service.application.event;

import appointment_service.domain.Appointment;
import java.time.Instant;
import java.util.UUID;

public record AppointmentEvent(
		String eventType,
		String eventId,
		UUID appointmentId,
		UUID patientId,
		Instant scheduledAt,
		String status) {

	public static AppointmentEvent created(Appointment appointment) {
		return from("APPOINTMENT_CREATED", appointment);
	}

	public static AppointmentEvent updated(Appointment appointment) {
		return from("APPOINTMENT_UPDATED", appointment);
	}

	private static AppointmentEvent from(String eventType, Appointment appointment) {
		return new AppointmentEvent(
				eventType,
				UUID.randomUUID().toString(),
				appointment.getId(),
				appointment.getPatientId(),
				appointment.getScheduledAt(),
				appointment.getStatus().name());
	}
}
