package notification_service.api.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationSecurityTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthIsPublic() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}

	@Test
	void notificationsRequireAuthentication() throws Exception {
		mockMvc.perform(get("/notifications"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void doctorCanListNotifications() throws Exception {
		mockMvc.perform(get("/notifications").with(httpBasic("medico@hospital.com", "123456")))
				.andExpect(status().isOk());
	}

	@Test
	void patientCannotListNotifications() throws Exception {
		mockMvc.perform(get("/notifications").with(httpBasic("paciente@hospital.com", "123456")))
				.andExpect(status().isForbidden());
	}
}
