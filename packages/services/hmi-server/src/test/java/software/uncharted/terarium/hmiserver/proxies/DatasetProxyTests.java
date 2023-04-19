package software.uncharted.terarium.hmiserver.proxies;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.uncharted.terarium.hmiserver.proxies.dataservice.DatasetProxy;
import javax.ws.rs.core.Response;

@QuarkusTest
public class DatasetProxyTests {

	@RestClient
	DatasetProxy datasetProxy;
	
	@Test
	public void testItCanFetchGetCsv() {
		final Response response = datasetProxy.getCsv("1",true,false,50);

		Assertions.assertNotNull(response);
		Assertions.assertNotNull(response.hasEntity());
		Assertions.assertTrue(response.getStatus() == 200);
	}

}
