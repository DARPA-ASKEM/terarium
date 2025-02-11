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
import software.uncharted.terarium.hmiserver.service.ExtractionService;
import software.uncharted.terarium.hmiserver.service.data.CodeService;
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
	private CodeService codeService;

	@Autowired
	private ProjectSearchService projectSearchService;

	@Autowired
	private ExtractionService extractionService;

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
			.setText(
				"""
				In this paper, we study the effectiveness of the modelling approach on the pandemic due to the spreading
				of the novel COVID-19 disease and develop a susceptible-infected-removed (SIR) model that provides a
				theoretical framework to investigate its spread within a community. Here, the model is based upon the
				well-known susceptible-infected-removed (SIR) model with the difference that a total population is not
				deﬁned or kept constant per se and the number of susceptible individuals does not decline monotonically.
				To the contrary, as we show herein, it can be increased in surge periods! In particular, we investigate
				the time evolution of different populations and monitor diverse signiﬁcant parameters for the spread
				of the disease in various communities, represented by China, South Korea, India, Australia, USA, Italy
				and the state of Texas in the USA. The SIR model can provide us with insights and predictions of the
				spread of the virus in communities that the recorded data alone cannot. Our work shows the importance
				of modelling the spread of COVID-19 by the SIR model that we propose here, as it can help to assess
				the impact of the disease by offering valuable predictions. Our analysis takes into account data from
				January to June, 2020, the period that contains the data before and during the implementation of strict
				and control measures. We propose predictions on various parameters related to the spread of COVID-19
				and on the number of susceptible, infected and removed populations until September 2020. By comparing
				the recorded data with the data from our modelling approaches, we deduce that the spread of COVID-
				19 can be under control in all communities considered, if proper restrictions and strong policies are
				implemented to control the infection rates early from the spread of the disease.
				"""
			)
			.setName("test-pdf-name")
			.setDescription("my description");

		documentAsset = documentAssetService.createAsset(documentAsset, project.getId(), ASSUME_WRITE_PERMISSION);

		final ClassPathResource resource = new ClassPathResource("knowledge/sir.json");
		final byte[] content = Files.readAllBytes(resource.getFile().toPath());
		Model model = objectMapper.readValue(content, Model.class);

		model = modelService.createAsset(model, project.getId(), ASSUME_WRITE_PERMISSION);

		mockMvc
			.perform(
				MockMvcRequestBuilders.post("/knowledge/profile-model/" + model.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.param("document-id", documentAsset.getId().toString())
					.with(csrf())
			)
			.andExpect(status().isOk());

		model = modelService.getAsset(model.getId(), ASSUME_WRITE_PERMISSION).orElseThrow();

		Assertions.assertNotNull(model.getMetadata().getCard());
	}

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void profileDataset() throws Exception {
		final ClassPathResource resource = new ClassPathResource("knowledge/dataset.csv");
		final byte[] content = Files.readAllBytes(resource.getFile().toPath());

		Dataset dataset = datasetService.createAsset(
			(Dataset) new Dataset().setName("test-dataset-name").setDescription("my description"),
			project.getId(),
			ASSUME_WRITE_PERMISSION
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
			.setText(
				"""
				In this paper, we study the effectiveness of the modelling approach on the pandemic due to the spreading
				of the novel COVID-19 disease and develop a susceptible-infected-removed (SIR) model that provides a
				theoretical framework to investigate its spread within a community. Here, the model is based upon the
				well-known susceptible-infected-removed (SIR) model with the difference that a total population is not
				deﬁned or kept constant per se and the number of susceptible individuals does not decline monotonically.
				To the contrary, as we show herein, it can be increased in surge periods! In particular, we investigate
				the time evolution of different populations and monitor diverse signiﬁcant parameters for the spread
				of the disease in various communities, represented by China, South Korea, India, Australia, USA, Italy
				and the state of Texas in the USA. The SIR model can provide us with insights and predictions of the
				spread of the virus in communities that the recorded data alone cannot. Our work shows the importance
				of modelling the spread of COVID-19 by the SIR model that we propose here, as it can help to assess
				the impact of the disease by offering valuable predictions. Our analysis takes into account data from
				January to June, 2020, the period that contains the data before and during the implementation of strict
				and control measures. We propose predictions on various parameters related to the spread of COVID-19
				and on the number of susceptible, infected and removed populations until September 2020. By comparing
				the recorded data with the data from our modelling approaches, we deduce that the spread of COVID-
				19 can be under control in all communities considered, if proper restrictions and strong policies are
				implemented to control the infection rates early from the spread of the disease.
				"""
			)
			.setName("test-pdf-name")
			.setDescription("my description");

		documentAsset = documentAssetService.createAsset(documentAsset, project.getId(), ASSUME_WRITE_PERMISSION);

		mockMvc
			.perform(
				MockMvcRequestBuilders.post("/knowledge/profile-dataset/" + dataset.getId())
					.contentType(MediaType.APPLICATION_JSON)
					.param("document-id", documentAsset.getId().toString())
					.with(csrf())
			)
			.andExpect(status().isOk());

		dataset = datasetService.getAsset(dataset.getId(), ASSUME_WRITE_PERMISSION).orElseThrow();

		Assertions.assertNotNull(dataset.getMetadata().get("dataCard"));
	}
}
