package software.uncharted.terarium.hmiserver.resources;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * This resource is unauthenticated and is used to provide any configuration to the client
 * that must occur before auth is setup.  This should _only_ be used for configuration that
 * is not a secret.
 */

@Path("/configuration")
@Tag(name = "Configuration REST Endpoints")
@Slf4j
public class ConfigurationResource {
	@ConfigProperty(name = "googleanalytics.id")
	Optional<String> googleAnalyticsId;

	@GET
	@Path("/ga")
	public Response getGA() {
		if (googleAnalyticsId.isPresent()) {
			return Response.ok(googleAnalyticsId.get())
				.build();
		} else {
			log.warn("No GA key is configured");
			return Response.ok("test").build();
		}
	}
}
