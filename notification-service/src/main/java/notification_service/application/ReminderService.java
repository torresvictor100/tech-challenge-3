package notification_service.application;

import notification_service.application.event.AppointmentEvent;
import notification_service.application.port.in.CreateNotificationUseCase;
import notification_service.application.port.in.CreateNotificationUseCase.CreateNotificationCommand;
import notification_service.application.port.in.SendNotificationUseCase;
import notification_service.domain.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReminderService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReminderService.class);

	private final CreateNotificationUseCase createNotificationUseCase;
	private final SendNotificationUseCase sendNotificationUseCase;
	private final ReminderRecipientResolver recipientResolver;

	public ReminderService(
			CreateNotificationUseCase createNotificationUseCase,
			SendNotificationUseCase sendNotificationUseCase,
			ReminderRecipientResolver recipientResolver) {
		this.createNotificationUseCase = createNotificationUseCase;
		this.sendNotificationUseCase = sendNotificationUseCase;
		this.recipientResolver = recipientResolver;
	}

	public void process(AppointmentEvent event) {
		String recipient = recipientResolver.resolve(event.patientId());
		NotificationView createdNotification = createNotificationUseCase.create(new CreateNotificationCommand(
				event.appointmentId(),
				recipient,
				subject(event),
				message(event),
				NotificationChannel.EMAIL));
		NotificationView sentNotification = sendNotificationUseCase.send(createdNotification.id());

		LOGGER.info(
				"patient-reminder-processed patientId={} appointmentId={} scheduledAt={} status={} notificationId={} notificationStatus={} recipient={}",
				event.patientId(),
				event.appointmentId(),
				event.scheduledAt(),
				event.status(),
				sentNotification.id(),
				sentNotification.status(),
				recipient);
	}

	private static String subject(AppointmentEvent event) {
		if ("CANCELED".equals(event.status())) {
			return "Consulta cancelada";
		}
		return "Lembrete de consulta";
	}

	private static String message(AppointmentEvent event) {
		if ("CANCELED".equals(event.status())) {
			return "Sua consulta de " + event.scheduledAt() + " foi cancelada.";
		}
		return "Lembrete: voce possui consulta agendada para " + event.scheduledAt()
				+ ". Status atual: " + event.status() + ".";
	}
}
