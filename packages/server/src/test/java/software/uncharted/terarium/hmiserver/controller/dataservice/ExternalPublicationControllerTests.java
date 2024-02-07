package software.uncharted.terarium.hmiserver.controller.dataservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.externalpublication.ExternalPublication;
import software.uncharted.terarium.hmiserver.service.data.ExternalPublicationService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

import java.io.IOException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ExternalPublicationControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ExternalPublicationService externalPublicationService;

	@Autowired
	private ElasticsearchService elasticService;

	@Autowired
	private ElasticsearchConfiguration elasticConfig;

	@BeforeEach
	public void setup() throws IOException {
		elasticService.createOrEnsureIndexIsEmpty(elasticConfig.getExternalPublicationIndex());
	}

	@AfterEach
	public void teardown() throws IOException {
		elasticService.deleteIndex(elasticConfig.getExternalPublicationIndex());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateExternalPublication() throws Exception {

		final ExternalPublication externalPublication = new ExternalPublication()
				.setTitle("test-publication-name");

		mockMvc.perform(MockMvcRequestBuilders.post("/external/publications")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(externalPublication)))
				.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetExternalPublication() throws Exception {

		final ExternalPublication externalPublication = externalPublicationService
				.createAsset(new ExternalPublication()
						.setTitle("test-publication-name"));

		mockMvc.perform(MockMvcRequestBuilders.get("/external/publications/" + externalPublication.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetExternalPublications() throws Exception {

		externalPublicationService
				.createAsset(new ExternalPublication()
						.setTitle("test-publication-name"));
		externalPublicationService
				.createAsset(new ExternalPublication()
						.setTitle("test-publication-name"));
		externalPublicationService
				.createAsset(new ExternalPublication()
						.setTitle("test-publication-name"));

		mockMvc.perform(MockMvcRequestBuilders.get("/external/publications")
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteExternalPublication() throws Exception {

		final ExternalPublication externalPublication = externalPublicationService
				.createAsset(new ExternalPublication()
						.setTitle("test-publication-name"));

		mockMvc.perform(MockMvcRequestBuilders.delete("/external/publications/" + externalPublication.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertTrue(externalPublicationService.getAsset(externalPublication.getId()).isEmpty());
	}

}
