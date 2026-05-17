package notification_service.api.rabbitmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import notification_service.application.ReminderService;
import notification_service.application.event.AppointmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("docker")
public class AppointmentNotificationRabbitMqAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentNotificationRabbitMqAdapter.class);

	private final ReminderService reminderService;
	private final ObjectMapper objectMapper = new ObjectMapper()
			.findAndRegisterModules()
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

	public AppointmentNotificationRabbitMqAdapter(ReminderService reminderService) {
		this.reminderService = reminderService;
	}

	@RabbitListener(queues = "${app.messaging.appointments-queue:appointments.queue}")
	public void consume(String payload) {
		AppointmentEvent event = readEvent(payload);
		LOGGER.info("appointment-event-consumed eventId={} type={} appointmentId={}",
				event.eventId(), event.eventType(), event.appointmentId());
		reminderService.process(event);
	}

	private AppointmentEvent readEvent(String payload) {
		try {
			return objectMapper.readValue(payload, AppointmentEvent.class);
		}
		catch (JsonProcessingException exception) {
			throw new IllegalArgumentException("Invalid appointment event payload", exception);
		}
	}
}
