package appointment_service.api.rest;

import appointment_service.api.dto.ApiErrorResponse;
import appointment_service.application.AppointmentNotFoundException;
import appointment_service.domain.AppointmentConflictException;
import appointment_service.domain.AppointmentDomainException;
import appointment_service.domain.BusinessRuleException;
import appointment_service.domain.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
class AppointmentRestExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(AppointmentRestExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		List<ApiErrorResponse.FieldErrorResponse> fields = exception.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new ApiErrorResponse.FieldErrorResponse(
						error.getField(),
						error.getDefaultMessage(),
						error.getRejectedValue()))
				.toList();

		return build(
				HttpStatus.BAD_REQUEST,
				"VALIDATION_ERROR",
				"Existem campos inválidos no payload.",
				request,
				fields);
	}

	@ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
	ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
		return build(
				HttpStatus.BAD_REQUEST,
				"BAD_REQUEST",
				"Payload ou parâmetro inválido.",
				request,
				List.of());
	}

	@ExceptionHandler(AppointmentNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleAppointmentNotFound(
			AppointmentNotFoundException exception,
			HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, "APPOINTMENT_NOT_FOUND", exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleResourceNotFound(
			ResourceNotFoundException exception,
			HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, exception.code(), exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(AppointmentConflictException.class)
	ResponseEntity<ApiErrorResponse> handleConflict(
			AppointmentConflictException exception,
			HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, "APPOINTMENT_CONFLICT", exception.getMessage(), request, List.of());
	}

	@ExceptionHandler({ BusinessRuleException.class, AppointmentDomainException.class })
	ResponseEntity<ApiErrorResponse> handleBusinessRule(RuntimeException exception, HttpServletRequest request) {
		String code = exception instanceof BusinessRuleException businessRuleException
				? businessRuleException.code()
				: "BUSINESS_RULE_VIOLATION";
		return build(HttpStatus.UNPROCESSABLE_ENTITY, code, exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ApiErrorResponse> handleForbidden(AccessDeniedException exception, HttpServletRequest request) {
		return build(
				HttpStatus.FORBIDDEN,
				"ACCESS_DENIED",
				"Usuário sem permissão para executar esta operação.",
				request,
				List.of());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<ApiErrorResponse> handleIllegalArgument(
			IllegalArgumentException exception,
			HttpServletRequest request) {
		return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request, List.of());
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
		LOGGER.error("unexpected-appointment-api-error path={}", request.getRequestURI(), exception);
		return build(
				HttpStatus.INTERNAL_SERVER_ERROR,
				"INTERNAL_ERROR",
				"Erro interno inesperado.",
				request,
				List.of());
	}

	private ResponseEntity<ApiErrorResponse> build(
			HttpStatus status,
			String code,
			String message,
			HttpServletRequest request,
			List<ApiErrorResponse.FieldErrorResponse> fieldErrors) {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				code,
				message,
				request.getRequestURI(),
				MDC.get("traceId"),
				fieldErrors);

		return ResponseEntity.status(status).body(body);
	}
}
