package appointment_service.api.graphql;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import appointment_service.infrastructure.memory.InMemoryUserRepositoryAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentGraphQlControllerTest {

	private static final String SCHEDULE_APPOINTMENT = """
			mutation ScheduleAppointment($input: ScheduleAppointmentInput!) {
			  scheduleAppointment(input: $input) {
			    appointmentId
			    status
			  }
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void scheduleAppointmentConflictReturnsGraphQlErrorCode() throws Exception {
		String scheduledAt = Instant.now()
				.plus(Duration.ofDays(365))
				.plusSeconds(ThreadLocalRandom.current().nextInt(1_000, 10_000))
				.truncatedTo(ChronoUnit.SECONDS)
				.toString();
		String request = scheduleAppointmentRequest(scheduledAt);

		mockMvc.perform(post("/graphql")
				.with(httpBasic("medico@hospital.com", "123456"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors").doesNotExist())
				.andExpect(jsonPath("$.data.scheduleAppointment.status").value("SCHEDULED"));

		mockMvc.perform(post("/graphql")
				.with(httpBasic("medico@hospital.com", "123456"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.errors[0].message").value("Professional already has an appointment at this time."))
				.andExpect(jsonPath("$.errors[0].extensions.code").value("APPOINTMENT_CONFLICT"))
				.andExpect(jsonPath("$.errors[0].extensions.classification").value("BAD_REQUEST"));
	}

	private String scheduleAppointmentRequest(String scheduledAt) throws Exception {
		return objectMapper.writeValueAsString(Map.of(
				"query",
				SCHEDULE_APPOINTMENT,
				"variables",
				Map.of("input", Map.of(
						"patientId",
						InMemoryUserRepositoryAdapter.SAMPLE_PATIENT_ID.toString(),
						"professionalId",
						InMemoryUserRepositoryAdapter.SAMPLE_DOCTOR_ID.toString(),
						"professionalName",
						"Dra. Marina Costa",
						"scheduledAt",
						scheduledAt,
						"status",
						"SCHEDULED",
						"notes",
						"Teste GraphQL conflito"))));
	}
}
