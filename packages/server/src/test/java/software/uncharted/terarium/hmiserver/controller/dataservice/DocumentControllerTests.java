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
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

public class DocumentControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocumentAssetService documentAssetService;

	@Autowired
	private ElasticsearchService elasticService;

	@Autowired
	private ElasticsearchConfiguration elasticConfig;

	@BeforeEach
	public void setup() throws IOException {
		elasticService.createOrEnsureIndexIsEmpty(elasticConfig.getDocumentIndex());
	}

	@AfterEach
	public void teardown() throws IOException {
		elasticService.deleteIndex(elasticConfig.getDocumentIndex());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateDocument() throws Exception {

		final DocumentAsset documentAsset = new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description");

		mockMvc.perform(MockMvcRequestBuilders.post("/document-asset")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(documentAsset)))
				.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDocument() throws Exception {

		final DocumentAsset documentAsset = documentAssetService.createDocumentAsset(new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.get("/document-asset/" + documentAsset.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDocuments() throws Exception {

		documentAssetService.createDocumentAsset(new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description"));

		documentAssetService.createDocumentAsset(new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description"));

		documentAssetService.createDocumentAsset(new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.get("/document-asset")
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteDocument() throws Exception {

		final DocumentAsset documentAsset = documentAssetService.createDocumentAsset(new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.delete("/document-asset/" + documentAsset.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertNull(documentAssetService.getDocumentAsset(documentAsset.getId()));
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadDocument() throws Exception {

		final DocumentAsset documentAsset = documentAssetService.createDocumentAsset(new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description"));

		// Create a MockMultipartFile object
		MockMultipartFile file = new MockMultipartFile(
				"file", // name of the file as expected in the request
				"filename.txt", // original filename
				"text/plain", // content type
				"file content".getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/document-asset/" + documentAsset.getId() + "/uploadDocument")
						.file(file)
						.queryParam("filename", "filename.txt")
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
	public void testItCanUploadDocumentFromGithub() throws Exception {

		final DocumentAsset documentAsset = documentAssetService.createDocumentAsset(new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description"));

		mockMvc.perform(
				MockMvcRequestBuilders.put("/document-asset/" + documentAsset.getId() + "/uploadDocumentFromGithub")
						.with(csrf())
						.param("repoOwnerAndName", "unchartedsoftware/torflow")
						.param("path", "README.md")
						.param("filename", "torflow-readme.md")
						.contentType("application/json"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDownloadDocument() throws Exception {

		final DocumentAsset documentAsset = documentAssetService.createDocumentAsset(new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description"));

		String content = "this is the file content for the testItCanDownloadDocument test";

		// Create a MockMultipartFile object
		MockMultipartFile file = new MockMultipartFile(
				"file", // name of the file as expected in the request
				"filename.txt", // original filename
				"text/plain", // content type
				content.getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/document-asset/" + documentAsset.getId() + "/uploadDocument")
						.file(file)
						.queryParam("filename", "filename.txt")
						.with(csrf())
						.contentType(MediaType.MULTIPART_FORM_DATA)
						.with(request -> {
							request.setMethod("PUT");
							return request;
						}))
				.andExpect(status().isOk());

		MvcResult res = mockMvc
				.perform(MockMvcRequestBuilders.get("/document-asset/" + documentAsset.getId() + "/downloadDocument")
						.queryParam("filename", "filename.txt")
						.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String resultContent = res.getResponse().getContentAsString();

		Assertions.assertEquals(content, resultContent);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDownloadDocumentAsText() throws Exception {

		final DocumentAsset documentAsset = documentAssetService.createDocumentAsset(new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description"));

		String content = "this is the file content for the testItCanDownloadDocument test";

		// Create a MockMultipartFile object
		MockMultipartFile file = new MockMultipartFile(
				"file", // name of the file as expected in the request
				"filename.txt", // original filename
				"text/plain", // content type
				content.getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/document-asset/" + documentAsset.getId() + "/uploadDocument")
						.file(file)
						.queryParam("filename", "filename.txt")
						.with(csrf())
						.contentType(MediaType.MULTIPART_FORM_DATA)
						.with(request -> {
							request.setMethod("PUT");
							return request;
						}))
				.andExpect(status().isOk());

		MvcResult res = mockMvc
				.perform(MockMvcRequestBuilders
						.get("/document-asset/" + documentAsset.getId() + "/download-document-as-text")
						.queryParam("filename", "filename.txt")
						.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String resultContent = res.getResponse().getContentAsString();

		Assertions.assertEquals(content, resultContent);
	}
}
