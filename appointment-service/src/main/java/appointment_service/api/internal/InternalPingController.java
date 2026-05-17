package appointment_service.api.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class InternalPingController {

	@GetMapping("/ping")
	public InternalPingResponse ping() {
		return new InternalPingResponse("appointment-service", "UP");
	}
}

record InternalPingResponse(String service, String status) {
}
