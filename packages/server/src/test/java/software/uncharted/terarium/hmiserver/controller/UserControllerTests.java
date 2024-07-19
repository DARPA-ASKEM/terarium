package software.uncharted.terarium.hmiserver.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.security.Roles;

public class UserControllerTests extends TerariumApplicationTests {

	@Test
	public void testItReturns401OnUnAuthorized() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/user/me")).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username = "ursula", authorities = { Roles.USER })
	public void testItReturnsOkOnAuthorized() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/user/me")).andExpect(status().isOk());
	}
}
