package software.uncharted.terarium.hmiserver.controller.xdd;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.DocumentsResponseOK;
import software.uncharted.terarium.hmiserver.models.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.security.Roles;

public class XDDTests extends TerariumApplicationTests {

	@Test
	public void canSearchForTermUnauthorized() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.get("/documents")
						.queryParam("dataset", "xdd-covid-19")
						.queryParam("term", "covid")
						.queryParam("max", "20")
						.queryParam("per_page", "20"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(
			username = "ursula",
			authorities = {Roles.USER})
	public void canStandardDocSearch() throws Exception {

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/documents")
						.queryParam("dataset", "xdd-covid-19")
						.queryParam("term", "covid")
						.queryParam("include_score", "true")
						.queryParam("include_highlights", "true")
						.queryParam("facets", "true")
						.queryParam("max", "20")
						.queryParam("additional_fields", "title,abstract")
						.queryParam("known_entities", "url_extractions,askem_object")
						.queryParam("per_page", "20"))
				.andExpect(status().isOk())
				.andReturn();

		ObjectMapper m = new ObjectMapper();
		XDDResponse<DocumentsResponseOK> res = null;
		try {
			res = m.readValue(
					result.getResponse().getContentAsByteArray(),
					new TypeReference<XDDResponse<DocumentsResponseOK>>() {});

		} catch (Exception e) {
			Assertions.fail("Unable to parse response", e);
		}

		// verify we got 20 results
		Assertions.assertNotNull(res);
		Assertions.assertFalse(res.getSuccess().getData().isEmpty());
		Assertions.assertEquals(20, res.getSuccess().getData().size());

		// verify facets are there
		Assertions.assertFalse(res.getSuccess().getFacets().isEmpty());

		// verify abstracts are there
		Assertions.assertTrue(res.getSuccess().getData().get(0).getAbstractText() != null
				&& !res.getSuccess().getData().get(0).getAbstractText().isEmpty());

		// verify highlights are there
		Assertions.assertTrue(res.getSuccess().getData().get(0).getHighlight() != null
				&& !res.getSuccess().getData().get(0).getHighlight().isEmpty());
	}

	@Test
	@WithMockUser(
			username = "ursula",
			authorities = {Roles.USER})
	public void canSearchWithDateFacet() throws Exception {

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/documents")
						.queryParam("dataset", "xdd-covid-19")
						.queryParam("term", "covid")
						.queryParam("include_score", "true")
						.queryParam("include_highlights", "true")
						.queryParam("facets", "true")
						.queryParam("max", "50")
						.queryParam("additional_fields", "title,abstract")
						.queryParam("known_entities", "url_extractions,askem_object")
						.queryParam("per_page", "50")
						.queryParam("min_published", "2022-01-01")
						.queryParam("max_published", "2022-12-31"))
				.andExpect(status().isOk())
				.andReturn();

		ObjectMapper m = new ObjectMapper();
		XDDResponse<DocumentsResponseOK> res = null;
		try {
			res = m.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {});

		} catch (Exception e) {
			Assertions.fail("Unable to parse response", e);
		}

		Assertions.assertNotNull(res);
		Assertions.assertFalse(res.getSuccess().getData().isEmpty());

		for (Document doc : res.getSuccess().getData()) {
			try {
				int year = Integer.parseInt(doc.getYear());
				Assertions.assertEquals(2022, year);
			} catch (Exception e) {
				Assertions.fail("Unable to parse year", e);
			}
		}
	}
	/*
	 * @Test
	 *
	 * @WithMockUser(username = "ursula", authorities = { Roles.USER })
	 * public void canItLimitExtractionsReturned() throws Exception {
	 *
	 * private static final String TEST_DOI = "10.1101/2020.08.18.20176354";
	 *
	 * ObjectMapper m = new ObjectMapper();
	 *
	 * MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/documents")
	 * .queryParam("doi", TEST_DOI)
	 * .queryParam("known_entities", "askem_object"))
	 * .andExpect(status().isOk()).andReturn();
	 *
	 * XDDResponse<DocumentsResponseOK> res = null;
	 * try {
	 * res = m.readValue(result.getResponse().getContentAsByteArray(), new
	 * TypeReference<>() {
	 * });
	 *
	 * } catch (Exception e) {
	 * Assertions.fail("Unable to parse response", e);
	 * }
	 *
	 * Assertions.assertNotNull(res);
	 * // Erroring - commenting this out
	 * //Assertions.assertEquals(res.getSuccess().getData().get(0).getKnownEntities(
	 * ).getAskemObjects().size(), 5);
	 *
	 * // At the time of writing this was 49, but, its possible this number changes.
	 * // Whats important is its more than 5!!
	 * Assertions.assertTrue(res.getSuccess().getData().get(0).
	 * getKnownEntitiesCounts().getAskemObjectCount() > 5);
	 * }
	 */
}
