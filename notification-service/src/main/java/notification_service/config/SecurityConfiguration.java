package notification_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfiguration {

	private static final String SEEDED_PASSWORD_HASH =
			"$2b$10$.yKZpmk2LWU.BXWr9m8AyeY7iXn1rVMrytPbYiGxTCGWcE7vW4Ovy";

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/actuator/health", "/actuator/info", "/internal/ping").permitAll()
						.requestMatchers("/notifications/**", "/graphql").hasAnyRole("MEDICO", "ENFERMEIRO")
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
	UserDetailsService userDetailsService() {
		return new InMemoryUserDetailsManager(
				User.withUsername("medico@hospital.com")
						.password(SEEDED_PASSWORD_HASH)
						.roles("MEDICO")
						.build(),
				User.withUsername("doctor@hospital.com")
						.password(SEEDED_PASSWORD_HASH)
						.roles("MEDICO")
						.build(),
				User.withUsername("enfermeiro@hospital.com")
						.password(SEEDED_PASSWORD_HASH)
						.roles("ENFERMEIRO")
						.build(),
				User.withUsername("nurse@hospital.com")
						.password(SEEDED_PASSWORD_HASH)
						.roles("ENFERMEIRO")
						.build(),
				User.withUsername("paciente@hospital.com")
						.password(SEEDED_PASSWORD_HASH)
						.roles("PACIENTE")
						.build(),
				User.withUsername("patient@hospital.com")
						.password(SEEDED_PASSWORD_HASH)
						.roles("PACIENTE")
						.build());
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
		NotificationSecurityError body = new NotificationSecurityError(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				code,
				message,
				request.getRequestURI());

		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), body);
	}

	private record NotificationSecurityError(
			Instant timestamp,
			int status,
			String error,
			String code,
			String message,
			String path) {
	}
}
