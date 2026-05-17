package appointment_service.infrastructure.cassandra;

import appointment_service.application.port.out.UserRepositoryPort;
import appointment_service.domain.User;
import appointment_service.domain.UserRole;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("docker")
public class CassandraUserRepositoryAdapter implements UserRepositoryPort {

	private final CqlSession session;
	private final String keyspace;

	public CassandraUserRepositoryAdapter(
			CqlSession session,
			@Value("${spring.cassandra.keyspace-name:${spring.cassandra.keyspace:hospital}}") String keyspace) {
		this.session = session;
		this.keyspace = validateIdentifier(keyspace);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		if (email == null) {
			return Optional.empty();
		}

		Row row = session.execute(SimpleStatement.newInstance(
				"SELECT email, user_id, full_name, password_hash, role FROM " + keyspace
						+ ".users_by_email WHERE email = ?",
				email.toLowerCase())).one();
		return Optional.ofNullable(row).map(this::toUser);
	}

	@Override
	public Optional<User> findById(UUID userId) {
		if (userId == null) {
			return Optional.empty();
		}

		return session.execute("SELECT email, user_id, full_name, password_hash, role FROM "
				+ keyspace + ".users_by_email")
				.all()
				.stream()
				.filter(row -> userId.equals(row.getUuid("user_id")))
				.findFirst()
				.map(this::toUser);
	}

	private User toUser(Row row) {
		return new User(
				row.getUuid("user_id"),
				row.getString("email"),
				row.getString("full_name"),
				row.getString("password_hash"),
				UserRole.valueOf(row.getString("role")));
	}

	private static String validateIdentifier(String value) {
		if (value == null || !value.matches("[A-Za-z_][A-Za-z0-9_]*")) {
			throw new IllegalArgumentException("Invalid Cassandra identifier: " + value);
		}

		return value;
	}
}
