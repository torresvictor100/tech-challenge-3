package appointment_service.api.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String code,
		String message,
		String path,
		String traceId,
		List<FieldErrorResponse> fieldErrors) {

	public record FieldErrorResponse(
			String field,
			String message,
			Object rejectedValue) {
	}
}
