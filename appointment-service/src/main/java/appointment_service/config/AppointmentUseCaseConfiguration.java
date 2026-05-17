package appointment_service.config;

import appointment_service.application.port.out.AppointmentEventPublisherPort;
import appointment_service.application.port.out.AppointmentRepositoryPort;
import appointment_service.application.port.out.UserRepositoryPort;
import appointment_service.application.service.AppointmentUseCaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AppointmentUseCaseConfiguration {

	@Bean
	AppointmentUseCaseService appointmentUseCaseService(
			AppointmentRepositoryPort appointmentRepository,
			AppointmentEventPublisherPort eventPublisher,
			UserRepositoryPort userRepository) {
		return new AppointmentUseCaseService(appointmentRepository, eventPublisher, userRepository);
	}
}
