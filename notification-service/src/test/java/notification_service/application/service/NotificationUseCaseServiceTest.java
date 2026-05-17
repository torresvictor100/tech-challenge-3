package notification_service.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import notification_service.application.NotificationView;
import notification_service.application.port.in.CreateNotificationUseCase.CreateNotificationCommand;
import notification_service.application.port.out.NotificationSenderPort;
import notification_service.application.port.out.NotificationSenderPort.DeliveryResult;
import notification_service.domain.Notification;
import notification_service.domain.NotificationChannel;
import notification_service.domain.NotificationStatus;
import notification_service.infrastructure.memory.InMemoryNotificationRepositoryAdapter;
import org.junit.jupiter.api.Test;

class NotificationUseCaseServiceTest {

	private final InMemoryNotificationRepositoryAdapter repository = new InMemoryNotificationRepositoryAdapter();

	@Test
	void marksNotificationAsSentWhenEmailIsDelivered() {
		NotificationUseCaseService service = new NotificationUseCaseService(
				repository,
				notification -> DeliveryResult.sent());
		NotificationView created = service.create(command());

		NotificationView sent = service.send(created.id());

		assertEquals(NotificationStatus.SENT, sent.status());
		assertNotNull(sent.sentAt());
		assertNull(sent.failureReason());
	}

	@Test
	void marksNotificationAsFailedWhenEmailIsNotConfigured() {
		NotificationUseCaseService service = new NotificationUseCaseService(
				repository,
				new NotConfiguredSender());
		NotificationView created = service.create(command());

		NotificationView failed = service.send(created.id());

		assertEquals(NotificationStatus.FAILED, failed.status());
		assertEquals("Gmail SMTP is not configured", failed.failureReason());
	}

	private static CreateNotificationCommand command() {
		return new CreateNotificationCommand(
				UUID.fromString("00000000-0000-0000-0000-000000000901"),
				"paciente@example.com",
				"Consulta agendada",
				"Sua consulta foi registrada.",
				NotificationChannel.EMAIL);
	}

	private static final class NotConfiguredSender implements NotificationSenderPort {

		@Override
		public DeliveryResult send(Notification notification) {
			return DeliveryResult.notSent("Gmail SMTP is not configured");
		}
	}
}
