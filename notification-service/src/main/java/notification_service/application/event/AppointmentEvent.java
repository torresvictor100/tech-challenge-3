package notification_service.application.event;

import java.time.Instant;
import java.util.UUID;

public record AppointmentEvent(
		String eventType,
		String eventId,
		UUID appointmentId,
		UUID patientId,
		Instant scheduledAt,
		String status) {
}
