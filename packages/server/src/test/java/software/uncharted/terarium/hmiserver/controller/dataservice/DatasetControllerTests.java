package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.UUID;

import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;

public class DatasetControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DatasetService datasetService;

	final Dataset dataset0 = new Dataset()
			.setId(UUID.randomUUID())
			.setName("test-dataset-name")
			.setDescription("my description");

	final Dataset dataset1 = new Dataset()
			.setId(UUID.randomUUID())
			.setName("test-dataset-name")
			.setDescription("my description");

	final Dataset dataset2 = new Dataset()
			.setId(UUID.randomUUID())
			.setName("test-dataset-name")
			.setDescription("my description");

	@After
	public void tearDown() throws IOException {
		datasetService.deleteDataset(dataset0.getId());
		datasetService.deleteDataset(dataset1.getId());
		datasetService.deleteDataset(dataset2.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateDataset() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/datasets")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(dataset0)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDataset() throws Exception {

		datasetService.createDataset(dataset0);

		mockMvc.perform(MockMvcRequestBuilders.get("/datasets/" + dataset0.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDatasets() throws Exception {
		datasetService.createDataset(dataset0);
		datasetService.createDataset(dataset1);
		datasetService.createDataset(dataset2);

		mockMvc.perform(MockMvcRequestBuilders.get("/datasets")
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteDataset() throws Exception {
		datasetService.createDataset(dataset0);

		mockMvc.perform(MockMvcRequestBuilders.delete("/datasets/" + dataset0.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertNull(datasetService.getDataset(dataset0.getId()));
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadDataset() throws Exception {
		datasetService.createDataset(dataset0);

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
				MockMvcRequestBuilders.multipart("/datasets/" + dataset0.getId() + "/uploadCSV")
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

		datasetService.createDataset(dataset0);

		mockMvc.perform(
				MockMvcRequestBuilders.put("/datasets/" + dataset0.getId() + "/uploadCSVFromGithub")
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
		datasetService.createDataset(dataset0);

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
				MockMvcRequestBuilders.multipart("/datasets/" + dataset0.getId() + "/uploadCSV")
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
				.perform(MockMvcRequestBuilders.get("/datasets/" + dataset0.getId() + "/downloadCSV")
						.queryParam("filename", "filename.csv")
						.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String resultContent = res.getResponse().getContentAsString();

		Assertions.assertTrue(resultContent.length() > 0);
	}
}
