package notification_service.infrastructure.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("docker")
class CassandraNotificationConfiguration {

	@Bean
	CqlSession notificationCqlSession(
			@Value("${spring.cassandra.contact-points:cassandra}") String contactPoints,
			@Value("${spring.cassandra.port:9042}") int port,
			@Value("${spring.cassandra.keyspace-name:hospital}") String keyspaceName,
			@Value("${spring.cassandra.local-datacenter:dc1}") String localDatacenter) {
		String keyspace = validateIdentifier(keyspaceName);
		CqlSession session = CqlSession.builder()
				.addContactPoint(new InetSocketAddress(contactPoints, port))
				.withLocalDatacenter(localDatacenter)
				.build();

		session.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace
				+ " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
		session.execute("""
				CREATE TABLE IF NOT EXISTS %s.notifications (
					id uuid PRIMARY KEY,
					appointment_id uuid,
					recipient text,
					subject text,
					message text,
					channel text,
					status text,
					created_at timestamp,
					sent_at timestamp,
					failure_reason text
				)
				""".formatted(keyspace));

		return session;
	}

	private static String validateIdentifier(String value) {
		if (value == null || !value.matches("[A-Za-z_][A-Za-z0-9_]*")) {
			throw new IllegalArgumentException("Invalid Cassandra identifier: " + value);
		}

		return value;
	}
}
