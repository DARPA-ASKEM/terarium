package software.uncharted.terarium.hmiserver.controller.tds;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.models.dataservice.ExternalPublication;
import software.uncharted.terarium.hmiserver.security.Roles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExternalPublicationTests extends TerariumApplicationTests {

//	// Technically both of these could be found from an xDD document search but
//	// this isn't a test class for xDD so we'll just hardcode them here
//	private static final String SAMPLE_GDDID = "5f525b54a58f1dfd52141151";
//	private static final String SAMPLE_TITLE = "Mucosal Pain Disorders of the Head and Neck";
//
//	//@Test
//	public void testCanGetPublicationsUnauthorized() throws Exception {
//		mockMvc.perform(MockMvcRequestBuilders.get("/external/publications")).andExpect(status().isUnauthorized());
//	}
//
//	//@Test
//	@WithMockUser(username="ursula",authorities={Roles.USER})
//	public void testCanGetPublicationsAuthorized() throws Exception {
//		mockMvc.perform(MockMvcRequestBuilders.get("/external/publications")).andExpect(status().isOk()).andReturn();
//	}
//
//	//@Test
//	@WithMockUser(username="ursula",authorities={Roles.USER})
//	public void testCanItCreateGetUpdateDeletePublication() throws Exception {
//
//		mockMvc.perform(MockMvcRequestBuilders.get("/external/publications")).andExpect(status().isOk()).andReturn();
//
//		final ExternalPublication publication = new ExternalPublication().setXddUri(SAMPLE_GDDID).setTitle(SAMPLE_TITLE);
//		final ObjectMapper mapper = new ObjectMapper();
//		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//		ExternalPublication res = null;
//		int id = -1;
//		final String json = mapper.writeValueAsString(publication);
//
//		// Create
//		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/external/publications").with(csrf())
//						.contentType(MediaType.APPLICATION_JSON)
//						.content(json)
//						.accept(MediaType.APPLICATION_JSON))
//				.andExpect(status().is2xxSuccessful()).andReturn();
//
//		try {
//			final JsonNode responseJson = mapper.readValue(result.getResponse().getContentAsByteArray(), JsonNode.class);
//			Assertions.assertNotNull(responseJson);
//			Assertions.assertNotNull(responseJson.get("id"));
//			id = responseJson.get("id").asInt();
//
//		} catch (final Exception e) {
//			Assertions.fail("Unable to create", e);
//		}
//
//		// Get
//		result = mockMvc.perform(MockMvcRequestBuilders.get("/external/publications/" + id)).andExpect(status().isOk()).andReturn();
//		try {
//			res = mapper.readValue(result.getResponse().getContentAsByteArray(), ExternalPublication.class);
//			Assertions.assertEquals(SAMPLE_GDDID, res.getXddUri());
//			Assertions.assertEquals(SAMPLE_TITLE, res.getTitle());
//
//		} catch (final Exception e) {
//			Assertions.fail("Unable to get", e);
//		}
//
//		// Update
//		final String updatedTitle = "Updated Title";
//		res.setTitle(updatedTitle);
//		mockMvc.perform(MockMvcRequestBuilders.put("/external/publications/" + id).with(csrf()).content(mapper.writeValueAsString(res)).contentType("application/json"))
//				.andExpect(status().isOk()).andReturn();
//
//		result = mockMvc.perform(MockMvcRequestBuilders.get("/external/publications/" + id)).andExpect(status().isOk()).andReturn();
//		try {
//			res = mapper.readValue(result.getResponse().getContentAsByteArray(), ExternalPublication.class);
//			Assertions.assertEquals(SAMPLE_GDDID, res.getXddUri());
//			Assertions.assertEquals(updatedTitle, res.getTitle());
//
//		} catch (final Exception e) {
//			Assertions.fail("Unable to update", e);
//		}
//
//		// Delete
//		mockMvc.perform(MockMvcRequestBuilders.delete("/external/publications/" + id).with(csrf())).andExpect(status().isOk()).andReturn();
//	}
}
