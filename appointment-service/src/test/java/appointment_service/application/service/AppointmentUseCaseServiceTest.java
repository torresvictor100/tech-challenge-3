package appointment_service.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import appointment_service.application.AppointmentView;
import appointment_service.application.port.in.ScheduleAppointmentUseCase.ScheduleAppointmentCommand;
import appointment_service.application.port.in.UpdateAppointmentUseCase.UpdateAppointmentCommand;
import appointment_service.application.port.out.AppointmentEventPublisherPort;
import appointment_service.domain.Appointment;
import appointment_service.domain.AppointmentConflictException;
import appointment_service.domain.AppointmentStatus;
import appointment_service.infrastructure.memory.InMemoryUserRepositoryAdapter;
import appointment_service.infrastructure.memory.InMemoryAppointmentRepositoryAdapter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AppointmentUseCaseServiceTest {

	private final InMemoryAppointmentRepositoryAdapter repository = new InMemoryAppointmentRepositoryAdapter();
	private final RecordingAppointmentEventPublisher eventPublisher = new RecordingAppointmentEventPublisher();
	private final InMemoryUserRepositoryAdapter userRepository =
			new InMemoryUserRepositoryAdapter(new BCryptPasswordEncoder());
	private final AppointmentUseCaseService service =
			new AppointmentUseCaseService(repository, eventPublisher, userRepository);

	@Test
	void schedulesAndReadsPatientHistory() {
		UUID patientId = InMemoryUserRepositoryAdapter.SAMPLE_PATIENT_ID;
		Instant firstSlot = Instant.now().plusSeconds(3_600);
		Instant secondSlot = Instant.now().plusSeconds(7_200);

		AppointmentView firstAppointment = service.schedule(command(patientId, firstSlot));
		AppointmentView secondAppointment = service.schedule(command(patientId, secondSlot));

		List<AppointmentView> patientHistory = service.listByPatient(patientId);

		assertEquals(List.of(secondAppointment.id(), firstAppointment.id()),
				patientHistory.stream().map(AppointmentView::id).toList());
		assertEquals(List.of(firstAppointment.id(), secondAppointment.id()),
				eventPublisher.scheduledAppointmentIds());
	}

	@Test
	void updatesAppointmentAndPublishesUpdateEvent() {
		UUID patientId = InMemoryUserRepositoryAdapter.SAMPLE_PATIENT_ID;
		AppointmentView appointment = service.schedule(command(patientId, Instant.now().plusSeconds(3_600)));
		Instant newSlot = Instant.now().plusSeconds(7_200);

		AppointmentView updated = service.update(appointment.id(), new UpdateAppointmentCommand(
				InMemoryUserRepositoryAdapter.SAMPLE_NURSE_ID,
				"Dr. Paulo Ramos",
				newSlot,
				AppointmentStatus.CONFIRMED,
				"Consulta atualizada"));

		assertEquals(newSlot, updated.scheduledAt());
		assertEquals(AppointmentStatus.CONFIRMED, updated.status());
		assertEquals(List.of(appointment.id()), eventPublisher.updatedAppointmentIds());
	}

	@Test
	void doesNotSaveOrPublishWhenProfessionalScheduleConflicts() {
		UUID patientId = InMemoryUserRepositoryAdapter.SAMPLE_PATIENT_ID;
		Instant slot = Instant.now().plusSeconds(3_600);

		service.schedule(command(patientId, slot));

		assertThrows(AppointmentConflictException.class, () -> service.schedule(command(patientId, slot)));
		assertEquals(1, repository.findAll().size());
		assertEquals(1, eventPublisher.scheduledAppointmentIds().size());
	}

	private static ScheduleAppointmentCommand command(UUID patientId, Instant scheduledAt) {
		return new ScheduleAppointmentCommand(
				patientId,
				InMemoryUserRepositoryAdapter.SAMPLE_DOCTOR_ID,
				"Dra. Marina Costa",
				scheduledAt,
				AppointmentStatus.SCHEDULED,
				"Consulta inicial");
	}

	private static final class RecordingAppointmentEventPublisher implements AppointmentEventPublisherPort {

		private final List<UUID> scheduledAppointmentIds = new ArrayList<>();
		private final List<UUID> updatedAppointmentIds = new ArrayList<>();

		@Override
		public void appointmentScheduled(Appointment appointment) {
			scheduledAppointmentIds.add(appointment.getId());
		}

		@Override
		public void appointmentUpdated(Appointment appointment) {
			updatedAppointmentIds.add(appointment.getId());
		}

		@Override
		public void appointmentConfirmed(Appointment appointment) {
		}

		@Override
		public void appointmentCanceled(Appointment appointment) {
		}

		private List<UUID> scheduledAppointmentIds() {
			return scheduledAppointmentIds;
		}

		private List<UUID> updatedAppointmentIds() {
			return updatedAppointmentIds;
		}
	}
}
