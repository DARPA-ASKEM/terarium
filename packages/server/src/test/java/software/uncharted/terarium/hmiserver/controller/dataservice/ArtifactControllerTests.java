package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

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
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.service.data.ArtifactService;

public class ArtifactControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ArtifactService artifactService;

	final Artifact artifact0 = new Artifact()
			.setId("test-artifact-id0")
			.setName("test-artifact-name")
			.setDescription("my description");

	final Artifact artifact1 = new Artifact()
			.setId("test-artifact-id1")
			.setName("test-artifact-name")
			.setDescription("my description");

	final Artifact artifact2 = new Artifact()
			.setId("test-artifact-id2")
			.setName("test-artifact-name")
			.setDescription("my description");

	@After
	public void tearDown() throws IOException {
		artifactService.deleteArtifact(artifact0.getId());
		artifactService.deleteArtifact(artifact1.getId());
		artifactService.deleteArtifact(artifact2.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateArtifact() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/artifacts")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(artifact0)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetArtifact() throws Exception {

		artifactService.createArtifact(artifact0);

		mockMvc.perform(MockMvcRequestBuilders.get("/artifacts/" + artifact0.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetArtifacts() throws Exception {
		artifactService.createArtifact(artifact0);
		artifactService.createArtifact(artifact1);
		artifactService.createArtifact(artifact2);

		mockMvc.perform(MockMvcRequestBuilders.get("/artifacts")
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteArtifact() throws Exception {
		artifactService.createArtifact(artifact0);

		mockMvc.perform(MockMvcRequestBuilders.delete("/artifacts/" + artifact0.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertNull(artifactService.getArtifact(artifact0.getId()));
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUploadArtifact() throws Exception {
		artifactService.createArtifact(artifact0);

		// Create a MockMultipartFile object
		MockMultipartFile file = new MockMultipartFile(
				"file", // name of the file as expected in the request
				"filename.txt", // original filename
				"text/plain", // content type
				"file content".getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/artifacts/" + artifact0.getId() + "/uploadFile")
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
	public void testItCanUploadArtifactFromGithub() throws Exception {

		artifactService.createArtifact(artifact0);

		mockMvc.perform(
				MockMvcRequestBuilders.put("/artifacts/" + artifact0.getId() + "/uploadArtifactFromGithub")
						.with(csrf())
						.param("repoOwnerAndName", "unchartedsoftware/torflow")
						.param("path", "README.md")
						.param("filename", "torflow-readme.md")
						.contentType("application/json"))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDownloadArtifact() throws Exception {
		artifactService.createArtifact(artifact0);

		String content = "this is the file content for the testItCanDownloadArtifact test";

		// Create a MockMultipartFile object
		MockMultipartFile file = new MockMultipartFile(
				"file", // name of the file as expected in the request
				"filename.txt", // original filename
				"text/plain", // content type
				content.getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/artifacts/" + artifact0.getId() + "/uploadFile")
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
				.perform(MockMvcRequestBuilders.get("/artifacts/" + artifact0.getId() + "/download-file")
						.queryParam("filename", "filename.txt")
						.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String resultContent = res.getResponse().getContentAsString();

		Assertions.assertEquals(content, resultContent);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDownloadArtifactAsText() throws Exception {
		artifactService.createArtifact(artifact0);

		String content = "this is the file content for the testItCanDownloadArtifact test";

		// Create a MockMultipartFile object
		MockMultipartFile file = new MockMultipartFile(
				"file", // name of the file as expected in the request
				"filename.txt", // original filename
				"text/plain", // content type
				content.getBytes() // content of the file
		);

		// Perform the multipart file upload request
		mockMvc.perform(
				MockMvcRequestBuilders.multipart("/artifacts/" + artifact0.getId() + "/uploadFile")
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
						.get("/artifacts/" + artifact0.getId() + "/download-file-as-text")
						.queryParam("filename", "filename.txt")
						.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String resultContent = res.getResponse().getContentAsString();

		Assertions.assertEquals(content, resultContent);
	}
}
