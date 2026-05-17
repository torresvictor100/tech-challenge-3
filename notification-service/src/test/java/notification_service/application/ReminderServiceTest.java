package notification_service.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.UUID;
import notification_service.application.event.AppointmentEvent;
import notification_service.application.service.NotificationUseCaseService;
import notification_service.domain.Notification;
import notification_service.domain.NotificationStatus;
import notification_service.infrastructure.memory.InMemoryNotificationRepositoryAdapter;
import org.junit.jupiter.api.Test;

class ReminderServiceTest {

	@Test
	void createsAndSendsEmailNotificationFromAppointmentEvent() {
		InMemoryNotificationRepositoryAdapter repository = new InMemoryNotificationRepositoryAdapter();
		NotificationUseCaseService notificationUseCaseService = new NotificationUseCaseService(
				repository,
				notification -> notification.getRecipient().equals("paciente@hospital.com")
						? notification_service.application.port.out.NotificationSenderPort.DeliveryResult.sent()
						: notification_service.application.port.out.NotificationSenderPort.DeliveryResult.notSent("unexpected recipient"));
		ReminderService reminderService = new ReminderService(
				notificationUseCaseService,
				notificationUseCaseService,
				new ReminderRecipientResolver("fallback@example.com"));

		reminderService.process(new AppointmentEvent(
				"APPOINTMENT_CREATED",
				"00000000-0000-0000-0000-000000000901",
				UUID.fromString("00000000-0000-0000-0000-000000000902"),
				UUID.fromString("00000000-0000-0000-0000-000000000601"),
				Instant.parse("2030-01-10T10:00:00Z"),
				"SCHEDULED"));

		Notification notification = repository.findAll().getFirst();
		assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000902"), notification.getAppointmentId());
		assertEquals("paciente@hospital.com", notification.getRecipient());
		assertEquals(NotificationStatus.SENT, notification.getStatus());
		assertNotNull(notification.getSentAt());
	}
}
