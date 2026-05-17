package appointment_service.api.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import appointment_service.infrastructure.memory.InMemoryUserRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientAppointmentRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void patientCanReadOwnHistory() throws Exception {
		mockMvc.perform(get("/api/patients/{patientId}/appointments", InMemoryUserRepositoryAdapter.SAMPLE_PATIENT_ID)
				.with(httpBasic("paciente@hospital.com", "123456")))
				.andExpect(status().isOk());
	}

	@Test
	void patientCannotReadAnotherPatientHistory() throws Exception {
		mockMvc.perform(get("/api/patients/{patientId}/appointments", "99999999-9999-9999-9999-999999999991")
				.with(httpBasic("paciente@hospital.com", "123456")))
				.andExpect(status().isForbidden());
	}
}
