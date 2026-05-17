package appointment_service.infrastructure.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("docker")
class CassandraAppointmentConfiguration {

	@Bean
	CqlSession appointmentCqlSession(
			@Value("${spring.cassandra.contact-points:cassandra}") String contactPoints,
			@Value("${spring.cassandra.port:9042}") int port,
			@Value("${spring.cassandra.keyspace-name:${spring.cassandra.keyspace:hospital}}") String keyspaceName,
			@Value("${spring.cassandra.local-datacenter:dc1}") String localDatacenter) {
		String keyspace = validateIdentifier(keyspaceName);
		CqlSession session = CqlSession.builder()
				.addContactPoint(new InetSocketAddress(contactPoints, port))
				.withLocalDatacenter(localDatacenter)
				.build();

		session.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace
				+ " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1}");
		session.execute("""
				CREATE TABLE IF NOT EXISTS %s.users_by_email (
					email text PRIMARY KEY,
					user_id uuid,
					full_name text,
					password_hash text,
					role text
				)
				""".formatted(keyspace));
		session.execute("""
				CREATE TABLE IF NOT EXISTS %s.patient_appointments_by_patient (
					patient_id uuid,
					scheduled_at timestamp,
					appointment_id uuid,
					professional_id uuid,
					professional_name text,
					status text,
					notes text,
					created_at timestamp,
					updated_at timestamp,
					PRIMARY KEY ((patient_id), scheduled_at, appointment_id)
				) WITH CLUSTERING ORDER BY (scheduled_at DESC, appointment_id ASC)
				""".formatted(keyspace));
		seedUsers(session, keyspace);

		return session;
	}

	private static void seedUsers(CqlSession session, String keyspace) {
		String passwordHash = "$2b$10$.yKZpmk2LWU.BXWr9m8AyeY7iXn1rVMrytPbYiGxTCGWcE7vW4Ovy";
		seedUser(session, keyspace, "medico@hospital.com", "00000000-0000-0000-0000-000000000701",
				"Dra. Marina Costa", passwordHash, "MEDICO");
		seedUser(session, keyspace, "doctor@hospital.com", "00000000-0000-0000-0000-000000000701",
				"Dra. Marina Costa", passwordHash, "MEDICO");
		seedUser(session, keyspace, "enfermeiro@hospital.com", "33333333-3333-3333-3333-333333333333",
				"Enf. Paulo Ramos", passwordHash, "ENFERMEIRO");
		seedUser(session, keyspace, "nurse@hospital.com", "33333333-3333-3333-3333-333333333333",
				"Enf. Paulo Ramos", passwordHash, "ENFERMEIRO");
		seedUser(session, keyspace, "paciente@hospital.com", "00000000-0000-0000-0000-000000000601",
				"Paciente Exemplo", passwordHash, "PACIENTE");
		seedUser(session, keyspace, "patient@hospital.com", "00000000-0000-0000-0000-000000000601",
				"Paciente Exemplo", passwordHash, "PACIENTE");
	}

	private static void seedUser(
			CqlSession session,
			String keyspace,
			String email,
			String userId,
			String fullName,
			String passwordHash,
			String role) {
		session.execute("""
				INSERT INTO %s.users_by_email (email, user_id, full_name, password_hash, role)
				VALUES (?, ?, ?, ?, ?)
				""".formatted(keyspace),
				email,
				java.util.UUID.fromString(userId),
				fullName,
				passwordHash,
				role);
	}

	private static String validateIdentifier(String value) {
		if (value == null || !value.matches("[A-Za-z_][A-Za-z0-9_]*")) {
			throw new IllegalArgumentException("Invalid Cassandra identifier: " + value);
		}

		return value;
	}
}
