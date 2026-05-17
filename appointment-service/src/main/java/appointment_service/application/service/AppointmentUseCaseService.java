package appointment_service.application.service;

import appointment_service.application.AppointmentView;
import appointment_service.application.port.in.CancelAppointmentUseCase;
import appointment_service.application.port.in.ConfirmAppointmentUseCase;
import appointment_service.application.port.in.GetAppointmentUseCase;
import appointment_service.application.port.in.ListAppointmentsUseCase;
import appointment_service.application.port.in.ListPatientAppointmentsUseCase;
import appointment_service.application.port.in.ScheduleAppointmentUseCase;
import appointment_service.application.port.in.UpdateAppointmentUseCase;
import appointment_service.application.port.out.AppointmentEventPublisherPort;
import appointment_service.application.port.out.AppointmentRepositoryPort;
import appointment_service.application.port.out.UserRepositoryPort;
import appointment_service.domain.Appointment;
import appointment_service.domain.AppointmentConflictException;
import appointment_service.domain.BusinessRuleException;
import appointment_service.domain.ResourceNotFoundException;
import appointment_service.domain.User;
import appointment_service.domain.UserRole;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class AppointmentUseCaseService implements
		ScheduleAppointmentUseCase,
		UpdateAppointmentUseCase,
		GetAppointmentUseCase,
		ListAppointmentsUseCase,
		ListPatientAppointmentsUseCase,
		ConfirmAppointmentUseCase,
		CancelAppointmentUseCase {

	private final AppointmentRepositoryPort appointmentRepository;
	private final AppointmentEventPublisherPort eventPublisher;
	private final UserRepositoryPort userRepository;

	public AppointmentUseCaseService(
			AppointmentRepositoryPort appointmentRepository,
			AppointmentEventPublisherPort eventPublisher,
			UserRepositoryPort userRepository) {
		this.appointmentRepository = appointmentRepository;
		this.eventPublisher = eventPublisher;
		this.userRepository = userRepository;
	}

	@Override
	public AppointmentView schedule(ScheduleAppointmentCommand command) {
		validatePatient(command.patientId());
		User professional = validateProfessional(command.professionalId());
		validateScheduleConflict(command.professionalId(), command.scheduledAt(), null);

		Appointment appointment = Appointment.schedule(
				command.patientId(),
				command.professionalId(),
				resolveProfessionalName(command.professionalName(), professional),
				command.scheduledAt(),
				command.status(),
				command.notes());

		Appointment savedAppointment = appointmentRepository.save(appointment);
		eventPublisher.appointmentScheduled(savedAppointment);
		return AppointmentView.from(savedAppointment);
	}

	@Override
	public AppointmentView update(UUID appointmentId, UpdateAppointmentCommand command) {
		Appointment currentAppointment = findAppointment(appointmentId);
		UUID professionalId = command.professionalId() == null
				? currentAppointment.getProfessionalId()
				: command.professionalId();
		Instant scheduledAt = command.scheduledAt() == null
				? currentAppointment.getScheduledAt()
				: command.scheduledAt();
		User professional = validateProfessional(professionalId);
		validateScheduleConflict(professionalId, scheduledAt, appointmentId);
		Appointment updatedAppointment = currentAppointment.update(
				professionalId,
				resolveProfessionalName(command.professionalName(), professional),
				command.scheduledAt(),
				command.status(),
				command.notes());

		appointmentRepository.delete(currentAppointment);
		Appointment savedAppointment = appointmentRepository.save(updatedAppointment);
		eventPublisher.appointmentUpdated(savedAppointment);
		return AppointmentView.from(savedAppointment);
	}

	@Override
	public AppointmentView getById(UUID appointmentId) {
		return AppointmentView.from(findAppointment(appointmentId));
	}

	@Override
	public List<AppointmentView> list() {
		return appointmentRepository.findAll()
				.stream()
				.sorted(Comparator.comparing(Appointment::getScheduledAt).reversed())
				.map(AppointmentView::from)
				.toList();
	}

	@Override
	public List<AppointmentView> listByPatient(UUID patientId) {
		return appointmentRepository.findByPatientId(patientId)
				.stream()
				.map(AppointmentView::from)
				.toList();
	}

	@Override
	public List<AppointmentView> listUpcomingByPatient(UUID patientId) {
		return appointmentRepository.findByPatientIdAndScheduledAtAfter(patientId, Instant.now())
				.stream()
				.map(AppointmentView::from)
				.toList();
	}

	@Override
	public AppointmentView confirm(UUID appointmentId) {
		Appointment appointment = findAppointment(appointmentId);
		appointment.confirm();

		Appointment savedAppointment = appointmentRepository.save(appointment);
		eventPublisher.appointmentConfirmed(savedAppointment);
		return AppointmentView.from(savedAppointment);
	}

	@Override
	public AppointmentView cancel(UUID appointmentId) {
		Appointment appointment = findAppointment(appointmentId);
		appointment.cancel();

		Appointment savedAppointment = appointmentRepository.save(appointment);
		eventPublisher.appointmentCanceled(savedAppointment);
		return AppointmentView.from(savedAppointment);
	}

	private Appointment findAppointment(UUID appointmentId) {
		return appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"APPOINTMENT_NOT_FOUND",
						"Appointment not found: " + appointmentId));
	}

	private void validatePatient(UUID patientId) {
		boolean exists = userRepository.existsByIdAndRole(patientId, UserRole.PACIENTE);
		if (!exists) {
			throw new ResourceNotFoundException("PATIENT_NOT_FOUND", "Patient not found: " + patientId);
		}
	}

	private User validateProfessional(UUID professionalId) {
		User professional = userRepository.findById(professionalId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"PROFESSIONAL_NOT_FOUND",
						"Professional not found: " + professionalId));

		if (professional.role() != UserRole.MEDICO && professional.role() != UserRole.ENFERMEIRO) {
			throw new BusinessRuleException(
					"INVALID_PROFESSIONAL",
					"Professional must have MEDICO or ENFERMEIRO role.");
		}

		return professional;
	}

	private void validateScheduleConflict(UUID professionalId, Instant scheduledAt, UUID ignoredAppointmentId) {
		if (appointmentRepository.existsByProfessionalIdAndScheduledAt(professionalId, scheduledAt, ignoredAppointmentId)) {
			throw new AppointmentConflictException("Professional already has an appointment at this time.");
		}
	}

	private static String resolveProfessionalName(String commandProfessionalName, User professional) {
		if (commandProfessionalName == null || commandProfessionalName.isBlank()) {
			return professional.fullName();
		}

		return commandProfessionalName;
	}
}
