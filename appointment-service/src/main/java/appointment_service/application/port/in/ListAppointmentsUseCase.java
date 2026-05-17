package appointment_service.application.port.in;

import appointment_service.application.AppointmentView;
import java.util.List;

public interface ListAppointmentsUseCase {

	List<AppointmentView> list();
}
