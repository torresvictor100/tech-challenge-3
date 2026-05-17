package appointment_service.application.port.in;

import appointment_service.application.AppointmentView;
import java.util.UUID;

public interface ConfirmAppointmentUseCase {

	AppointmentView confirm(UUID appointmentId);
}
