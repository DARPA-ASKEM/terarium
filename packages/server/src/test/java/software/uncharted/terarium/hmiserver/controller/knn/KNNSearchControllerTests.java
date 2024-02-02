package software.uncharted.terarium.hmiserver.controller.knn;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.controller.knn.KNNSearchController.KNNSearchRequest;

@Slf4j
public class KNNSearchControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	private static final String TEST_INDEX = "tds_covid_tera_1.0";

	@Test
	@WithUserDetails(MockUser.ADAM)
	public void testKnnSearch() throws Exception {

		KNNSearchRequest req = new KNNSearchRequest();
		req.setText("Papers that discuss the use of masks to prevent the spread of COVID-19");

		// Test that we get a 404 if we provide a project id that doesn't exist
		MvcResult res = mockMvc.perform(MockMvcRequestBuilders.get("/knn/" + TEST_INDEX)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.with(request -> {
					try {
						request.setMethod("GET");
						request.setContent(objectMapper.writeValueAsBytes(req));
					} catch (Exception e) {
						e.printStackTrace();
					}
					return request;
				})
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		List<JsonNode> docs = objectMapper.readValue(res.getResponse().getContentAsString(),
				new TypeReference<List<JsonNode>>() {
				});
		for (JsonNode doc : docs) {
			log.info("doc: {}", doc);
		}
	}

}
