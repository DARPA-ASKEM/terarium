package software.uncharted.terarium.hmiserver.controller.knowledge;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.util.Base64;

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
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;

@Slf4j
public class KnoweldgeControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void equationsToModelRegNet() throws Exception {

		String payload1 = """
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

		Model amr = objectMapper.readValue(res.getResponse().getContentAsString(), Model.class);
		log.info(amr.toString());

		String payload2 = """
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

		amr = objectMapper.readValue(res.getResponse().getContentAsString(), Model.class);
		log.info(amr.toString());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void equationsToModelPetrinet() throws Exception {

		String payload1 = """
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

		Model amr = objectMapper.readValue(res.getResponse().getContentAsString(), Model.class);
		log.info(amr.toString());

		String payload2 = """
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

		amr = objectMapper.readValue(res.getResponse().getContentAsString(), Model.class);
		log.info(amr.toString());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void base64EquationsToAMRTests() throws Exception {

		ClassPathResource resource = new ClassPathResource("knowledge/equation.png");
		byte[] content = Files.readAllBytes(resource.getFile().toPath());
		String encodedString = Base64.getEncoder().encodeToString(content);

		String payload = "{\"images\": [\"" + encodedString + "\"],\"model\": \"regnet\"}";

		MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/base64-equations-to-model")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload)
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		Model amr = objectMapper.readValue(res.getResponse().getContentAsString(), Model.class);
		log.info(amr.toString());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void base64EquationsToLatexTests() throws Exception {

		ClassPathResource resource = new ClassPathResource("knowledge/equation.png");
		byte[] content = Files.readAllBytes(resource.getFile().toPath());
		String encodedString = Base64.getEncoder().encodeToString(content);

		String payload = "{\"images\": [\"" + encodedString + "\"]}";

		MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/knowledge/base64-equations-to-latex")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload)
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		String latex = res.getResponse().getContentAsString();
		log.info(latex.toString());
	}

}
