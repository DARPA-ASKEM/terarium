package software.uncharted.terarium.hmiserver.controller.xdd;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    @WithMockUser(username = "ursula", authorities = { Roles.USER })
    public void canStandardDocSearch() throws Exception {

        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/documents")
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

        final ObjectMapper m = new ObjectMapper();
        XDDResponse<DocumentsResponseOK> res = null;
        try {
            res = m.readValue(
                    result.getResponse().getContentAsByteArray(),
                    new TypeReference<XDDResponse<DocumentsResponseOK>>() {
                    });

        } catch (final Exception e) {
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
    @WithMockUser(username = "ursula", authorities = { Roles.USER })
    public void canSearchWithDateFacet() throws Exception {

        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/documents")
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

        final ObjectMapper m = new ObjectMapper();
        XDDResponse<DocumentsResponseOK> res = null;
        try {
            res = m.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {
            });

        } catch (final Exception e) {
            Assertions.fail("Unable to parse response", e);
        }

        Assertions.assertNotNull(res);
        Assertions.assertFalse(res.getSuccess().getData().isEmpty());

        for (final Document doc : res.getSuccess().getData()) {
            try {
                final int year = Integer.parseInt(doc.getYear());
                Assertions.assertEquals(2022, year);
            } catch (final Exception e) {
                Assertions.fail("Unable to parse year", e);
            }
        }
    }
}
