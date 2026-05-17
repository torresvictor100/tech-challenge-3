package appointment_service.application.port.out;

import appointment_service.domain.Appointment;

public interface AppointmentEventPublisherPort {

	void appointmentScheduled(Appointment appointment);

	void appointmentUpdated(Appointment appointment);

	void appointmentConfirmed(Appointment appointment);

	void appointmentCanceled(Appointment appointment);
}
