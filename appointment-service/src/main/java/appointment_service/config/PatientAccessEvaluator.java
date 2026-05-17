package appointment_service.config;

import appointment_service.application.port.out.AppointmentRepositoryPort;
import appointment_service.application.port.out.UserRepositoryPort;
import appointment_service.domain.UserRole;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component("patientAccessEvaluator")
public class PatientAccessEvaluator {

	private final UserRepositoryPort userRepository;
	private final AppointmentRepositoryPort appointmentRepository;

	public PatientAccessEvaluator(
			UserRepositoryPort userRepository,
			AppointmentRepositoryPort appointmentRepository) {
		this.userRepository = userRepository;
		this.appointmentRepository = appointmentRepository;
	}

	public boolean canReadPatient(UUID patientId, String username) {
		if (patientId == null || username == null) {
			return false;
		}

		return userRepository.findByEmail(username)
				.filter(user -> user.role() == UserRole.PACIENTE)
				.map(user -> user.id().equals(patientId))
				.orElse(false);
	}

	public boolean canReadPatient(String patientId, String username) {
		try {
			return canReadPatient(UUID.fromString(patientId), username);
		}
		catch (IllegalArgumentException exception) {
			return false;
		}
	}

	public boolean canReadAppointment(UUID appointmentId, String username) {
		if (appointmentId == null || username == null) {
			return false;
		}

		return appointmentRepository.findById(appointmentId)
				.map(appointment -> canReadPatient(appointment.getPatientId(), username))
				.orElse(false);
	}

	public boolean canReadAppointment(String appointmentId, String username) {
		try {
			return canReadAppointment(UUID.fromString(appointmentId), username);
		}
		catch (IllegalArgumentException exception) {
			return false;
		}
	}
}
