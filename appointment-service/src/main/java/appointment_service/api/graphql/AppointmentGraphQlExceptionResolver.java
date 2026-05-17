package appointment_service.api.graphql;

import appointment_service.application.AppointmentNotFoundException;
import appointment_service.domain.AppointmentConflictException;
import appointment_service.domain.AppointmentDomainException;
import appointment_service.domain.BusinessRuleException;
import appointment_service.domain.ResourceNotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.concurrent.CompletionException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
class AppointmentGraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

	@Override
	protected GraphQLError resolveToSingleError(Throwable exception, DataFetchingEnvironment environment) {
		Throwable cause = unwrap(exception);

		if (cause instanceof AppointmentNotFoundException) {
			return buildError(environment, ErrorType.NOT_FOUND, "APPOINTMENT_NOT_FOUND", cause.getMessage());
		}
		if (cause instanceof ResourceNotFoundException resourceNotFoundException) {
			return buildError(
					environment,
					ErrorType.NOT_FOUND,
					resourceNotFoundException.code(),
					resourceNotFoundException.getMessage());
		}
		if (cause instanceof AppointmentConflictException) {
			return buildError(environment, ErrorType.BAD_REQUEST, "APPOINTMENT_CONFLICT", cause.getMessage());
		}
		if (cause instanceof BusinessRuleException businessRuleException) {
			return buildError(
					environment,
					ErrorType.BAD_REQUEST,
					businessRuleException.code(),
					businessRuleException.getMessage());
		}
		if (cause instanceof AppointmentDomainException) {
			return buildError(environment, ErrorType.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", cause.getMessage());
		}
		if (cause instanceof DateTimeParseException) {
			return buildError(
					environment,
					ErrorType.BAD_REQUEST,
					"BAD_REQUEST",
					"Data/hora inválida. Use ISO-8601, por exemplo 2031-04-14T23:09:00Z.");
		}
		if (cause instanceof IllegalArgumentException) {
			return buildError(environment, ErrorType.BAD_REQUEST, "BAD_REQUEST", cause.getMessage());
		}
		if (cause instanceof AccessDeniedException) {
			return buildError(
					environment,
					ErrorType.FORBIDDEN,
					"ACCESS_DENIED",
					"Usuário sem permissão para executar esta operação.");
		}

		return null;
	}

	private static Throwable unwrap(Throwable exception) {
		Throwable current = exception;
		while ((current instanceof CompletionException || current instanceof InvocationTargetException)
				&& current.getCause() != null) {
			current = current.getCause();
		}
		return current;
	}

	private static GraphQLError buildError(
			DataFetchingEnvironment environment,
			ErrorType errorType,
			String code,
			String message) {
		return GraphqlErrorBuilder.newError(environment)
				.message(message)
				.errorType(errorType)
				.extensions(Map.of("code", code))
				.build();
	}
}
