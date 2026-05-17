package notification_service.application.port.in;

import java.util.UUID;
import notification_service.application.NotificationView;

public interface SendNotificationUseCase {

	NotificationView send(UUID notificationId);
}
