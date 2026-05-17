package appointment_service.domain;

public class AppointmentConflictException extends RuntimeException {

	public AppointmentConflictException(String message) {
		super(message);
	}
}
