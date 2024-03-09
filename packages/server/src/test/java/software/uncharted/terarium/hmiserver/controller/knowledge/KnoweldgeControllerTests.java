package software.uncharted.terarium.hmiserver.controller.knowledge;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;

import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class KnoweldgeControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

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

		UUID regnetModelId = UUID.fromString(res.getResponse().getContentAsString());
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

		regnetModelId = UUID.fromString(res.getResponse().getContentAsString());
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

		UUID petrinetModelId = UUID.fromString(res.getResponse().getContentAsString());
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

		petrinetModelId = UUID.fromString(res.getResponse().getContentAsString());
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
		log.info(latex.toString());
	}

}
