package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.equation.Equation;
import software.uncharted.terarium.hmiserver.service.data.EquationService;

public class EquationControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private EquationService equationService;

	final Equation equation0 = new Equation()
			.setId("test-equation-id0")
			.setName("test-equation-name");

	final Equation equation1 = new Equation()
			.setId("test-equation-id1")
			.setName("test-equation-name");

	final Equation equation2 = new Equation()
			.setId("test-equation-id2")
			.setName("test-equation-name");

	@After
	public void tearDown() throws IOException {
		equationService.deleteEquation(equation0.getId());
		equationService.deleteEquation(equation1.getId());
		equationService.deleteEquation(equation2.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateEquation() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/equations")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(equation0)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetEquation() throws Exception {

		equationService.createEquation(equation0);

		mockMvc.perform(MockMvcRequestBuilders.get("/equations/" + equation0.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetEquations() throws Exception {
		equationService.createEquation(equation0);
		equationService.createEquation(equation1);
		equationService.createEquation(equation2);

		mockMvc.perform(MockMvcRequestBuilders.get("/equations")
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteEquation() throws Exception {
		equationService.createEquation(equation0);

		mockMvc.perform(MockMvcRequestBuilders.delete("/equations/" + equation0.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertNull(equationService.getEquation(equation0.getId()));
	}

}
