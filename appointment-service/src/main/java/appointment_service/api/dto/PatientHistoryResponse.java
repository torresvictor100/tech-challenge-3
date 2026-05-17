package appointment_service.api.dto;

import appointment_service.application.AppointmentView;
import java.util.List;
import java.util.UUID;

public record PatientHistoryResponse(
		UUID patientId,
		List<AppointmentResponse> appointments) {

	public static PatientHistoryResponse from(UUID patientId, List<AppointmentView> appointments) {
		return new PatientHistoryResponse(
				patientId,
				appointments.stream().map(AppointmentResponse::from).toList());
	}
}
