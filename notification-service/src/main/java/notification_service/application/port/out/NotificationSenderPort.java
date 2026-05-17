package notification_service.application.port.out;

import notification_service.domain.Notification;

public interface NotificationSenderPort {

	DeliveryResult send(Notification notification);

	record DeliveryResult(boolean delivered, String failureReason) {

		public static DeliveryResult sent() {
			return new DeliveryResult(true, null);
		}

		public static DeliveryResult notSent(String reason) {
			return new DeliveryResult(false, reason);
		}
	}
}
