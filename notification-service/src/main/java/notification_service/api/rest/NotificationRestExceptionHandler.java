package notification_service.api.rest;

import notification_service.application.NotificationNotFoundException;
import notification_service.domain.NotificationDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = NotificationRestController.class)
class NotificationRestExceptionHandler {

	@ExceptionHandler(NotificationNotFoundException.class)
	ResponseEntity<NotificationRestError> handleNotFound(NotificationNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new NotificationRestError(exception.getMessage()));
	}

	@ExceptionHandler({ NotificationDomainException.class, IllegalArgumentException.class })
	ResponseEntity<NotificationRestError> handleBadRequest(RuntimeException exception) {
		return ResponseEntity.badRequest()
				.body(new NotificationRestError(exception.getMessage()));
	}
}
