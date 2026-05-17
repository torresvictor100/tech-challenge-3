package appointment_service.infrastructure.memory;

import appointment_service.application.port.out.AppointmentRepositoryPort;
import appointment_service.domain.Appointment;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class InMemoryAppointmentRepositoryAdapter implements AppointmentRepositoryPort {

	private final ConcurrentMap<UUID, Appointment> appointments = new ConcurrentHashMap<>();

	@Override
	public Appointment save(Appointment appointment) {
		appointments.put(appointment.getId(), appointment);
		return appointment;
	}

	@Override
	public void delete(Appointment appointment) {
		appointments.remove(appointment.getId());
	}

	@Override
	public Optional<Appointment> findById(UUID appointmentId) {
		return Optional.ofNullable(appointments.get(appointmentId));
	}

	@Override
	public List<Appointment> findAll() {
		return new ArrayList<>(appointments.values());
	}

	@Override
	public List<Appointment> findByPatientId(UUID patientId) {
		return appointments.values()
				.stream()
				.filter(appointment -> appointment.getPatientId().equals(patientId))
				.sorted(Comparator.comparing(Appointment::getScheduledAt).reversed()
						.thenComparing(Appointment::getId))
				.toList();
	}

	@Override
	public List<Appointment> findByPatientIdAndScheduledAtAfter(UUID patientId, Instant scheduledAt) {
		return findByPatientId(patientId)
				.stream()
				.filter(appointment -> !appointment.getScheduledAt().isBefore(scheduledAt))
				.toList();
	}
}
