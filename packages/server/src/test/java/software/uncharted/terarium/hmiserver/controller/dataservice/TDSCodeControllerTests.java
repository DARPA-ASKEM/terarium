package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
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
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.code.CodeFile;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Dynamics;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.CodeService;
import software.uncharted.terarium.hmiserver.service.data.ProjectSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

public class TDSCodeControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CodeService codeAssetService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectSearchService projectSearchService;

	Project project;

	@BeforeEach
	public void setup() throws IOException {
		projectSearchService.setupIndexAndAliasAndEnsureEmpty();

		project = projectService.createProject(
			(Project) new Project().setPublicAsset(true).setName("test-project-name").setDescription("my description")
		);
	}

	@AfterEach
	public void teardown() throws IOException {
		projectSearchService.teardownIndexAndAlias();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateCode() throws Exception {
		final Code codeAsset = (Code) new Code().setName("test-code-name").setDescription("my description");

		mockMvc
			.perform(
				MockMvcRequestBuilders.post("/code-asset")
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(codeAsset))
			)
			.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetCode() throws Exception {
		final Code codeAsset = codeAssetService.createAsset(
			(Code) new Code().setName("test-code-name").setDescription("my description"),
			project.getId()
		);

		mockMvc
			.perform(
				MockMvcRequestBuilders.get("/code-asset/" + codeAsset.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
			)
			.andExpect(status().isOk());
	}

	Map<String, String> createMetadata() {
		return Map.of("key1", "value1", "key2", "value2");
	}

	Dynamics createDynamics() {
		return new Dynamics().setName("dynamics_name").setDescription("description").setBlock(List.of("a", "b", "c"));
	}

	CodeFile createCodeFile() {
		return new CodeFile().setFileNameAndProgrammingLanguage("test.py").setDynamics(createDynamics());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteCode() throws Exception {
		final Code codeAsset = codeAssetService.createAsset(
			(Code) new Code()
				.setMetadata(createMetadata())
				.setFiles(Map.of("test.py", createCodeFile()))
				.setName("test-code-name")
				.setDescription("my description"),
			project.getId()
		);

		mockMvc
			.perform(
				MockMvcRequestBuilders.delete("/code-asset/" + codeAsset.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
			)
			.andExpect(status().isOk());

		Assertions.assertTrue(codeAssetService.getAsset(codeAsset.getId()).isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadCode() throws Exception {
		final Code codeAsset = codeAssetService.createAsset(
			(Code) new Code()
				.setMetadata(createMetadata())
				.setFiles(Map.of("test.py", createCodeFile()))
				.setName("test-code-name")
				.setDescription("my description"),
			project.getId()
		);

		// Create a MockMultipartFile object
		final MockMultipartFile file = new MockMultipartFile(
			"file", // name of the file as expected in the request
			"filename.txt", // original filename
			"text/plain", // content type
			"file content".getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc
			.perform(
				MockMvcRequestBuilders.multipart("/code-asset/" + codeAsset.getId() + "/upload-code")
					.file(file)
					.param("project-id", PROJECT_ID.toString())
					.queryParam("filename", "filename.txt")
					.with(csrf())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.with(request -> {
						request.setMethod("PUT");
						return request;
					})
			)
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadCodeFromGithub() throws Exception {
		final Code codeAsset = codeAssetService.createAsset(
			(Code) new Code().setMetadata(createMetadata()).setName("test-code-name").setDescription("my description"),
			project.getId()
		);

		mockMvc
			.perform(
				MockMvcRequestBuilders.put("/code-asset/" + codeAsset.getId() + "/upload-code-from-github")
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
					.param("repo-owner-and-name", "unchartedsoftware/torflow")
					.param("path", "README.md")
					.param("filename", "torflow-readme.md")
					.contentType("application/json")
			)
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadCodeFromGithubRepo() throws Exception {
		final Code codeAsset = codeAssetService.createAsset(
			(Code) new Code().setMetadata(createMetadata()).setName("test-code-name").setDescription("my description"),
			project.getId()
		);

		mockMvc
			.perform(
				MockMvcRequestBuilders.put("/code-asset/" + codeAsset.getId() + "/upload-code-from-github-repo")
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
					.param("repo-owner-and-name", "unchartedsoftware/torflow")
					.param("repo-name", "torflow.zip")
					.contentType("application/json")
			)
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDownloadCodeAsText() throws Exception {
		final Code codeAsset = codeAssetService.createAsset(
			(Code) new Code().setMetadata(createMetadata()).setName("test-code-name").setDescription("my description"),
			project.getId()
		);

		final String content = "this is the file content for the testItCanDownloadCode test";

		// Create a MockMultipartFile object
		final MockMultipartFile file = new MockMultipartFile(
			"file", // name of the file as expected in the request
			"filename.txt", // original filename
			"text/plain", // content type
			content.getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc
			.perform(
				MockMvcRequestBuilders.multipart("/code-asset/" + codeAsset.getId() + "/upload-code")
					.file(file)
					.param("project-id", PROJECT_ID.toString())
					.queryParam("filename", "filename.txt")
					.with(csrf())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.with(request -> {
						request.setMethod("PUT");
						return request;
					})
			)
			.andExpect(status().isOk());

		final MvcResult res = mockMvc
			.perform(
				MockMvcRequestBuilders.get("/code-asset/" + codeAsset.getId() + "/download-code-as-text")
					.queryParam("filename", "filename.txt")
					.with(csrf())
			)
			.andExpect(status().isOk())
			.andReturn();

		final String resultContent = res.getResponse().getContentAsString();

		Assertions.assertEquals(content, resultContent);
	}
}
