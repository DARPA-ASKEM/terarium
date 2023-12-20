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
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.service.data.CodeService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

public class TDSCodeControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CodeService codeAssetService;

	@Autowired
	private ElasticsearchService elasticService;

	@Autowired
	private ElasticsearchConfiguration elasticConfig;

	@BeforeEach
	public void setup() throws IOException {
		elasticService.createOrEnsureIndexIsEmpty(elasticConfig.getCodeIndex());
	}

	@AfterEach
	public void teardown() throws IOException {
		elasticService.deleteIndex(elasticConfig.getCodeIndex());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateCode() throws Exception {

		final Code codeAsset = new Code()
				.setName("test-code-name")
				.setDescription("my description");

		mockMvc.perform(MockMvcRequestBuilders.post("/code-asset")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(codeAsset)))
				.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetCode() throws Exception {

		final Code codeAsset = codeAssetService.createCode(new Code()
				.setName("test-code-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.get("/code-asset/" + codeAsset.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetCodes() throws Exception {

		codeAssetService.createCode(new Code()
				.setName("test-code-name")
				.setDescription("my description"));

		codeAssetService.createCode(new Code()
				.setName("test-code-name")
				.setDescription("my description"));

		codeAssetService.createCode(new Code()
				.setName("test-code-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.get("/code-asset")
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteCode() throws Exception {

		final Code codeAsset = codeAssetService.createCode(new Code()
				.setName("test-code-name")
				.setDescription("my description"));

		mockMvc.perform(MockMvcRequestBuilders.delete("/code-asset/" + codeAsset.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertTrue(codeAssetService.getCode(codeAsset.getId()).isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadCode() throws Exception {

		final Code codeAsset = codeAssetService.createCode(new Code()
				.setName("test-code-name")
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
				MockMvcRequestBuilders.multipart("/code-asset/" + codeAsset.getId() + "/upload-code")
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
	public void testItCanUploadCodeFromGithub() throws Exception {

		final Code codeAsset = codeAssetService.createCode(new Code()
				.setName("test-code-name")
				.setDescription("my description"));

		mockMvc.perform(
				MockMvcRequestBuilders.put("/code-asset/" + codeAsset.getId() + "/upload-code-from-github")
						.with(csrf())
						.param("repo-owner-and-name", "unchartedsoftware/torflow")
						.param("path", "README.md")
						.param("filename", "torflow-readme.md")
						.contentType("application/json"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadCodeFromGithubRepo() throws Exception {

		final Code codeAsset = codeAssetService.createCode(new Code()
				.setName("test-code-name")
				.setDescription("my description"));

		mockMvc.perform(
				MockMvcRequestBuilders.put("/code-asset/" + codeAsset.getId() + "/upload-code-from-github-repo")
						.with(csrf())
						.param("repo-owner-and-name", "unchartedsoftware/torflow")
						.param("repo-name", "torflow.zip")
						.contentType("application/json"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDownloadCodeAsText() throws Exception {

		final Code codeAsset = codeAssetService.createCode(new Code()
				.setName("test-code-name")
				.setDescription("my description"));

		String content = "this is the file content for the testItCanDownloadCode test";

		// Create a MockMultipartFile object
		MockMultipartFile file = new MockMultipartFile(
				"file", // name of the file as expected in the request
				"filename.txt", // original filename
				"text/plain", // content type
				content.getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/code-asset/" + codeAsset.getId() + "/upload-code")
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
						.get("/code-asset/" + codeAsset.getId() + "/download-code-as-text")
						.queryParam("filename", "filename.txt")
						.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String resultContent = res.getResponse().getContentAsString();

		Assertions.assertEquals(content, resultContent);
	}
}
