package notification_service.application.port.in;

import java.util.UUID;
import notification_service.application.NotificationView;
import notification_service.domain.NotificationChannel;

public interface CreateNotificationUseCase {

	NotificationView create(CreateNotificationCommand command);

	record CreateNotificationCommand(
			UUID appointmentId,
			String recipient,
			String subject,
			String message,
			NotificationChannel channel) {
	}
}
