package software.uncharted.terarium.hmiserver.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;

public class AuthoritiesTests extends TerariumApplicationTests {

	@Test
	@WithUserDetails(MockUser.ADAM)
	public void testItCanPreauthorizeRoles() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/test/authorities/hasRoleAdmin")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItReturns403WhenPreauthorizedRolesAreNotPresent() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/test/authorities/hasRoleAdmin")).andExpect(status().isForbidden());
	}

	@Test
	@WithUserDetails(MockUser.ADAM)
	public void testItCanPreauthorizeAuthorities() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/test/authorities/hasCreateUsers")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItReturns403WhenPreauthorizedAuthoritiesAreNotPresent() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/test/authorities/hasCreateUsers")).andExpect(status().isForbidden());
	}
}
