package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

public class DatasetControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DatasetService datasetService;

	@Autowired
	private ElasticsearchService elasticService;

	@Autowired
	private ElasticsearchConfiguration elasticConfig;

	@BeforeEach
	public void setup() throws IOException {
		elasticService.createOrEnsureIndexIsEmpty(elasticConfig.getDatasetIndex());
	}

	@AfterEach
	public void teardown() throws IOException {
		elasticService.deleteIndex(elasticConfig.getDatasetIndex());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateDataset() throws Exception {

		Dataset dataset = datasetService.createDataset(new Dataset()
				.setName("test-dataset-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.post("/datasets")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(dataset)))
				.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDataset() throws Exception {

		Dataset dataset = datasetService.createDataset(new Dataset()
				.setName("test-dataset-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.get("/datasets/" + dataset.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDatasets() throws Exception {

		datasetService.createDataset(new Dataset()
				.setName("test-dataset-name0")
				.setDescription("my description"));

		datasetService.createDataset(new Dataset()
				.setName("test-dataset-name1")
				.setDescription("my description"));

		datasetService.createDataset(new Dataset()
				.setName("test-dataset-name2")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.get("/datasets")
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteDataset() throws Exception {

		Dataset dataset = datasetService.createDataset(new Dataset()
				.setName("test-dataset-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.delete("/datasets/" + dataset.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertTrue(datasetService.getDataset(dataset.getId()).isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadDataset() throws Exception {

		Dataset dataset = datasetService.createDataset(new Dataset()
				.setName("test-dataset-name")
				.setDescription("my description"));

		String content = "col0,col1,col2,col3\na,b,c,d\n";

		// Create a MockMultipartFile object
		MockMultipartFile file = new MockMultipartFile(
				"file", // name of the file as expected in the request
				"filename.csv", // original filename
				"text/csv", // content type
				content.getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/datasets/" + dataset.getId() + "/upload-csv")
						.file(file)
						.queryParam("filename", "filename.csv")
						.with(csrf())
						.contentType(MediaType.MULTIPART_FORM_DATA)
						.with(request -> {
							request.setMethod("PUT");
							return request;
						}))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadDatasetFromGithub() throws Exception {

		Dataset dataset = datasetService.createDataset(new Dataset()
				.setName("test-dataset-name")
				.setDescription("my description"));

		mockMvc.perform(
				MockMvcRequestBuilders.put("/datasets/" + dataset.getId() + "/upload-csv-from-github")
						.with(csrf())
						.param("repoOwnerAndName", "unchartedsoftware/torflow")
						.param("path", "README.md")
						.param("filename", "torflow-readme.md")
						.contentType("application/json"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDownloadDataset() throws Exception {

		Dataset dataset = datasetService.createDataset(new Dataset()
				.setName("test-dataset-name")
				.setDescription("my description"));

		String content = "col0,col1,col2,col3\na,b,c,d\n";

		// Create a MockMultipartFile object
		MockMultipartFile file = new MockMultipartFile(
				"file", // name of the file as expected in the request
				"filename.csv", // original filename
				"text/csv", // content type
				content.getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/datasets/" + dataset.getId() + "/upload-csv")
						.file(file)
						.queryParam("filename", "filename.csv")
						.with(csrf())
						.contentType(MediaType.MULTIPART_FORM_DATA)
						.with(request -> {
							request.setMethod("PUT");
							return request;
						}))
				.andExpect(status().isOk());

		MvcResult res = mockMvc
				.perform(MockMvcRequestBuilders.get("/datasets/" + dataset.getId() + "/download-csv")
						.queryParam("filename", "filename.csv")
						.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String resultContent = res.getResponse().getContentAsString();

		Assertions.assertTrue(resultContent.length() > 0);
	}
}
