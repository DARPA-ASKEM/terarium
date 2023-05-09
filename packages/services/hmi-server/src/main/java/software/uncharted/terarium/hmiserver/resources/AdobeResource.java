package software.uncharted.terarium.hmiserver.resources;

import io.quarkus.security.Authenticated;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/api/adobe")

@Tag(name = "Adobe REST Endpoints")
@Slf4j
public class AdobeResource {
	@ConfigProperty(name = "adobe.api_key")
	Optional<String> key;

	@GET
	public Response getKey() {
		if (key.isPresent()) {
			return Response.ok(key.get())
				.build();
		} else {
			log.error("No Adobe API key is configured");
			return Response.serverError().build();
		}
	}
}
