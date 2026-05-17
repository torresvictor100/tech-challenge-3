package appointment_service.infrastructure.cassandra;

import static org.junit.jupiter.api.Assertions.assertEquals;

import appointment_service.domain.Appointment;
import appointment_service.domain.AppointmentStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PatientAppointmentRowTest {

	@Test
	void mapsAppointmentToPatientHistoryRowAndBack() {
		UUID appointmentId = UUID.fromString("00000000-0000-0000-0000-000000000001");
		UUID patientId = UUID.fromString("00000000-0000-0000-0000-000000000002");
		UUID professionalId = UUID.fromString("00000000-0000-0000-0000-000000000003");
		Instant scheduledAt = Instant.parse("2030-04-13T10:00:00Z");
		Instant createdAt = Instant.parse("2026-04-13T10:00:00Z");
		Instant updatedAt = Instant.parse("2026-04-13T10:30:00Z");

		Appointment appointment = Appointment.restore(
				appointmentId,
				patientId,
				professionalId,
				"Dra. Ana Silva",
				scheduledAt,
				AppointmentStatus.CONFIRMED,
				"Retorno cardiologia",
				createdAt,
				updatedAt);

		PatientAppointmentRow row = PatientAppointmentRow.from(appointment);
		Appointment restored = row.toAppointment();

		assertEquals(patientId, row.patientId());
		assertEquals(scheduledAt, row.scheduledAt());
		assertEquals(appointmentId, row.appointmentId());
		assertEquals(professionalId, row.professionalId());
		assertEquals("Dra. Ana Silva", row.professionalName());
		assertEquals(AppointmentStatus.CONFIRMED.name(), row.status());
		assertEquals("Retorno cardiologia", row.notes());
		assertEquals(createdAt, row.createdAt());
		assertEquals(updatedAt, row.updatedAt());

		assertEquals(appointment.getId(), restored.getId());
		assertEquals(appointment.getPatientId(), restored.getPatientId());
		assertEquals(appointment.getProfessionalId(), restored.getProfessionalId());
		assertEquals(appointment.getProfessionalName(), restored.getProfessionalName());
		assertEquals(appointment.getScheduledAt(), restored.getScheduledAt());
		assertEquals(appointment.getStatus(), restored.getStatus());
		assertEquals(appointment.getNotes(), restored.getNotes());
		assertEquals(appointment.getCreatedAt(), restored.getCreatedAt());
		assertEquals(appointment.getUpdatedAt(), restored.getUpdatedAt());
	}
}
