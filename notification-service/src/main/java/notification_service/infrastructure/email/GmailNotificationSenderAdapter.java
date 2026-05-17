package notification_service.infrastructure.email;

import java.util.Properties;
import notification_service.application.port.out.NotificationSenderPort;
import notification_service.domain.Notification;
import notification_service.domain.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class GmailNotificationSenderAdapter implements NotificationSenderPort {

	private static final Logger LOGGER = LoggerFactory.getLogger(GmailNotificationSenderAdapter.class);
	private static final String NOT_CONFIGURED_REASON =
			"Gmail SMTP is not configured; notification was registered but email was not sent.";

	private final String host;
	private final int port;
	private final String username;
	private final String appPassword;
	private final String from;

	public GmailNotificationSenderAdapter(
			@Value("${app.email.gmail.host:smtp.gmail.com}") String host,
			@Value("${app.email.gmail.port:587}") int port,
			@Value("${app.email.gmail.username:}") String username,
			@Value("${app.email.gmail.app-password:}") String appPassword,
			@Value("${app.email.gmail.from:}") String from) {
		this.host = host;
		this.port = port;
		this.username = normalize(username);
		this.appPassword = normalize(appPassword);
		this.from = normalize(from);
	}

	@Override
	public DeliveryResult send(Notification notification) {
		if (notification.getChannel() != NotificationChannel.EMAIL) {
			String reason = "Gmail sender supports only EMAIL notifications.";
			LOGGER.info("notification.email.skipped notificationId={} channel={} reason={}",
					notification.getId(), notification.getChannel(), reason);
			return DeliveryResult.notSent(reason);
		}

		if (!isConfigured()) {
			LOGGER.info("notification.email.not-configured notificationId={} recipient={}",
					notification.getId(), notification.getRecipient());
			return DeliveryResult.notSent(NOT_CONFIGURED_REASON);
		}

		try {
			JavaMailSenderImpl mailSender = mailSender();
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(resolveFrom());
			message.setTo(notification.getRecipient());
			message.setSubject(notification.getSubject());
			message.setText(notification.getMessage());

			mailSender.send(message);
			LOGGER.info("notification.email.sent notificationId={} recipient={}",
					notification.getId(), notification.getRecipient());
			return DeliveryResult.sent();
		}
		catch (MailException exception) {
			String reason = "Gmail email send failed: " + rootMessage(exception);
			LOGGER.warn("notification.email.failed notificationId={} recipient={} reason={}",
					notification.getId(), notification.getRecipient(), reason);
			return DeliveryResult.notSent(reason);
		}
	}

	private JavaMailSenderImpl mailSender() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(host);
		mailSender.setPort(port);
		mailSender.setUsername(username);
		mailSender.setPassword(appPassword);
		mailSender.setJavaMailProperties(mailProperties());
		return mailSender;
	}

	private Properties mailProperties() {
		Properties properties = new Properties();
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.starttls.required", "true");
		return properties;
	}

	private boolean isConfigured() {
		return username != null && appPassword != null;
	}

	private String resolveFrom() {
		return from == null ? username : from;
	}

	private static String normalize(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}

	private static String rootMessage(Throwable throwable) {
		Throwable current = throwable;
		while (current.getCause() != null) {
			current = current.getCause();
		}

		return current.getMessage() == null ? throwable.getClass().getSimpleName() : current.getMessage();
	}
}
