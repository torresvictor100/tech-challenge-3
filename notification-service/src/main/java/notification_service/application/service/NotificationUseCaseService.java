package notification_service.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import notification_service.application.NotificationNotFoundException;
import notification_service.application.NotificationView;
import notification_service.application.port.in.CreateNotificationUseCase;
import notification_service.application.port.in.GetNotificationUseCase;
import notification_service.application.port.in.ListNotificationsUseCase;
import notification_service.application.port.in.SendNotificationUseCase;
import notification_service.application.port.out.NotificationRepositoryPort;
import notification_service.application.port.out.NotificationSenderPort;
import notification_service.application.port.out.NotificationSenderPort.DeliveryResult;
import notification_service.domain.Notification;
import notification_service.domain.NotificationDomainException;
import notification_service.domain.NotificationStatus;

public class NotificationUseCaseService implements
		CreateNotificationUseCase,
		GetNotificationUseCase,
		ListNotificationsUseCase,
		SendNotificationUseCase {

	private final NotificationRepositoryPort notificationRepository;
	private final NotificationSenderPort notificationSender;

	public NotificationUseCaseService(
			NotificationRepositoryPort notificationRepository,
			NotificationSenderPort notificationSender) {
		this.notificationRepository = notificationRepository;
		this.notificationSender = notificationSender;
	}

	@Override
	public NotificationView create(CreateNotificationCommand command) {
		Notification notification = Notification.create(
				command.appointmentId(),
				command.recipient(),
				command.subject(),
				command.message(),
				command.channel());

		return NotificationView.from(notificationRepository.save(notification));
	}

	@Override
	public NotificationView getById(UUID notificationId) {
		return NotificationView.from(findNotification(notificationId));
	}

	@Override
	public List<NotificationView> list() {
		return notificationRepository.findAll()
				.stream()
				.sorted(Comparator.comparing(Notification::getCreatedAt))
				.map(NotificationView::from)
				.toList();
	}

	@Override
	public NotificationView send(UUID notificationId) {
		Notification notification = findNotification(notificationId);
		if (notification.getStatus() == NotificationStatus.SENT) {
			throw new NotificationDomainException("Notification was already sent");
		}

		DeliveryResult deliveryResult = notificationSender.send(notification);
		if (deliveryResult.delivered()) {
			notification.markSent();
		}
		else {
			notification.markFailed(deliveryResult.failureReason());
		}

		return NotificationView.from(notificationRepository.save(notification));
	}

	private Notification findNotification(UUID notificationId) {
		return notificationRepository.findById(notificationId)
				.orElseThrow(() -> new NotificationNotFoundException(notificationId));
	}
}
