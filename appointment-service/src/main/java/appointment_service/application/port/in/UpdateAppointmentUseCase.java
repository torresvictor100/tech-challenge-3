package appointment_service.application.port.in;

import appointment_service.application.AppointmentView;
import appointment_service.domain.AppointmentStatus;
import java.time.Instant;
import java.util.UUID;

public interface UpdateAppointmentUseCase {

	AppointmentView update(UUID appointmentId, UpdateAppointmentCommand command);

	record UpdateAppointmentCommand(
			UUID professionalId,
			String professionalName,
			Instant scheduledAt,
			AppointmentStatus status,
			String notes) {
	}
}
