package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.ArtifactService;
import software.uncharted.terarium.hmiserver.service.data.ProjectSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

public class ArtifactControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ArtifactService artifactService;

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
	public void testItCanCreateArtifact() throws Exception {
		final Artifact artifact = (Artifact) new Artifact().setName("test-artifact-name").setDescription("my description");

		mockMvc
			.perform(
				MockMvcRequestBuilders.post("/artifacts")
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(artifact))
			)
			.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetArtifact() throws Exception {
		final Artifact artifact = artifactService.createAsset(
			(Artifact) new Artifact().setName("test-artifact-name").setDescription("my description"),
			project.getId()
		);

		mockMvc
			.perform(
				MockMvcRequestBuilders.get("/artifacts/" + artifact.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
			)
			.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteArtifact() throws Exception {
		final Artifact artifact = artifactService.createAsset(
			(Artifact) new Artifact().setName("test-artifact-name").setDescription("my description"),
			project.getId()
		);

		mockMvc
			.perform(
				MockMvcRequestBuilders.delete("/artifacts/" + artifact.getId())
					.param("project-id", PROJECT_ID.toString())
					.with(csrf())
			)
			.andExpect(status().isOk());

		Assertions.assertTrue(artifactService.getAsset(artifact.getId()).isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadArtifact() throws Exception {
		final Artifact artifact = artifactService.createAsset(
			(Artifact) new Artifact().setName("test-artifact-name").setDescription("my description"),
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
				MockMvcRequestBuilders.multipart("/artifacts/" + artifact.getId() + "/upload-file")
					.file(file)
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
	public void testItCanUploadArtifactFromGithub() throws Exception {
		final Artifact artifact = artifactService.createAsset(
			(Artifact) new Artifact().setName("test-artifact-name").setDescription("my description"),
			project.getId()
		);

		mockMvc
			.perform(
				MockMvcRequestBuilders.put("/artifacts/" + artifact.getId() + "/upload-artifact-from-github")
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
	public void testItCanDownloadArtifact() throws Exception {
		final Artifact artifact = artifactService.createAsset(
			(Artifact) new Artifact().setName("test-artifact-name").setDescription("my description"),
			project.getId()
		);

		final String content = "this is the file content for the testItCanDownloadArtifact test";

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
				MockMvcRequestBuilders.multipart("/artifacts/" + artifact.getId() + "/upload-file")
					.file(file)
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
				MockMvcRequestBuilders.get("/artifacts/" + artifact.getId() + "/download-file")
					.queryParam("filename", "filename.txt")
					.with(csrf())
			)
			.andExpect(status().isOk())
			.andReturn();

		final String resultContent = res.getResponse().getContentAsString();

		Assertions.assertEquals(content, resultContent);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDownloadArtifactAsText() throws Exception {
		final Artifact artifact = artifactService.createAsset(
			(Artifact) new Artifact().setName("test-artifact-name").setDescription("my description"),
			project.getId()
		);

		final String content = "this is the file content for the testItCanDownloadArtifact test";

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
				MockMvcRequestBuilders.multipart("/artifacts/" + artifact.getId() + "/upload-file")
					.file(file)
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
				MockMvcRequestBuilders.get("/artifacts/" + artifact.getId() + "/download-file-as-text")
					.queryParam("filename", "filename.txt")
					.with(csrf())
			)
			.andExpect(status().isOk())
			.andReturn();

		final String resultContent = res.getResponse().getContentAsString();

		Assertions.assertEquals(content, resultContent);
	}
}
