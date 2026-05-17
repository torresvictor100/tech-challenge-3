package notification_service.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import notification_service.domain.Notification;

public interface NotificationRepositoryPort {

	Notification save(Notification notification);

	Optional<Notification> findById(UUID notificationId);

	List<Notification> findAll();
}
