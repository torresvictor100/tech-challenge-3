package appointment_service.api.graphql;

import appointment_service.application.AppointmentView;
import appointment_service.application.port.in.CancelAppointmentUseCase;
import appointment_service.application.port.in.ConfirmAppointmentUseCase;
import appointment_service.application.port.in.GetAppointmentUseCase;
import appointment_service.application.port.in.ListAppointmentsUseCase;
import appointment_service.application.port.in.ListPatientAppointmentsUseCase;
import appointment_service.application.port.in.ScheduleAppointmentUseCase;
import appointment_service.application.port.in.ScheduleAppointmentUseCase.ScheduleAppointmentCommand;
import appointment_service.application.port.in.UpdateAppointmentUseCase;
import appointment_service.application.port.in.UpdateAppointmentUseCase.UpdateAppointmentCommand;
import appointment_service.domain.AppointmentStatus;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

@Controller
public class AppointmentGraphQlController {

	private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;
	private final GetAppointmentUseCase getAppointmentUseCase;
	private final ListAppointmentsUseCase listAppointmentsUseCase;
	private final ListPatientAppointmentsUseCase listPatientAppointmentsUseCase;
	private final ConfirmAppointmentUseCase confirmAppointmentUseCase;
	private final CancelAppointmentUseCase cancelAppointmentUseCase;
	private final UpdateAppointmentUseCase updateAppointmentUseCase;

	public AppointmentGraphQlController(
			ScheduleAppointmentUseCase scheduleAppointmentUseCase,
			GetAppointmentUseCase getAppointmentUseCase,
			ListAppointmentsUseCase listAppointmentsUseCase,
			ListPatientAppointmentsUseCase listPatientAppointmentsUseCase,
			ConfirmAppointmentUseCase confirmAppointmentUseCase,
			CancelAppointmentUseCase cancelAppointmentUseCase,
			UpdateAppointmentUseCase updateAppointmentUseCase) {
		this.scheduleAppointmentUseCase = scheduleAppointmentUseCase;
		this.getAppointmentUseCase = getAppointmentUseCase;
		this.listAppointmentsUseCase = listAppointmentsUseCase;
		this.listPatientAppointmentsUseCase = listPatientAppointmentsUseCase;
		this.confirmAppointmentUseCase = confirmAppointmentUseCase;
		this.cancelAppointmentUseCase = cancelAppointmentUseCase;
		this.updateAppointmentUseCase = updateAppointmentUseCase;
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
	@QueryMapping
	public List<AppointmentGraphQlResponse> appointments() {
		return listAppointmentsUseCase.list()
				.stream()
				.map(AppointmentGraphQlResponse::from)
				.toList();
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO') or @patientAccessEvaluator.canReadAppointment(#id, authentication.name)")
	@QueryMapping
	public AppointmentGraphQlResponse appointmentById(@Argument String id) {
		return AppointmentGraphQlResponse.from(getAppointmentUseCase.getById(UUID.fromString(id)));
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO') or @patientAccessEvaluator.canReadPatient(#patientId, authentication.name)")
	@QueryMapping
	public PatientHistoryGraphQlResponse patientHistory(@Argument String patientId) {
		UUID id = UUID.fromString(patientId);
		List<AppointmentGraphQlResponse> appointments = listPatientAppointmentsUseCase.listByPatient(id)
				.stream()
				.map(AppointmentGraphQlResponse::from)
				.toList();
		return new PatientHistoryGraphQlResponse(patientId, appointments);
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO') or @patientAccessEvaluator.canReadPatient(#patientId, authentication.name)")
	@QueryMapping
	public List<AppointmentGraphQlResponse> futureAppointments(@Argument String patientId) {
		return listPatientAppointmentsUseCase.listUpcomingByPatient(UUID.fromString(patientId))
				.stream()
				.map(AppointmentGraphQlResponse::from)
				.toList();
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO') or @patientAccessEvaluator.canReadPatient(#patientId, authentication.name)")
	@QueryMapping
	public List<AppointmentGraphQlResponse> patientAppointments(@Argument String patientId) {
		return listPatientAppointmentsUseCase.listByPatient(UUID.fromString(patientId))
				.stream()
				.map(AppointmentGraphQlResponse::from)
				.toList();
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO') or @patientAccessEvaluator.canReadPatient(#patientId, authentication.name)")
	@QueryMapping
	public List<AppointmentGraphQlResponse> upcomingPatientAppointments(@Argument String patientId) {
		return listPatientAppointmentsUseCase.listUpcomingByPatient(UUID.fromString(patientId))
				.stream()
				.map(AppointmentGraphQlResponse::from)
				.toList();
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
	@MutationMapping
	public AppointmentGraphQlResponse scheduleAppointment(@Argument ScheduleAppointmentGraphQlInput input) {
		return AppointmentGraphQlResponse.from(scheduleAppointmentUseCase.schedule(input.toCommand()));
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
	@MutationMapping
	public AppointmentGraphQlResponse updateAppointment(
			@Argument String id,
			@Argument UpdateAppointmentGraphQlInput input) {
		return AppointmentGraphQlResponse.from(updateAppointmentUseCase.update(UUID.fromString(id), input.toCommand()));
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
	@MutationMapping
	public AppointmentGraphQlResponse confirmAppointment(@Argument String id) {
		return AppointmentGraphQlResponse.from(confirmAppointmentUseCase.confirm(UUID.fromString(id)));
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
	@MutationMapping
	public AppointmentGraphQlResponse cancelAppointment(@Argument String id) {
		return AppointmentGraphQlResponse.from(cancelAppointmentUseCase.cancel(UUID.fromString(id)));
	}
}

record ScheduleAppointmentGraphQlInput(
		String patientId,
		String professionalId,
		String professionalName,
		String scheduledAt,
		String status,
		String notes) {

	ScheduleAppointmentCommand toCommand() {
		return new ScheduleAppointmentCommand(
				UUID.fromString(patientId),
				UUID.fromString(professionalId),
				professionalName,
				parseScheduledAt(scheduledAt),
				parseStatus(status, AppointmentStatus.SCHEDULED),
				notes);
	}

	static Instant parseScheduledAt(String value) {
		try {
			return Instant.parse(value);
		}
		catch (DateTimeParseException exception) {
			return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
		}
	}

	static AppointmentStatus parseStatus(String value, AppointmentStatus defaultStatus) {
		if (value == null || value.isBlank()) {
			return defaultStatus;
		}
		if ("CANCELLED".equals(value)) {
			return AppointmentStatus.CANCELED;
		}

		return AppointmentStatus.valueOf(value);
	}
}

record UpdateAppointmentGraphQlInput(
		String professionalId,
		String professionalName,
		String scheduledAt,
		String status,
		String notes) {

	UpdateAppointmentCommand toCommand() {
		return new UpdateAppointmentCommand(
				professionalId == null ? null : UUID.fromString(professionalId),
				professionalName,
				scheduledAt == null ? null : ScheduleAppointmentGraphQlInput.parseScheduledAt(scheduledAt),
				ScheduleAppointmentGraphQlInput.parseStatus(status, null),
				notes);
	}
}

record PatientHistoryGraphQlResponse(String patientId, List<AppointmentGraphQlResponse> appointments) {
}

record AppointmentGraphQlResponse(
		String id,
		String appointmentId,
		String patientId,
		String professionalId,
		String professionalName,
		String scheduledAt,
		String status,
		String notes,
		String createdAt,
		String updatedAt) {

	static AppointmentGraphQlResponse from(AppointmentView appointment) {
		String appointmentId = appointment.id().toString();
		return new AppointmentGraphQlResponse(
				appointmentId,
				appointmentId,
				appointment.patientId().toString(),
				appointment.professionalId().toString(),
				appointment.professionalName(),
				appointment.scheduledAt().toString(),
				appointment.status().name(),
				appointment.notes(),
				appointment.createdAt().toString(),
				appointment.updatedAt().toString());
	}
}
