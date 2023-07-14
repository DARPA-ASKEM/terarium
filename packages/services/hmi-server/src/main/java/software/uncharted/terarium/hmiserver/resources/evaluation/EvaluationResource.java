package software.uncharted.terarium.hmiserver.resources.evaluation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import software.uncharted.terarium.hmiserver.entities.Event;
import software.uncharted.terarium.hmiserver.models.EventType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Tag(name = "Evaluation Scenarios REST Endpoint")
@Path("/api/evaluation")
@Slf4j
public class EvaluationResource {
	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	ObjectMapper mapper;

	@GET
	@Path("/status")
	public String getStatus(@QueryParam("name") String name) {
		final String username = securityIdentity.getPrincipal().getName();
		final List<Event> events = Event.find("type = ?1 and username = ?2", Sort.descending("timestampmillis"), EventType.EVALUATION_SCENARIO, username).list();
		final Event latestEvent = events
			.stream()
			.filter(event -> {
				try {
					final JsonNode value = mapper.readValue(event.getValue(), JsonNode.class);
					return value.at("/name").asText().equals(name);
				} catch (JsonProcessingException e) {
					log.error("Error parsing event value", e);
				}
				return false;
			})
			.findFirst().orElse(null);

		if (latestEvent != null) {
			try {
				final JsonNode value = mapper.readValue(latestEvent.getValue(), JsonNode.class);
				return value.at("/action").asText();
			} catch (JsonProcessingException e) {
				log.error("Error parsing event value", e);
			}
		}
		return null;
	}
}
