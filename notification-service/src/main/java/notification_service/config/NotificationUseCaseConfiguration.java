package notification_service.config;

import notification_service.application.port.out.NotificationRepositoryPort;
import notification_service.application.port.out.NotificationSenderPort;
import notification_service.application.service.NotificationUseCaseService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class NotificationUseCaseConfiguration {

	@Bean
	NotificationUseCaseService notificationUseCaseService(
			NotificationRepositoryPort notificationRepository,
			NotificationSenderPort notificationSender) {
		return new NotificationUseCaseService(notificationRepository, notificationSender);
	}
}
