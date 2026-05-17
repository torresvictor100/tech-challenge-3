package appointment_service.api.rest;

import appointment_service.api.dto.AppointmentResponse;
import appointment_service.application.port.in.ListPatientAppointmentsUseCase;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/patients", "/api/patients" })
class PatientAppointmentRestController {

	private final ListPatientAppointmentsUseCase listPatientAppointmentsUseCase;

	PatientAppointmentRestController(ListPatientAppointmentsUseCase listPatientAppointmentsUseCase) {
		this.listPatientAppointmentsUseCase = listPatientAppointmentsUseCase;
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO') or @patientAccessEvaluator.canReadPatient(#patientId, authentication.name)")
	@GetMapping("/{patientId}/appointments")
	List<AppointmentResponse> listByPatient(@PathVariable UUID patientId) {
		return listPatientAppointmentsUseCase.listByPatient(patientId)
				.stream()
				.map(AppointmentResponse::from)
				.toList();
	}

	@PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO') or @patientAccessEvaluator.canReadPatient(#patientId, authentication.name)")
	@GetMapping("/{patientId}/appointments/upcoming")
	List<AppointmentResponse> listUpcomingByPatient(@PathVariable UUID patientId) {
		return listPatientAppointmentsUseCase.listUpcomingByPatient(patientId)
				.stream()
				.map(AppointmentResponse::from)
				.toList();
	}
}
