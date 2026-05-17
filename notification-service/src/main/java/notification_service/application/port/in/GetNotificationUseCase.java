package notification_service.application.port.in;

import java.util.UUID;
import notification_service.application.NotificationView;

public interface GetNotificationUseCase {

	NotificationView getById(UUID notificationId);
}
