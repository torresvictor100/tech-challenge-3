package appointment_service.infrastructure.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import appointment_service.domain.Appointment;
import appointment_service.domain.AppointmentStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryAppointmentRepositoryAdapterTest {

	private final InMemoryAppointmentRepositoryAdapter repository = new InMemoryAppointmentRepositoryAdapter();

	@Test
	void findsPatientHistoryOrderedByScheduledAtDescending() {
		UUID patientId = UUID.fromString("00000000-0000-0000-0000-000000000101");
		Appointment oldest = appointment(
				"00000000-0000-0000-0000-000000000201",
				patientId,
				"2030-04-13T09:00:00Z");
		Appointment newest = appointment(
				"00000000-0000-0000-0000-000000000202",
				patientId,
				"2030-04-15T09:00:00Z");
		Appointment otherPatientAppointment = appointment(
				"00000000-0000-0000-0000-000000000203",
				UUID.fromString("00000000-0000-0000-0000-000000000102"),
				"2030-04-16T09:00:00Z");

		repository.save(oldest);
		repository.save(newest);
		repository.save(otherPatientAppointment);

		List<Appointment> history = repository.findByPatientId(patientId);

		assertEquals(List.of(newest.getId(), oldest.getId()), history.stream().map(Appointment::getId).toList());
	}

	@Test
	void findsUpcomingAppointmentsFromPatientHistoryTableShape() {
		UUID patientId = UUID.fromString("00000000-0000-0000-0000-000000000301");
		Appointment pastAppointment = appointment(
				"00000000-0000-0000-0000-000000000401",
				patientId,
				"2026-04-12T09:00:00Z");
		Appointment upcomingAppointment = appointment(
				"00000000-0000-0000-0000-000000000402",
				patientId,
				"2030-04-13T09:00:00Z");

		repository.save(pastAppointment);
		repository.save(upcomingAppointment);

		List<Appointment> upcoming = repository.findByPatientIdAndScheduledAtAfter(
				patientId,
				Instant.parse("2026-04-13T00:00:00Z"));

		assertEquals(List.of(upcomingAppointment.getId()), upcoming.stream().map(Appointment::getId).toList());
	}

	private static Appointment appointment(String appointmentId, UUID patientId, String scheduledAt) {
		return Appointment.restore(
				UUID.fromString(appointmentId),
				patientId,
				UUID.fromString("00000000-0000-0000-0000-000000000501"),
				"Dr. Carlos Lima",
				Instant.parse(scheduledAt),
				AppointmentStatus.SCHEDULED,
				"Consulta",
				Instant.parse("2026-04-13T08:00:00Z"),
				Instant.parse("2026-04-13T08:00:00Z"));
	}
}
