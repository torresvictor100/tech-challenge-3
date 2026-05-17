package notification_service.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Notification {

	private final UUID id;
	private final UUID appointmentId;
	private final String recipient;
	private final String subject;
	private final String message;
	private final NotificationChannel channel;
	private final Instant createdAt;
	private NotificationStatus status;
	private Instant sentAt;
	private String failureReason;

	private Notification(
			UUID id,
			UUID appointmentId,
			String recipient,
			String subject,
			String message,
			NotificationChannel channel,
			NotificationStatus status,
			Instant createdAt,
			Instant sentAt,
			String failureReason) {
		this.id = require(id, "Notification id is required");
		this.appointmentId = require(appointmentId, "Appointment id is required");
		this.recipient = requireText(recipient, "Recipient is required");
		this.subject = requireText(subject, "Subject is required");
		this.message = requireText(message, "Message is required");
		this.channel = require(channel, "Notification channel is required");
		this.status = require(status, "Notification status is required");
		this.createdAt = require(createdAt, "Creation date is required");
		this.sentAt = sentAt;
		this.failureReason = normalize(failureReason);
	}

	public static Notification create(
			UUID appointmentId,
			String recipient,
			String subject,
			String message,
			NotificationChannel channel) {
		return new Notification(
				UUID.randomUUID(),
				appointmentId,
				recipient,
				subject,
				message,
				channel,
				NotificationStatus.PENDING,
				Instant.now(),
				null,
				null);
	}

	public static Notification restore(
			UUID id,
			UUID appointmentId,
			String recipient,
			String subject,
			String message,
			NotificationChannel channel,
			NotificationStatus status,
			Instant createdAt,
			Instant sentAt,
			String failureReason) {
		return new Notification(
				id,
				appointmentId,
				recipient,
				subject,
				message,
				channel,
				status,
				createdAt,
				sentAt,
				failureReason);
	}

	public void markSent() {
		if (status == NotificationStatus.SENT) {
			throw new NotificationDomainException("Notification was already sent");
		}

		status = NotificationStatus.SENT;
		sentAt = Instant.now();
		failureReason = null;
	}

	public void markFailed(String reason) {
		status = NotificationStatus.FAILED;
		failureReason = normalize(reason);
	}

	public UUID getId() {
		return id;
	}

	public UUID getAppointmentId() {
		return appointmentId;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getSubject() {
		return subject;
	}

	public String getMessage() {
		return message;
	}

	public NotificationChannel getChannel() {
		return channel;
	}

	public NotificationStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getSentAt() {
		return sentAt;
	}

	public String getFailureReason() {
		return failureReason;
	}

	private static <T> T require(T value, String message) {
		return Objects.requireNonNull(value, message);
	}

	private static String requireText(String value, String message) {
		String normalized = normalize(value);
		if (normalized == null) {
			throw new NotificationDomainException(message);
		}

		return normalized;
	}

	private static String normalize(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}
}
