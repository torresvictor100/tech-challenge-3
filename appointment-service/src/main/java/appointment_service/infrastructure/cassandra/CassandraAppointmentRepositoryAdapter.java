package appointment_service.infrastructure.cassandra;

import appointment_service.application.port.out.AppointmentRepositoryPort;
import appointment_service.domain.Appointment;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("docker")
public class CassandraAppointmentRepositoryAdapter implements AppointmentRepositoryPort {

	private final CqlSession session;
	private final String keyspace;

	public CassandraAppointmentRepositoryAdapter(
			CqlSession session,
			@Value("${spring.cassandra.keyspace-name:${spring.cassandra.keyspace:hospital}}") String keyspace) {
		this.session = session;
		this.keyspace = validateIdentifier(keyspace);
	}

	@Override
	public Appointment save(Appointment appointment) {
		PatientAppointmentRow row = PatientAppointmentRow.from(appointment);
		session.execute(SimpleStatement.newInstance("""
				INSERT INTO %s.patient_appointments_by_patient (
					patient_id,
					scheduled_at,
					appointment_id,
					professional_id,
					professional_name,
					status,
					notes,
					created_at,
					updated_at
				) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
				""".formatted(keyspace),
				row.patientId(),
				row.scheduledAt(),
				row.appointmentId(),
				row.professionalId(),
				row.professionalName(),
				row.status(),
				row.notes(),
				row.createdAt(),
				row.updatedAt()));

		return appointment;
	}

	@Override
	public void delete(Appointment appointment) {
		session.execute(SimpleStatement.newInstance(
				"DELETE FROM " + keyspace
						+ ".patient_appointments_by_patient WHERE patient_id = ? AND scheduled_at = ? AND appointment_id = ?",
				appointment.getPatientId(),
				appointment.getScheduledAt(),
				appointment.getId()));
	}

	@Override
	public Optional<Appointment> findById(UUID appointmentId) {
		return findAll()
				.stream()
				.filter(appointment -> appointment.getId().equals(appointmentId))
				.findFirst();
	}

	@Override
	public List<Appointment> findAll() {
		List<Row> rows = session.execute("SELECT * FROM " + keyspace + ".patient_appointments_by_patient").all();
		return toAppointments(rows);
	}

	@Override
	public List<Appointment> findByPatientId(UUID patientId) {
		List<Row> rows = session.execute(SimpleStatement.newInstance(
				"SELECT * FROM " + keyspace + ".patient_appointments_by_patient WHERE patient_id = ?",
				patientId)).all();

		return toAppointments(rows);
	}

	@Override
	public List<Appointment> findByPatientIdAndScheduledAtAfter(UUID patientId, Instant scheduledAt) {
		List<Row> rows = session.execute(SimpleStatement.newInstance(
				"SELECT * FROM " + keyspace
						+ ".patient_appointments_by_patient WHERE patient_id = ? AND scheduled_at >= ?",
				patientId,
				scheduledAt)).all();

		return toAppointments(rows);
	}

	private List<Appointment> toAppointments(List<Row> rows) {
		return rows.stream()
				.map(this::toPatientAppointmentRow)
				.map(PatientAppointmentRow::toAppointment)
				.toList();
	}

	private PatientAppointmentRow toPatientAppointmentRow(Row row) {
		return new PatientAppointmentRow(
				row.getUuid("patient_id"),
				row.getInstant("scheduled_at"),
				row.getUuid("appointment_id"),
				row.getUuid("professional_id"),
				row.getString("professional_name"),
				row.getString("status"),
				row.getString("notes"),
				row.getInstant("created_at"),
				row.getInstant("updated_at"));
	}

	private static String validateIdentifier(String value) {
		if (value == null || !value.matches("[A-Za-z_][A-Za-z0-9_]*")) {
			throw new IllegalArgumentException("Invalid Cassandra identifier: " + value);
		}

		return value;
	}
}
