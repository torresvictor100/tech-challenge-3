package notification_service.application;

import java.time.Instant;
import java.util.UUID;
import notification_service.domain.Notification;
import notification_service.domain.NotificationChannel;
import notification_service.domain.NotificationStatus;

public record NotificationView(
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

	public static NotificationView from(Notification notification) {
		return new NotificationView(
				notification.getId(),
				notification.getAppointmentId(),
				notification.getRecipient(),
				notification.getSubject(),
				notification.getMessage(),
				notification.getChannel(),
				notification.getStatus(),
				notification.getCreatedAt(),
				notification.getSentAt(),
				notification.getFailureReason());
	}
}
