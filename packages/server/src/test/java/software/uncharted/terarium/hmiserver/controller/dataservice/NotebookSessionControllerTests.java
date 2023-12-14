package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.notebooksession.NotebookSession;
import software.uncharted.terarium.hmiserver.service.data.NotebookSessionService;

public class NotebookSessionControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private NotebookSessionService equationService;

	final NotebookSession equation0 = new NotebookSession()
			.setId(UUID.randomUUID())
			.setName("test-notebook-name");

	final NotebookSession equation1 = new NotebookSession()
			.setId(UUID.randomUUID())
			.setName("test-notebook-name");

	final NotebookSession equation2 = new NotebookSession()
			.setId(UUID.randomUUID())
			.setName("test-notebook-name");

	@After
	public void tearDown() throws IOException {
		equationService.deleteNotebookSession(equation0.getId());
		equationService.deleteNotebookSession(equation1.getId());
		equationService.deleteNotebookSession(equation2.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateNotebookSession() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/equations")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(equation0)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetNotebookSession() throws Exception {

		equationService.createNotebookSession(equation0);

		mockMvc.perform(MockMvcRequestBuilders.get("/equations/" + equation0.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetNotebookSessions() throws Exception {
		equationService.createNotebookSession(equation0);
		equationService.createNotebookSession(equation1);
		equationService.createNotebookSession(equation2);

		mockMvc.perform(MockMvcRequestBuilders.get("/equations")
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteNotebookSession() throws Exception {
		equationService.createNotebookSession(equation0);

		mockMvc.perform(MockMvcRequestBuilders.delete("/equations/" + equation0.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertNull(equationService.getNotebookSession(equation0.getId()));
	}

}
