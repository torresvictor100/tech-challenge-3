package notification_service.api.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import notification_service.application.NotificationView;
import notification_service.application.port.in.CreateNotificationUseCase;
import notification_service.application.port.in.CreateNotificationUseCase.CreateNotificationCommand;
import notification_service.application.port.in.GetNotificationUseCase;
import notification_service.application.port.in.ListNotificationsUseCase;
import notification_service.application.port.in.SendNotificationUseCase;
import notification_service.domain.NotificationChannel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationRestController {

	private final CreateNotificationUseCase createNotificationUseCase;
	private final GetNotificationUseCase getNotificationUseCase;
	private final ListNotificationsUseCase listNotificationsUseCase;
	private final SendNotificationUseCase sendNotificationUseCase;

	public NotificationRestController(
			CreateNotificationUseCase createNotificationUseCase,
			GetNotificationUseCase getNotificationUseCase,
			ListNotificationsUseCase listNotificationsUseCase,
			SendNotificationUseCase sendNotificationUseCase) {
		this.createNotificationUseCase = createNotificationUseCase;
		this.getNotificationUseCase = getNotificationUseCase;
		this.listNotificationsUseCase = listNotificationsUseCase;
		this.sendNotificationUseCase = sendNotificationUseCase;
	}

	@PostMapping
	public ResponseEntity<NotificationRestResponse> create(@RequestBody CreateNotificationRestRequest request) {
		NotificationView notification = createNotificationUseCase.create(request.toCommand());

		return ResponseEntity
				.created(URI.create("/notifications/" + notification.id()))
				.body(NotificationRestResponse.from(notification));
	}

	@GetMapping("/{notificationId}")
	public NotificationRestResponse getById(@PathVariable UUID notificationId) {
		return NotificationRestResponse.from(getNotificationUseCase.getById(notificationId));
	}

	@GetMapping
	public List<NotificationRestResponse> list() {
		return listNotificationsUseCase.list()
				.stream()
				.map(NotificationRestResponse::from)
				.toList();
	}

	@PostMapping("/{notificationId}/send")
	public NotificationRestResponse send(@PathVariable UUID notificationId) {
		return NotificationRestResponse.from(sendNotificationUseCase.send(notificationId));
	}
}

record CreateNotificationRestRequest(
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

record NotificationRestResponse(
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

	static NotificationRestResponse from(NotificationView notification) {
		return new NotificationRestResponse(
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

record NotificationRestError(String message) {
}
