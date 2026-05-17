package notification_service.infrastructure.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import notification_service.application.port.out.NotificationRepositoryPort;
import notification_service.domain.Notification;
import notification_service.domain.NotificationChannel;
import notification_service.domain.NotificationStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("docker")
public class CassandraNotificationRepositoryAdapter implements NotificationRepositoryPort {

	private final CqlSession session;
	private final String keyspace;

	public CassandraNotificationRepositoryAdapter(
			CqlSession session,
			@Value("${spring.cassandra.keyspace-name:hospital}") String keyspace) {
		this.session = session;
		this.keyspace = keyspace;
	}

	@Override
	public Notification save(Notification notification) {
		session.execute(SimpleStatement.newInstance("""
				INSERT INTO %s.notifications (
					id,
					appointment_id,
					recipient,
					subject,
					message,
					channel,
					status,
					created_at,
					sent_at,
					failure_reason
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""".formatted(keyspace),
				notification.getId(),
				notification.getAppointmentId(),
				notification.getRecipient(),
				notification.getSubject(),
				notification.getMessage(),
				notification.getChannel().name(),
				notification.getStatus().name(),
				notification.getCreatedAt(),
				notification.getSentAt(),
				notification.getFailureReason()));

		return notification;
	}

	@Override
	public Optional<Notification> findById(UUID notificationId) {
		Row row = session.execute(SimpleStatement.newInstance(
				"SELECT * FROM " + keyspace + ".notifications WHERE id = ?",
				notificationId)).one();

		return Optional.ofNullable(row).map(this::toNotification);
	}

	@Override
	public List<Notification> findAll() {
		return session.execute("SELECT * FROM " + keyspace + ".notifications")
				.all()
				.stream()
				.map(this::toNotification)
				.toList();
	}

	private Notification toNotification(Row row) {
		return Notification.restore(
				row.getUuid("id"),
				row.getUuid("appointment_id"),
				row.getString("recipient"),
				row.getString("subject"),
				row.getString("message"),
				NotificationChannel.valueOf(row.getString("channel")),
				NotificationStatus.valueOf(row.getString("status")),
				row.getInstant("created_at"),
				row.getInstant("sent_at"),
				row.getString("failure_reason"));
	}
}
