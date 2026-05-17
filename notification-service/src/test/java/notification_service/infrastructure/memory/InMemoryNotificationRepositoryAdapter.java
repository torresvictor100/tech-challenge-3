package notification_service.infrastructure.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import notification_service.application.port.out.NotificationRepositoryPort;
import notification_service.domain.Notification;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class InMemoryNotificationRepositoryAdapter implements NotificationRepositoryPort {

	private final ConcurrentMap<UUID, Notification> notifications = new ConcurrentHashMap<>();

	@Override
	public Notification save(Notification notification) {
		notifications.put(notification.getId(), notification);
		return notification;
	}

	@Override
	public Optional<Notification> findById(UUID notificationId) {
		return Optional.ofNullable(notifications.get(notificationId));
	}

	@Override
	public List<Notification> findAll() {
		return new ArrayList<>(notifications.values());
	}
}
