package notification_service.api.rabbitmq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;
import notification_service.application.ReminderRecipientResolver;
import notification_service.application.ReminderService;
import notification_service.application.event.AppointmentEvent;
import org.junit.jupiter.api.Test;

class AppointmentNotificationRabbitMqAdapterTest {

	@Test
	void consumesAppointmentEventAndProcessesReminder() {
		RecordingReminderService reminderService = new RecordingReminderService();
		AppointmentNotificationRabbitMqAdapter adapter = new AppointmentNotificationRabbitMqAdapter(reminderService);

		adapter.consume("""
				{
				  "eventType": "APPOINTMENT_CREATED",
				  "eventId": "00000000-0000-0000-0000-000000000901",
				  "appointmentId": "00000000-0000-0000-0000-000000000902",
				  "patientId": "00000000-0000-0000-0000-000000000601",
				  "scheduledAt": "2030-01-10T10:00:00Z",
				  "status": "SCHEDULED"
				}
				""");

		assertEquals("APPOINTMENT_CREATED", reminderService.event.eventType());
		assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000902"), reminderService.event.appointmentId());
		assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000601"), reminderService.event.patientId());
		assertEquals(Instant.parse("2030-01-10T10:00:00Z"), reminderService.event.scheduledAt());
		assertEquals("SCHEDULED", reminderService.event.status());
	}

	@Test
	void rejectsInvalidAppointmentEventPayload() {
		AppointmentNotificationRabbitMqAdapter adapter = new AppointmentNotificationRabbitMqAdapter(
				new RecordingReminderService());

		assertThrows(IllegalArgumentException.class, () -> adapter.consume("{invalid-json"));
	}

	private static final class RecordingReminderService extends ReminderService {

		private AppointmentEvent event;

		private RecordingReminderService() {
			super(
					command -> null,
					notificationId -> null,
					new ReminderRecipientResolver("paciente@example.com"));
		}

		@Override
		public void process(AppointmentEvent event) {
			this.event = event;
		}
	}
}
