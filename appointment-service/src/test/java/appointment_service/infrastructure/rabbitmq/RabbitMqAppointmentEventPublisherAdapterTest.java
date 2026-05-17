package appointment_service.infrastructure.rabbitmq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appointment_service.domain.Appointment;
import appointment_service.domain.AppointmentStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

class RabbitMqAppointmentEventPublisherAdapterTest {

	@Test
	void publishesCreatedEventWithIsoInstantPayload() {
		CapturingRabbitTemplate rabbitTemplate = new CapturingRabbitTemplate();
		RabbitMqAppointmentEventPublisherAdapter adapter = new RabbitMqAppointmentEventPublisherAdapter(
				rabbitTemplate,
				"appointments.exchange");
		Appointment appointment = Appointment.restore(
				UUID.fromString("00000000-0000-0000-0000-000000000901"),
				UUID.fromString("00000000-0000-0000-0000-000000000601"),
				UUID.fromString("00000000-0000-0000-0000-000000000701"),
				"Dra. Marina Costa",
				Instant.parse("2030-01-10T10:00:00Z"),
				AppointmentStatus.SCHEDULED,
				"Primeira consulta",
				Instant.parse("2026-04-14T10:00:00Z"),
				Instant.parse("2026-04-14T10:00:00Z"));

		adapter.appointmentScheduled(appointment);

		assertEquals("appointments.exchange", rabbitTemplate.exchange);
		assertEquals("appointments.created", rabbitTemplate.routingKey);
		assertTrue(rabbitTemplate.payload.contains("\"eventType\":\"APPOINTMENT_CREATED\""));
		assertTrue(rabbitTemplate.payload.contains("\"scheduledAt\":\"2030-01-10T10:00:00Z\""));
	}

	private static final class CapturingRabbitTemplate extends RabbitTemplate {

		private String exchange;
		private String routingKey;
		private String payload;

		@Override
		public void convertAndSend(String exchange, String routingKey, Object object) throws AmqpException {
			this.exchange = exchange;
			this.routingKey = routingKey;
			this.payload = (String) object;
		}
	}
}
