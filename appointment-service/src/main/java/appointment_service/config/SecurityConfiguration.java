package appointment_service.config;

import appointment_service.api.dto.ApiErrorResponse;
import appointment_service.application.port.out.UserRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/actuator/health", "/actuator/info", "/internal/ping").permitAll()
						.anyRequest().authenticated())
				.httpBasic(Customizer.withDefaults())
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint((request, response, exception) -> writeError(
								objectMapper,
								request,
								response,
								HttpStatus.UNAUTHORIZED,
								"UNAUTHORIZED",
								"Credenciais ausentes ou inválidas."))
						.accessDeniedHandler((request, response, exception) -> writeError(
								objectMapper,
								request,
								response,
								HttpStatus.FORBIDDEN,
								"ACCESS_DENIED",
								"Usuário sem permissão para executar esta operação.")))
				.build();
	}

	@Bean
	UserDetailsService userDetailsService(UserRepositoryPort userRepository) {
		return username -> userRepository.findByEmail(username)
				.map(user -> User.withUsername(user.email())
						.password(user.passwordHash())
						.roles(user.role().name())
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@ConditionalOnMissingBean
	ObjectMapper objectMapper() {
		return new ObjectMapper().findAndRegisterModules();
	}

	private static void writeError(
			ObjectMapper objectMapper,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpStatus status,
			String code,
			String message) throws IOException {
		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				code,
				message,
				request.getRequestURI(),
				MDC.get("traceId"),
				List.of());

		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), body);
	}
}
