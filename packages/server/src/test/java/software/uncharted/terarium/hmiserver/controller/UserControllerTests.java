package software.uncharted.terarium.hmiserver.controller;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.security.SecurityConfig;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfig.class)
public class UserControllerTests extends TerariumApplicationTests {

	@Test
	public void testItReturns401OnUnAuthorized() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/user/me"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(username="ursula",roles={"user"})
	public void testItReturnsOkOnAuthorized() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/user/me"))
			.andExpect(status().isOk());
	}
}
