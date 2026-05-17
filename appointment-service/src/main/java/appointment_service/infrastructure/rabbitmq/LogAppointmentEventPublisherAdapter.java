package appointment_service.infrastructure.rabbitmq;

import appointment_service.application.event.AppointmentEvent;
import appointment_service.application.port.out.AppointmentEventPublisherPort;
import appointment_service.domain.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!docker")
public class LogAppointmentEventPublisherAdapter implements AppointmentEventPublisherPort {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogAppointmentEventPublisherAdapter.class);

	@Override
	public void appointmentScheduled(Appointment appointment) {
		AppointmentEvent event = AppointmentEvent.created(appointment);
		LOGGER.info("appointment-event-published eventId={} type={} appointmentId={}",
				event.eventId(), event.eventType(), event.appointmentId());
	}

	@Override
	public void appointmentUpdated(Appointment appointment) {
		AppointmentEvent event = AppointmentEvent.updated(appointment);
		LOGGER.info("appointment-event-published eventId={} type={} appointmentId={}",
				event.eventId(), event.eventType(), event.appointmentId());
	}

	@Override
	public void appointmentConfirmed(Appointment appointment) {
		appointmentUpdated(appointment);
	}

	@Override
	public void appointmentCanceled(Appointment appointment) {
		appointmentUpdated(appointment);
	}
}
