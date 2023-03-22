package software.uncharted.terarium.hmiserver.proxies;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.uncharted.terarium.hmiserver.proxies.documentservice.DocumentProxy;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.DocumentsResponseOK;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDDictionariesResponseOK;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDResponse;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDSetsResponse;

@QuarkusTest
public class DocumentProxyTests {

	@RestClient
	DocumentProxy documentProxy;

	@Test
	public void testItCanFetchSets() {
		final XDDSetsResponse response = documentProxy.getAvailableSets();

		Assertions.assertNotNull(response);
		Assertions.assertNotNull(response.getDescription());
		Assertions.assertNotNull(response.getAvailableSets());
		Assertions.assertTrue(response.getAvailableSets().size() > 0);
	}

	@Test
	public void testItCanGetAllAvailableDictionaries() {
		final XDDResponse<XDDDictionariesResponseOK> response = documentProxy.getAvailableDictionaries("");

		Assertions.assertNotNull(response);
		Assertions.assertNull(response.getError());
		Assertions.assertNotNull(response.getSuccess());
		Assertions.assertNotNull(response.getSuccess().getData());
		Assertions.assertTrue(response.getSuccess().getData().size() > 0);
	}

	@Test
	public void testItCanGetADocumentById() {
		final String TARGET_ID = "607182f63f2ac7e701921c92";
		final XDDResponse<DocumentsResponseOK> response = documentProxy.getDocuments(
			TARGET_ID, null, null, null, null, null, null, null, null, null, null, null, null,
			null, null, null, null, null, null, "url_extractions,summaries", null);

		Assertions.assertNotNull(response);
		Assertions.assertNull(response.getError());
		Assertions.assertNotNull(response.getSuccess());
		Assertions.assertNotNull(response.getSuccess().getData());
		Assertions.assertEquals(1, response.getSuccess().getData().size());
		Assertions.assertEquals(TARGET_ID, response.getSuccess().getData().get(0).getGddId());
	}

	@Test
	public void testItCanSearchForADocByTerm() {
		final XDDResponse<DocumentsResponseOK> response = documentProxy.getDocuments(
			null, null, null, "COVID-19", "xdd-covid-19", "true", "true", null, null, "100", "2", null, "true",
			null, null, null, null, "title,abstract", "true", "url_extractions", null);

		Assertions.assertNotNull(response);
		Assertions.assertNull(response.getError());
		Assertions.assertNotNull(response.getSuccess());
		Assertions.assertNotNull(response.getSuccess().getData());
		Assertions.assertTrue(response.getSuccess().getData().size() > 0);

		Assertions.assertNotNull(response.getSuccess().getData().get(0).getHighlight());
		Assertions.assertTrue(response.getSuccess().getData().get(0).getHighlight().size() > 0);

		Assertions.assertNotNull(response.getSuccess().getData().get(0).getAbstractText());
		Assertions.assertTrue(response.getSuccess().getData().get(0).getAbstractText().length() > 0);

		Assertions.assertNotNull(response.getSuccess().getData().get(0).getTitle());
		Assertions.assertTrue(response.getSuccess().getData().get(0).getTitle().length() > 0);

		Assertions.assertNotNull(response.getSuccess().getFacets());
		Assertions.assertTrue(response.getSuccess().getFacets().size() > 0);
	}

	@Test
	public void testItCanSearchForGithubURL() {
		final XDDResponse<DocumentsResponseOK> response = documentProxy.getDocuments(
			null, null, null, null, null, null, null, null, null, "100", "2", null, "true",
			null, null, null, null, null, null, null, "https://github.com/ieee8023/covid-chestxray-dataset");

		Assertions.assertNotNull(response);
		Assertions.assertNull(response.getError());
		Assertions.assertNotNull(response.getSuccess());
		Assertions.assertNotNull(response.getSuccess().getData());
		Assertions.assertTrue(response.getSuccess().getData().size() > 0);

		Assertions.assertNotNull(response.getSuccess().getData().get(0).getHighlight());
		Assertions.assertTrue(response.getSuccess().getData().get(0).getHighlight().size() > 0);

		Assertions.assertNotNull(response.getSuccess().getData().get(0).getTitle());
		Assertions.assertTrue(response.getSuccess().getData().get(0).getTitle().length() > 0);

		Assertions.assertNotNull(response.getSuccess().getFacets());
		Assertions.assertTrue(response.getSuccess().getFacets().size() > 0);
	}
}
