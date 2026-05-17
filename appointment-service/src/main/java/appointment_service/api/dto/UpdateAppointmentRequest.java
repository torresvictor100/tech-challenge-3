package appointment_service.api.dto;

import appointment_service.application.port.in.UpdateAppointmentUseCase.UpdateAppointmentCommand;
import appointment_service.domain.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record UpdateAppointmentRequest(
		UUID professionalId,

		@Size(max = 120, message = "professionalName deve ter no máximo 120 caracteres")
		String professionalName,

		@Future(message = "scheduledAt deve ser uma data futura")
		Instant scheduledAt,

		@Pattern(regexp = "SCHEDULED|CONFIRMED|CANCELED|CANCELLED",
				message = "status deve ser SCHEDULED, CONFIRMED, CANCELED ou CANCELLED")
		String status,

		@Size(max = 500, message = "notes deve ter no máximo 500 caracteres")
		String notes) {

	public UpdateAppointmentCommand toCommand() {
		return new UpdateAppointmentCommand(
				professionalId,
				professionalName,
				scheduledAt,
				parseStatus(status),
				notes);
	}

	private static AppointmentStatus parseStatus(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		if ("CANCELLED".equals(value)) {
			return AppointmentStatus.CANCELED;
		}

		return AppointmentStatus.valueOf(value);
	}
}
