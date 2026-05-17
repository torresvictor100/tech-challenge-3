package notification_service.api.graphql;

import java.util.List;
import java.util.UUID;
import notification_service.application.NotificationView;
import notification_service.application.port.in.CreateNotificationUseCase;
import notification_service.application.port.in.CreateNotificationUseCase.CreateNotificationCommand;
import notification_service.application.port.in.GetNotificationUseCase;
import notification_service.application.port.in.ListNotificationsUseCase;
import notification_service.application.port.in.SendNotificationUseCase;
import notification_service.domain.NotificationChannel;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class NotificationGraphQlController {

	private final CreateNotificationUseCase createNotificationUseCase;
	private final GetNotificationUseCase getNotificationUseCase;
	private final ListNotificationsUseCase listNotificationsUseCase;
	private final SendNotificationUseCase sendNotificationUseCase;

	public NotificationGraphQlController(
			CreateNotificationUseCase createNotificationUseCase,
			GetNotificationUseCase getNotificationUseCase,
			ListNotificationsUseCase listNotificationsUseCase,
			SendNotificationUseCase sendNotificationUseCase) {
		this.createNotificationUseCase = createNotificationUseCase;
		this.getNotificationUseCase = getNotificationUseCase;
		this.listNotificationsUseCase = listNotificationsUseCase;
		this.sendNotificationUseCase = sendNotificationUseCase;
	}

	@QueryMapping
	public List<NotificationGraphQlResponse> notifications() {
		return listNotificationsUseCase.list()
				.stream()
				.map(NotificationGraphQlResponse::from)
				.toList();
	}

	@QueryMapping
	public NotificationGraphQlResponse notificationById(@Argument String id) {
		return NotificationGraphQlResponse.from(getNotificationUseCase.getById(UUID.fromString(id)));
	}

	@MutationMapping
	public NotificationGraphQlResponse createNotification(@Argument CreateNotificationGraphQlInput input) {
		return NotificationGraphQlResponse.from(createNotificationUseCase.create(input.toCommand()));
	}

	@MutationMapping
	public NotificationGraphQlResponse sendNotification(@Argument String id) {
		return NotificationGraphQlResponse.from(sendNotificationUseCase.send(UUID.fromString(id)));
	}
}

record CreateNotificationGraphQlInput(
		String appointmentId,
		String recipient,
		String subject,
		String message,
		String channel) {

	CreateNotificationCommand toCommand() {
		return new CreateNotificationCommand(
				UUID.fromString(appointmentId),
				recipient,
				subject,
				message,
				NotificationChannel.valueOf(channel));
	}
}

record NotificationGraphQlResponse(
		String id,
		String appointmentId,
		String recipient,
		String subject,
		String message,
		String channel,
		String status,
		String createdAt,
		String sentAt,
		String failureReason) {

	static NotificationGraphQlResponse from(NotificationView notification) {
		return new NotificationGraphQlResponse(
				notification.id().toString(),
				notification.appointmentId().toString(),
				notification.recipient(),
				notification.subject(),
				notification.message(),
				notification.channel().name(),
				notification.status().name(),
				notification.createdAt().toString(),
				notification.sentAt() == null ? null : notification.sentAt().toString(),
				notification.failureReason());
	}
}
