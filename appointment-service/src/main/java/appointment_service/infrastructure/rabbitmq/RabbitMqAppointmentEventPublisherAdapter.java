package appointment_service.infrastructure.rabbitmq;

import appointment_service.application.event.AppointmentEvent;
import appointment_service.application.port.out.AppointmentEventPublisherPort;
import appointment_service.domain.Appointment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("docker")
public class RabbitMqAppointmentEventPublisherAdapter implements AppointmentEventPublisherPort {

	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper = new ObjectMapper()
			.findAndRegisterModules()
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	private final String appointmentExchange;

	public RabbitMqAppointmentEventPublisherAdapter(
			RabbitTemplate rabbitTemplate,
			@Value("${app.messaging.appointments-exchange:appointments.exchange}") String appointmentExchange) {
		this.rabbitTemplate = rabbitTemplate;
		this.appointmentExchange = appointmentExchange;
	}

	@Override
	public void appointmentScheduled(Appointment appointment) {
		publish("appointments.created", AppointmentEvent.created(appointment));
	}

	@Override
	public void appointmentUpdated(Appointment appointment) {
		publish("appointments.updated", AppointmentEvent.updated(appointment));
	}

	@Override
	public void appointmentConfirmed(Appointment appointment) {
		appointmentUpdated(appointment);
	}

	@Override
	public void appointmentCanceled(Appointment appointment) {
		appointmentUpdated(appointment);
	}

	private void publish(String routingKey, AppointmentEvent event) {
		rabbitTemplate.convertAndSend(appointmentExchange, routingKey, toJson(event));
	}

	private String toJson(AppointmentEvent event) {
		try {
			return objectMapper.writeValueAsString(event);
		}
		catch (JsonProcessingException exception) {
			throw new IllegalStateException("Could not serialize appointment event", exception);
		}
	}
}
