package appointment_service.api.dto;

import appointment_service.application.port.in.ScheduleAppointmentUseCase.ScheduleAppointmentCommand;
import appointment_service.domain.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreateAppointmentRequest(
		@NotNull(message = "patientId é obrigatório")
		UUID patientId,

		@NotNull(message = "professionalId é obrigatório")
		UUID professionalId,

		@Size(max = 120, message = "professionalName deve ter no máximo 120 caracteres")
		String professionalName,

		@NotNull(message = "scheduledAt é obrigatório")
		@Future(message = "scheduledAt deve ser uma data futura")
		Instant scheduledAt,

		@NotBlank(message = "status é obrigatório")
		@Pattern(regexp = "SCHEDULED|CONFIRMED|CANCELED|CANCELLED",
				message = "status deve ser SCHEDULED, CONFIRMED, CANCELED ou CANCELLED")
		String status,

		@Size(max = 500, message = "notes deve ter no máximo 500 caracteres")
		String notes) {

	public ScheduleAppointmentCommand toCommand() {
		return new ScheduleAppointmentCommand(
				patientId,
				professionalId,
				professionalName,
				scheduledAt,
				parseStatus(status),
				notes);
	}

	private static AppointmentStatus parseStatus(String value) {
		if (value == null || value.isBlank()) {
			return AppointmentStatus.SCHEDULED;
		}
		if ("CANCELLED".equals(value)) {
			return AppointmentStatus.CANCELED;
		}

		return AppointmentStatus.valueOf(value);
	}
}
