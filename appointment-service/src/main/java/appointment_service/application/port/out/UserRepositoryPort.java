package appointment_service.application.port.out;

import appointment_service.domain.User;
import appointment_service.domain.UserRole;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

	Optional<User> findByEmail(String email);

	Optional<User> findById(UUID userId);

	default boolean existsByIdAndRole(UUID userId, UserRole role) {
		return findById(userId)
				.map(user -> user.role() == role)
				.orElse(false);
	}
}
