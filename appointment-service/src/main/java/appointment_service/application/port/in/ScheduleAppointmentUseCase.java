package appointment_service.application.port.in;

import appointment_service.application.AppointmentView;
import appointment_service.domain.AppointmentStatus;
import java.time.Instant;
import java.util.UUID;

public interface ScheduleAppointmentUseCase {

	AppointmentView schedule(ScheduleAppointmentCommand command);

	record ScheduleAppointmentCommand(
			UUID patientId,
			UUID professionalId,
			String professionalName,
			Instant scheduledAt,
			AppointmentStatus status,
			String notes) {
	}
}
