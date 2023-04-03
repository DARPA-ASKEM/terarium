package software.uncharted.terarium.hmiserver.proxies;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.uncharted.terarium.hmiserver.models.documentservice.autocomplete.AutoComplete;
import software.uncharted.terarium.hmiserver.proxies.documentservice.ExtractionProxy;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDExtractionsResponseOK;
import software.uncharted.terarium.hmiserver.resources.documentservice.responses.XDDResponse;

@QuarkusTest
public class ExtractionProxyTests {


	@ConfigProperty(name = "xdd_api_key", defaultValue = "")
	String key;

	@RestClient
	ExtractionProxy proxy;

	@Test
	public void testItCanGetExtractions() {
		XDDResponse<XDDExtractionsResponseOK> response = proxy.getExtractions(null, "covid", null, null, "true", key);

		Assertions.assertNotNull(response);
		Assertions.assertNull(response.getError());
		Assertions.assertNotNull(response.getSuccess().getData());
		Assertions.assertTrue(response.getSuccess().getData().size() > 0);
	}

	@Test
	public void testItCanGetAutocomplete() {
		final AutoComplete response = proxy.getAutocomplete("cov");

		Assertions.assertNotNull(response);
		Assertions.assertNotNull(response.getSuggest());
		Assertions.assertNotNull(response.getSuggest().getEntitySuggestFuzzy());
		Assertions.assertTrue(response.getSuggest().getEntitySuggestFuzzy().size() > 0);
	}
}
