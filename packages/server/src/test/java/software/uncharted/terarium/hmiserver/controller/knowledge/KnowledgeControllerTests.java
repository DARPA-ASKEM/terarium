package software.uncharted.terarium.hmiserver.controller.knowledge;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProjectSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

@Slf4j
public class KnowledgeControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocumentAssetService documentAssetService;

	@Autowired
	private ModelService modelService;

	@Autowired
	private DatasetService datasetService;

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

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void equationsToModelRegNet() throws Exception {
		final String payload1 =
			"""
				{
					"equations": [
						"\\\\frac{dS}{dt} = -\\\\alpha S I -\\\\beta S D -\\\\gamma S A -\\\\delta S R",
						"\\\\frac{dI}{dt} = \\\\alpha S I +\\\\beta S D +\\\\gamma S A +\\\\delta S R - \\\\epsilon I -\\\\zeta I -\\\\lambda I",
						"\\\\frac{dD}{dt} = -\\\\eta D + \\\\epsilon I - \\\\rho D",
						"\\\\frac{dA}{dt} = -\\\\kappa A -\\\\theta A -\\\\mu A +\\\\zeta I",
						"\\\\frac{dT}{dt} = -\\\\tau T +\\\\mu A +\\\\nu R -\\\\sigma T",
						"\\\\frac{dH}{dt} = \\\\kappa A + \\\\xi R +\\\\sigma T +\\\\rho D + \\\\lambda I",
						"\\\\frac{dE}{dt} = \\\\tau T",
						"\\\\frac{dR}{dt} = \\\\eta D + \\\\theta A -\\\\nu R -\\\\xi R"
					],
					"model": "regnet"
				}
			""";

		MvcResult res = mockMvc
			.perform(
				MockMvcRequestBuilders.post("/knowledge/equations-to-model")
					.contentType(MediaType.APPLICATION_JSON)
					.content(payload1)
					.with(csrf())
			)
			.andExpect(status().isOk())
			.andReturn();

		String responseContent = res.getResponse().getContentAsString().replaceAll("^\"|\"$", "");
		UUID regnetModelId = UUID.fromString(responseContent);
		log.info(regnetModelId.toString());

		final String payload2 =
			"""
				{
					"equations": [
					"\\\\frac{d S}{d t} = -\\\\beta S I",
						"\\\\frac{d I}{d t} = \\\\beta S I - \\\\gamma I",
						"\\\\frac{d R}{d t} = \\\\gamma I"],
					"model": "regnet"
				}
			""";

		res = mockMvc
			.perform(
				MockMvcRequestBuilders.post("/knowledge/equations-to-model")
					.contentType(MediaType.APPLICATION_JSON)
					.content(payload2)
					.with(csrf())
			)
			.andExpect(status().isOk())
			.andReturn();

		responseContent = res.getResponse().getContentAsString().replaceAll("^\"|\"$", "");
		regnetModelId = UUID.fromString(responseContent);
		log.info(regnetModelId.toString());
	}

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void equationsToModelPetrinet() throws Exception {
		final String payload1 =
			"""
				{
					"equations": [
						"\\\\frac{dS}{dt} = -\\\\alpha S I -\\\\beta S D -\\\\gamma S A -\\\\delta S R",
						"\\\\frac{dI}{dt} = \\\\alpha S I +\\\\beta S D +\\\\gamma S A +\\\\delta S R - \\\\epsilon I -\\\\zeta I -\\\\lambda I",
						"\\\\frac{dD}{dt} = -\\\\eta D + \\\\epsilon I - \\\\rho D",
						"\\\\frac{dA}{dt} = -\\\\kappa A -\\\\theta A -\\\\mu A +\\\\zeta I",
						"\\\\frac{dT}{dt} = -\\\\tau T +\\\\mu A +\\\\nu R -\\\\sigma T",
						"\\\\frac{dH}{dt} = \\\\kappa A + \\\\xi R +\\\\sigma T +\\\\rho D + \\\\lambda I",
						"\\\\frac{dE}{dt} = \\\\tau T",
						"\\\\frac{dR}{dt} = \\\\eta D + \\\\theta A -\\\\nu R -\\\\xi R"
					],
					"model": "petrinet"
				}
			""";

		MvcResult res = mockMvc
			.perform(
				MockMvcRequestBuilders.post("/knowledge/equations-to-model")
					.contentType(MediaType.APPLICATION_JSON)
					.content(payload1)
					.with(csrf())
			)
			.andExpect(status().isOk())
			.andReturn();

		String responseContent = res.getResponse().getContentAsString().replaceAll("^\"|\"$", "");
		UUID petrinetModelId = UUID.fromString(responseContent);
		log.info(petrinetModelId.toString());

		final String payload2 =
			"""
				{
					"equations": [
						"\\\\frac{d S}{d t} = -\\\\beta S I",
						"\\\\frac{d I}{d t} = \\\\beta S I - \\\\gamma I",
						"\\\\frac{d R}{d t} = \\\\gamma I"],
					"model": "regnet"
				}
			""";

		res = mockMvc
			.perform(
				MockMvcRequestBuilders.post("/knowledge/equations-to-model")
					.contentType(MediaType.APPLICATION_JSON)
					.content(payload2)
					.with(csrf())
			)
			.andExpect(status().isOk())
			.andReturn();

		responseContent = res.getResponse().getContentAsString().replaceAll("^\"|\"$", "");
		petrinetModelId = UUID.fromString(responseContent);
		log.info(petrinetModelId.toString());
	}

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void profileModel() throws Exception {
		DocumentAsset documentAsset = (DocumentAsset) new DocumentAsset()
			.setName("test-pdf-name")
			.setDescription("my description");

		documentAsset = documentAssetService.createAsset(documentAsset, project.getId());

		final ClassPathResource resource = new ClassPathResource("knowledge/sir.json");
		final byte[] content = Files.readAllBytes(resource.getFile().toPath());
		Model model = objectMapper.readValue(content, Model.class);

		model = modelService.createAsset(model, project.getId());

		mockMvc
			.perform(
				MockMvcRequestBuilders.post("/knowledge/profile-model/" + model.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.param("document-id", documentAsset.getId().toString())
					.with(csrf())
			)
			.andExpect(status().isOk());

		model = modelService.getAsset(model.getId()).orElseThrow();

		Assertions.assertNotNull(model.getMetadata().getCard());
	}

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void profileDataset() throws Exception {
		final ClassPathResource resource = new ClassPathResource("knowledge/dataset.csv");
		final byte[] content = Files.readAllBytes(resource.getFile().toPath());

		Dataset dataset = datasetService.createAsset(
			(Dataset) new Dataset().setName("test-dataset-name").setDescription("my description"),
			project.getId()
		);

		// Create a MockMultipartFile object
		final MockMultipartFile file = new MockMultipartFile(
			"file", // name of the file as expected in the request
			"filename.csv", // original filename
			"text/csv", // content type
			content // content of the file
		);

		// Perform the multipart file upload request
		mockMvc
			.perform(
				MockMvcRequestBuilders.multipart("/datasets/" + dataset.getId() + "/upload-csv")
					.file(file)
					.queryParam("filename", "filename.csv")
					.with(csrf())
					.contentType(MediaType.MULTIPART_FORM_DATA)
					.with(request -> {
						request.setMethod("PUT");
						return request;
					})
			)
			.andExpect(status().isOk());

		DocumentAsset documentAsset = (DocumentAsset) new DocumentAsset()
			.setName("test-pdf-name")
			.setDescription("my description");

		documentAsset = documentAssetService.createAsset(documentAsset, project.getId());

		mockMvc
			.perform(
				MockMvcRequestBuilders.post("/knowledge/profile-dataset/" + dataset.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.param("document-id", documentAsset.getId().toString())
					.with(csrf())
			)
			.andExpect(status().isOk());

		dataset = datasetService.getAsset(dataset.getId()).orElseThrow();

		Assertions.assertNotNull(dataset.getMetadata().get("dataCard"));
	}
}
