package notification_service.api.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableRabbit
@Profile("docker")
class RabbitMqNotificationConfiguration {

	@Bean
	Queue appointmentQueue(@Value("${app.messaging.appointments-queue:appointments.queue}") String queueName) {
		return new Queue(queueName, true);
	}

	@Bean
	TopicExchange appointmentExchange(@Value("${app.messaging.appointments-exchange:appointments.exchange}") String exchangeName) {
		return new TopicExchange(exchangeName, true, false);
	}

	@Bean
	Binding appointmentBinding(Queue appointmentQueue, TopicExchange appointmentExchange) {
		return BindingBuilder.bind(appointmentQueue).to(appointmentExchange).with("appointments.*");
	}
}
