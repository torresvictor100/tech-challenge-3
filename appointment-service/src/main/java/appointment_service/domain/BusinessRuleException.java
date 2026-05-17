package appointment_service.domain;

public class BusinessRuleException extends RuntimeException {

	private final String code;

	public BusinessRuleException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String code() {
		return code;
	}
}
