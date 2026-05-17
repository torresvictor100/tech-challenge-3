package notification_service.application;

import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReminderRecipientResolver {

	private static final Map<UUID, String> SEEDED_PATIENT_EMAILS = Map.of(
			UUID.fromString("00000000-0000-0000-0000-000000000601"),
			"paciente@hospital.com");

	private final String fallbackRecipient;

	public ReminderRecipientResolver(
			@Value("${app.notifications.fallback-recipient:paciente@hospital.com}") String fallbackRecipient) {
		this.fallbackRecipient = normalize(fallbackRecipient);
	}

	public String resolve(UUID patientId) {
		String recipient = SEEDED_PATIENT_EMAILS.get(patientId);
		if (recipient != null) {
			return recipient;
		}

		return fallbackRecipient;
	}

	private static String normalize(String value) {
		if (value == null || value.isBlank()) {
			return "paciente@hospital.com";
		}

		return value.trim();
	}
}
