package appointment_service.api.rest;

import appointment_service.api.dto.AppointmentResponse;
import appointment_service.api.dto.CreateAppointmentRequest;
import appointment_service.api.dto.UpdateAppointmentRequest;
import appointment_service.application.AppointmentView;
import appointment_service.application.port.in.CancelAppointmentUseCase;
import appointment_service.application.port.in.ConfirmAppointmentUseCase;
import appointment_service.application.port.in.GetAppointmentUseCase;
import appointment_service.application.port.in.ListAppointmentsUseCase;
import appointment_service.application.port.in.ListPatientAppointmentsUseCase;
import appointment_service.application.port.in.ScheduleAppointmentUseCase;
import appointment_service.application.port.in.UpdateAppointmentUseCase;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/appointments", "/api/appointments" })
public class AppointmentRestController {

	private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;
	private final UpdateAppointmentUseCase updateAppointmentUseCase;
	private final GetAppointmentUseCase getAppointmentUseCase;
	private final ListAppointmentsUseCase listAppointmentsUseCase;
	private final ListPatientAppointmentsUseCase listPatientAppointmentsUseCase;
	private final ConfirmAppointmentUseCase confirmAppointmentUseCase;
	private final CancelAppointmentUseCase cancelAppointmentUseCase;

	public AppointmentRestController(
			ScheduleAppointmentUseCase scheduleAppointmentUseCase,
			UpdateAppointmentUseCase updateAppointmentUseCase,
			GetAppointmentUseCase getAppointmentUseCase,
			ListAppointmentsUseCase listAppointmentsUseCase,
			ListPatientAppointmentsUseCase listPatientAppointmentsUseCase,
			ConfirmAppointmentUseCase confirmAppointmentUseCase,
			CancelAppointmentUseCase cancelAppointmentUseCase) {
		this.scheduleAppointmentUseCase = scheduleAppointmentUseCase;
		this.updateAppointmentUseCase = updateAppointmentUseCase;
		this.getAppointmentUseCase = getAppointmentUseCase;
		this.listAppointmentsUseCase = listAppointmentsUseCase;
		this.listPatientAppointmentsUseCase = listPatientAppointmentsUseCase;
		this.confirmAppointmentUseCase = confirmAppointmentUseCase;
		this.cancelAppointmentUseCase = cancelAppointmentUseCase;
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
	@PostMapping
	public ResponseEntity<AppointmentResponse> schedule(@Valid @RequestBody CreateAppointmentRequest request) {
		AppointmentView appointment = scheduleAppointmentUseCase.schedule(request.toCommand());

		return ResponseEntity
				.created(URI.create("/api/appointments/" + appointment.id()))
				.body(AppointmentResponse.from(appointment));
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
	@PutMapping("/{appointmentId}")
	public ResponseEntity<AppointmentResponse> update(
			@PathVariable UUID appointmentId,
			@Valid @RequestBody UpdateAppointmentRequest request) {
		return ResponseEntity.ok(AppointmentResponse.from(updateAppointmentUseCase.update(appointmentId, request.toCommand())));
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO') or @patientAccessEvaluator.canReadAppointment(#appointmentId, authentication.name)")
	@GetMapping("/{appointmentId}")
	public AppointmentResponse getById(@PathVariable UUID appointmentId) {
		return AppointmentResponse.from(getAppointmentUseCase.getById(appointmentId));
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
	@GetMapping
	public List<AppointmentResponse> list() {
		return listAppointmentsUseCase.list()
				.stream()
				.map(AppointmentResponse::from)
				.toList();
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO') or @patientAccessEvaluator.canReadPatient(#patientId, authentication.name)")
	@GetMapping("/patients/{patientId}")
	public List<AppointmentResponse> listByPatient(@PathVariable UUID patientId) {
		return listPatientAppointmentsUseCase.listByPatient(patientId)
				.stream()
				.map(AppointmentResponse::from)
				.toList();
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO') or @patientAccessEvaluator.canReadPatient(#patientId, authentication.name)")
	@GetMapping("/patients/{patientId}/upcoming")
	public List<AppointmentResponse> listUpcomingByPatient(@PathVariable UUID patientId) {
		return listPatientAppointmentsUseCase.listUpcomingByPatient(patientId)
				.stream()
				.map(AppointmentResponse::from)
				.toList();
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
	@PostMapping("/{appointmentId}/confirm")
	public AppointmentResponse confirm(@PathVariable UUID appointmentId) {
		return AppointmentResponse.from(confirmAppointmentUseCase.confirm(appointmentId));
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO')")
	@PostMapping("/{appointmentId}/cancel")
	public AppointmentResponse cancel(@PathVariable UUID appointmentId) {
		return AppointmentResponse.from(cancelAppointmentUseCase.cancel(appointmentId));
	}
}
