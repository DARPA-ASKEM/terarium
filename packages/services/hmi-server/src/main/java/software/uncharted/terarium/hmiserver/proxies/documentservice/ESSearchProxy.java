package software.uncharted.terarium.hmiserver.proxies.documentservice;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import software.uncharted.terarium.hmiserver.exceptions.HmiResponseExceptionMapper;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * This is a temporary proxy as currently the ES endpoint is just on xDD's development
 * server. Eventually we will want to move this to DocumentProxy
 */
@RegisterRestClient(configKey="xdd-dev-service")
@Produces(MediaType.APPLICATION_JSON)
@RegisterProvider(HmiResponseExceptionMapper.class)
@Path("/api")

public interface ESSearchProxy {

	/**
	 * Perform an elastic search query to xDD
	 * @param apiKey header API key
	 * @param data json query
	 * @return query results
	 */
	@POST
	@Path("/es")
	Response search(@HeaderParam("x-api-key") final String apiKey, final String data);

}
