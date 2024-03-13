package software.uncharted.terarium.hmiserver.controller.knowledge;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Slf4j
public class KnowledgeControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DocumentAssetService documentAssetService;

	@Autowired
	private ModelService modelService;

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
	public void equationsToModelRegNet() throws Exception {

		final String payload1 = """
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

		MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/equations-to-model")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload1)
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String responseContent = res.getResponse().getContentAsString().replaceAll("^\"|\"$", "");
		UUID regnetModelId = UUID.fromString(responseContent);
		log.info(regnetModelId.toString());

		final String payload2 = """
					{
						"equations": [
						  "\\\\frac{d S}{d t} = -\\\\beta S I",
							"\\\\frac{d I}{d t} = \\\\beta S I - \\\\gamma I",
							"\\\\frac{d R}{d t} = \\\\gamma I"],
						"model": "regnet"
					}
				""";

		res = mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/equations-to-model")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload2)
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		responseContent = res.getResponse().getContentAsString().replaceAll("^\"|\"$", "");
		regnetModelId = UUID.fromString(responseContent);
		log.info(regnetModelId.toString());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void equationsToModelPetrinet() throws Exception {

		final String payload1 = """
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

		MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/equations-to-model")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload1)
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String responseContent = res.getResponse().getContentAsString().replaceAll("^\"|\"$", "");
		UUID petrinetModelId = UUID.fromString(responseContent);
		log.info(petrinetModelId.toString());

		final String payload2 = """
					{
						"equations": [
						  	"\\\\frac{d S}{d t} = -\\\\beta S I",
							"\\\\frac{d I}{d t} = \\\\beta S I - \\\\gamma I",
							"\\\\frac{d R}{d t} = \\\\gamma I"],
						"model": "regnet"
					}
				""";

		res = mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/equations-to-model")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload2)
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		responseContent = res.getResponse().getContentAsString().replaceAll("^\"|\"$", "");
		petrinetModelId = UUID.fromString(responseContent);
		log.info(petrinetModelId.toString());
	}

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void base64EquationsToAMRTests() throws Exception {

		final ClassPathResource resource1 = new ClassPathResource("knowledge/equation1.png");
		final byte[] content1 = Files.readAllBytes(resource1.getFile().toPath());
		final String encodedString1 = Base64.getEncoder().encodeToString(content1);

		final ClassPathResource resource2 = new ClassPathResource("knowledge/equation2.png");
		final byte[] content2 = Files.readAllBytes(resource2.getFile().toPath());
		final String encodedString2 = Base64.getEncoder().encodeToString(content2);

		final ClassPathResource resource3 = new ClassPathResource("knowledge/equation3.png");
		final byte[] content3 = Files.readAllBytes(resource3.getFile().toPath());
		final String encodedString3 = Base64.getEncoder().encodeToString(content3);

		final String payload = "{\"images\": [" +
				"\"" + encodedString1 + "\"," +
				"\"" + encodedString2 + "\"," +
				"\"" + encodedString3 + "\"],\"model\": \"regnet\"}";

		final MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/base64-equations-to-model")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload)
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		final Model amr = objectMapper.readValue(res.getResponse().getContentAsString(), Model.class);
		log.info(amr.toString());
	}

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void base64EquationsToLatexTests() throws Exception {

		final ClassPathResource resource1 = new ClassPathResource("knowledge/equation1.png");
		final byte[] content1 = Files.readAllBytes(resource1.getFile().toPath());
		final String encodedString1 = Base64.getEncoder().encodeToString(content1);

		final ClassPathResource resource2 = new ClassPathResource("knowledge/equation2.png");
		final byte[] content2 = Files.readAllBytes(resource2.getFile().toPath());
		final String encodedString2 = Base64.getEncoder().encodeToString(content2);

		final ClassPathResource resource3 = new ClassPathResource("knowledge/equation3.png");
		final byte[] content3 = Files.readAllBytes(resource3.getFile().toPath());
		final String encodedString3 = Base64.getEncoder().encodeToString(content3);

		final String payload = "{\"images\": [" +
				"\"" + encodedString1 + "\"," +
				"\"" + encodedString2 + "\"," +
				"\"" + encodedString3 + "\"],\"model\": \"regnet\"}";

		final MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/base64-equations-to-latex")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload)
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		final String latex = res.getResponse().getContentAsString();
		log.info(latex);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void variableExtractionTests() throws Exception {

		DocumentAsset documentAsset = new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description")
				.setText("x = 0. y = 1. I = Infected population.");

		documentAsset = documentAssetService.createAsset(documentAsset);

		final MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/variable-extractions")
				.contentType(MediaType.APPLICATION_JSON)
				.param("document-id", documentAsset.getId().toString())
				.param("annotate-skema", "true")
				.param("annotate-mit", "true")
				.param("domain", "epi")
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		final DocumentAsset document = objectMapper.readValue(res.getResponse().getContentAsString(),
				DocumentAsset.class);

		Assertions.assertTrue(document.getMetadata() != null);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void linkAmrTests() throws Exception {

		DocumentAsset documentAsset = new DocumentAsset()
				.setName("test-document-name")
				.setDescription("my description")
				.setText("x = 0. y = 1. I = Infected population.");

		documentAsset = documentAssetService.createAsset(documentAsset);

		mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/variable-extractions")
				.contentType(MediaType.APPLICATION_JSON)
				.param("document-id", documentAsset.getId().toString())
				.param("annotate-skema", "true")
				.param("annotate-mit", "true")
				.param("domain", "epi")
				.with(csrf()))
				.andExpect(status().isOk());

		final ClassPathResource resource = new ClassPathResource("knowledge/sir.json");
		final byte[] content = Files.readAllBytes(resource.getFile().toPath());
		Model model = objectMapper.readValue(content, Model.class);

		model = modelService.createAsset(model);

		final MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/link-amr")
				.contentType(MediaType.APPLICATION_JSON)
				.param("document-id", documentAsset.getId().toString())
				.param("model-id", model.getId().toString())
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		model = objectMapper.readValue(res.getResponse().getContentAsString(),
				Model.class);

		Assertions.assertTrue(model != null);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void cosmosPdfExtraction() throws Exception {

		final ClassPathResource resource = new ClassPathResource("knowledge/paper.pdf");
		final byte[] content = Files.readAllBytes(resource.getFile().toPath());

		final HttpEntity pdfFileEntity = new ByteArrayEntity(content, ContentType.create("application/pdf"));

		DocumentAsset documentAsset = new DocumentAsset()
				.setName("test-pdf-name")
				.setDescription("my description")
				.setFileNames(List.of("paper.pdf"));

		documentAsset = documentAssetService.createAsset(documentAsset);

		documentAssetService.uploadFile(documentAsset.getId(), "paper.pdf", pdfFileEntity);

		mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/pdf-to-cosmos")
				.contentType(MediaType.APPLICATION_JSON)
				.param("document-id", documentAsset.getId().toString())
				.with(csrf()))
				.andExpect(status().isOk());

		documentAsset = documentAssetService.getAsset(documentAsset.getId()).orElseThrow();

		Assertions.assertTrue(documentAsset.getAssets().size() > 0);
	}
}
