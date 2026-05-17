package appointment_service.application.port.out;

import appointment_service.domain.Appointment;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepositoryPort {

	Appointment save(Appointment appointment);

	void delete(Appointment appointment);

	Optional<Appointment> findById(UUID appointmentId);

	List<Appointment> findAll();

	List<Appointment> findByPatientId(UUID patientId);

	List<Appointment> findByPatientIdAndScheduledAtAfter(UUID patientId, Instant scheduledAt);

	default boolean existsByProfessionalIdAndScheduledAt(
			UUID professionalId,
			Instant scheduledAt,
			UUID ignoredAppointmentId) {
		return findAll()
				.stream()
				.anyMatch(appointment -> appointment.getProfessionalId().equals(professionalId)
						&& appointment.getScheduledAt().equals(scheduledAt)
						&& (ignoredAppointmentId == null || !appointment.getId().equals(ignoredAppointmentId)));
	}
}
