package appointment_service.application.port.in;

import appointment_service.application.AppointmentView;
import java.util.List;
import java.util.UUID;

public interface ListPatientAppointmentsUseCase {

	List<AppointmentView> listByPatient(UUID patientId);

	List<AppointmentView> listUpcomingByPatient(UUID patientId);
}
