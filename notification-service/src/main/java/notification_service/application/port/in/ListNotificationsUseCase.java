package notification_service.application.port.in;

import java.util.List;
import notification_service.application.NotificationView;

public interface ListNotificationsUseCase {

	List<NotificationView> list();
}
