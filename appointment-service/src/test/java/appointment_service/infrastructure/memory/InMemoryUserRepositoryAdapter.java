package appointment_service.infrastructure.memory;

import appointment_service.application.port.out.UserRepositoryPort;
import appointment_service.domain.User;
import appointment_service.domain.UserRole;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class InMemoryUserRepositoryAdapter implements UserRepositoryPort {

	public static final UUID SAMPLE_PATIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000601");
	public static final UUID SAMPLE_DOCTOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000701");
	public static final UUID SAMPLE_NURSE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	private final ConcurrentMap<String, User> usersByEmail = new ConcurrentHashMap<>();
	private final ConcurrentMap<UUID, User> usersById = new ConcurrentHashMap<>();

	public InMemoryUserRepositoryAdapter(PasswordEncoder passwordEncoder) {
		String passwordHash = passwordEncoder.encode("123456");
		seed(List.of(
				new User(SAMPLE_DOCTOR_ID, "medico@hospital.com", "Dra. Marina Costa", passwordHash, UserRole.MEDICO),
				new User(SAMPLE_DOCTOR_ID, "doctor@hospital.com", "Dra. Marina Costa", passwordHash, UserRole.MEDICO),
				new User(SAMPLE_NURSE_ID, "enfermeiro@hospital.com", "Enf. Paulo Ramos", passwordHash, UserRole.ENFERMEIRO),
				new User(SAMPLE_NURSE_ID, "nurse@hospital.com", "Enf. Paulo Ramos", passwordHash, UserRole.ENFERMEIRO),
				new User(SAMPLE_PATIENT_ID, "paciente@hospital.com", "Paciente Exemplo", passwordHash, UserRole.PACIENTE),
				new User(SAMPLE_PATIENT_ID, "patient@hospital.com", "Paciente Exemplo", passwordHash, UserRole.PACIENTE)));
	}

	private void seed(List<User> users) {
		for (User user : users) {
			usersByEmail.put(user.email(), user);
			usersById.putIfAbsent(user.id(), user);
		}
	}

	@Override
	public Optional<User> findByEmail(String email) {
		if (email == null) {
			return Optional.empty();
		}

		return Optional.ofNullable(usersByEmail.get(email.toLowerCase()));
	}

	@Override
	public Optional<User> findById(UUID userId) {
		if (userId == null) {
			return Optional.empty();
		}

		return Optional.ofNullable(usersById.get(userId));
	}

	public Map<String, User> usersByEmail() {
		return Map.copyOf(usersByEmail);
	}
}
