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
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;

public class DocumentControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocumentAssetService documentAssetService;

	final DocumentAsset documentAsset0 = new DocumentAsset()
			.setId(UUID.randomUUID())
			.setName("test-document-name")
			.setDescription("my description");

	final DocumentAsset documentAsset1 = new DocumentAsset()
			.setId(UUID.randomUUID())
			.setName("test-document-name")
			.setDescription("my description");

	final DocumentAsset documentAsset2 = new DocumentAsset()
			.setId(UUID.randomUUID())
			.setName("test-document-name")
			.setDescription("my description");

	@After
	public void tearDown() throws IOException {
		documentAssetService.deleteDocumentAsset(documentAsset0.getId());
		documentAssetService.deleteDocumentAsset(documentAsset1.getId());
		documentAssetService.deleteDocumentAsset(documentAsset2.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateDocument() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/document-asset")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(documentAsset0)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDocument() throws Exception {

		documentAssetService.createDocumentAsset(documentAsset0);

		mockMvc.perform(MockMvcRequestBuilders.get("/document-asset/" + documentAsset0.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDocuments() throws Exception {
		documentAssetService.createDocumentAsset(documentAsset0);
		documentAssetService.createDocumentAsset(documentAsset1);
		documentAssetService.createDocumentAsset(documentAsset2);

		mockMvc.perform(MockMvcRequestBuilders.get("/document-asset")
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteDocument() throws Exception {
		documentAssetService.createDocumentAsset(documentAsset0);

		mockMvc.perform(MockMvcRequestBuilders.delete("/document-asset/" + documentAsset0.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertNull(documentAssetService.getDocumentAsset(documentAsset0.getId()));
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadDocument() throws Exception {
		documentAssetService.createDocumentAsset(documentAsset0);

		// Create a MockMultipartFile object
		MockMultipartFile file = new MockMultipartFile(
				"file", // name of the file as expected in the request
				"filename.txt", // original filename
				"text/plain", // content type
				"file content".getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/document-asset/" + documentAsset0.getId() + "/uploadDocument")
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

		documentAssetService.createDocumentAsset(documentAsset0);

		mockMvc.perform(
				MockMvcRequestBuilders.put("/document-asset/" + documentAsset0.getId() + "/uploadDocumentFromGithub")
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
		documentAssetService.createDocumentAsset(documentAsset0);

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
				MockMvcRequestBuilders.multipart("/document-asset/" + documentAsset0.getId() + "/uploadDocument")
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
				.perform(MockMvcRequestBuilders.get("/document-asset/" + documentAsset0.getId() + "/downloadDocument")
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
		documentAssetService.createDocumentAsset(documentAsset0);

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
				MockMvcRequestBuilders.multipart("/document-asset/" + documentAsset0.getId() + "/uploadDocument")
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
						.get("/document-asset/" + documentAsset0.getId() + "/download-document-as-text")
						.queryParam("filename", "filename.txt")
						.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String resultContent = res.getResponse().getContentAsString();

		Assertions.assertEquals(content, resultContent);
	}
}
