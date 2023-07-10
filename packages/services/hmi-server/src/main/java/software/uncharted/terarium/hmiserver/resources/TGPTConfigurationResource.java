package software.uncharted.terarium.hmiserver.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Path("/api/tgpt")
@Tag(name = "TGPT REST Endpoints")
@Slf4j
public class TGPTConfigurationResource {
	@ConfigProperty(name = "tgpt.client_key")
	String token;

	@ConfigProperty(name = "tgpt.base_url", defaultValue = "/chatty/")
	private String baseUrl;

	@ConfigProperty(name = "tgpt.app_url", defaultValue = "http://localhost:8078/chatty/")
	private String appUrl;

	@ConfigProperty(name = "tgpt.ws_url", defaultValue = "ws://localhost:8078/chatty_ws/")
	private String wsUrl;

	@Inject
	ObjectMapper mapper;

	@GET
	public Response getConfiguration() {
		final ObjectNode node = mapper.createObjectNode();
		node.put("token", token);
		node.put("baseUrl", baseUrl);
		node.put("appUrl", appUrl);
		node.put("wsUrl", wsUrl);

		return Response.ok(node)
			.build();
	}
}
