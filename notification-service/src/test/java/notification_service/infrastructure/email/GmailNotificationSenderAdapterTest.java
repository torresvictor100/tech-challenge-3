package notification_service.infrastructure.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.UUID;
import notification_service.application.port.out.NotificationSenderPort.DeliveryResult;
import notification_service.domain.Notification;
import notification_service.domain.NotificationChannel;
import org.junit.jupiter.api.Test;

class GmailNotificationSenderAdapterTest {

	@Test
	void doesNotAttemptToSendEmailWhenGmailIsNotConfigured() {
		GmailNotificationSenderAdapter adapter = new GmailNotificationSenderAdapter(
				"smtp.gmail.com",
				587,
				"",
				"",
				"");

		DeliveryResult result = adapter.send(notification(NotificationChannel.EMAIL));

		assertFalse(result.delivered());
		assertEquals(
				"Gmail SMTP is not configured; notification was registered but email was not sent.",
				result.failureReason());
	}

	@Test
	void skipsUnsupportedChannels() {
		GmailNotificationSenderAdapter adapter = new GmailNotificationSenderAdapter(
				"smtp.gmail.com",
				587,
				"sender@gmail.com",
				"app-password",
				"sender@gmail.com");

		DeliveryResult result = adapter.send(notification(NotificationChannel.SMS));

		assertFalse(result.delivered());
		assertEquals("Gmail sender supports only EMAIL notifications.", result.failureReason());
	}

	private static Notification notification(NotificationChannel channel) {
		return Notification.create(
				UUID.fromString("00000000-0000-0000-0000-000000000901"),
				"paciente@example.com",
				"Consulta agendada",
				"Sua consulta foi registrada.",
				channel);
	}
}
