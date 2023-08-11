package software.uncharted.terarium.hmiserver.resources.evaluation;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.identity.SecurityIdentity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import software.uncharted.terarium.hmiserver.entities.Event;
import software.uncharted.terarium.hmiserver.models.EvaluationScenarioStatus;
import software.uncharted.terarium.hmiserver.models.EvaluationScenarioSummary;
import software.uncharted.terarium.hmiserver.models.EventType;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Evaluation Scenarios REST Endpoint")
@Path("/api/evaluation")
@Slf4j
public class EvaluationResource {
	@Inject
	SecurityIdentity securityIdentity;

	@Inject
	ObjectMapper mapper;

	/**
	 * Get a list of all evaluation scenarios
	 * @return	A list of all evaluation scenarios
	 */
	@GET
	@Path("/scenarios")
	public List<EvaluationScenarioSummary> getScenarios() {
		Map<String, Map<String, EvaluationScenarioSummary>> usernameToScenarioNameToSummary = new HashMap<>();

		// Find the first event for each summary
		final List<Event> events = Event.find("type = ?1", Sort.descending("timestampmillis"), EventType.EVALUATION_SCENARIO).list();
		events.forEach(event -> {
			try {
				final JsonNode value = mapper.readValue(event.getValue(), JsonNode.class);
				final String username = event.getUsername();
				final String scenarioName = value.at("/name").asText();
				final Long timestampMillis = event.getTimestampMillis();
				final Map<String, EvaluationScenarioSummary> scenarioNameToSummary = usernameToScenarioNameToSummary.getOrDefault(username, new HashMap<>());
				final EvaluationScenarioSummary summary = scenarioNameToSummary.getOrDefault(scenarioName, null);

				// If this event is earlier than the current one, store it
				if (summary == null || summary.getTimestampMillis() < timestampMillis) {
					scenarioNameToSummary.put(scenarioName, new EvaluationScenarioSummary()
						.setName(scenarioName)
						.setTask(value.at("/task").asText())
						.setDescription(value.at("/description").asText())
						.setNotes(value.at("/notes").asText())
						.setTimestampMillis(timestampMillis)
						.setUsername(username));
				}
				usernameToScenarioNameToSummary.put(username, scenarioNameToSummary);
			} catch (JsonProcessingException e) {
				log.error("Error parsing event value", e);
			}
		});

		// Populate the final model
		List<EvaluationScenarioSummary> scenarioSummaries = new ArrayList<>();
		usernameToScenarioNameToSummary.forEach((username, scenarioNameToSummary) -> {
			scenarioSummaries.addAll(scenarioNameToSummary.values());
		});
		return scenarioSummaries;
	}

	/**
	 * Get the status of the given scenario
	 * @param name
	 * @return
	 */
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

	@GET
	@Path("/runtime")
	public Long getRuntime(@QueryParam("name") String name) {
		final String username = securityIdentity.getPrincipal().getName();
		final List<Event> events = Event.find("type = ?1 and username = ?2", Sort.ascending("timestampmillis"), EventType.EVALUATION_SCENARIO, username).list();
		final List<Event> scenarioEvents = events
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
			.toList();

		if (scenarioEvents.size() == 1) {
			return Instant.now().toEpochMilli() - scenarioEvents.get(0).getTimestampMillis();
		}

		long runtime = 0L;
		for (int i = 0; i < scenarioEvents.size() - 1; i++) {
			final Event currentEvent = scenarioEvents.get(i);
			final Event nextEvent = scenarioEvents.get(i+1);

			try {
				final JsonNode currentValue = mapper.readValue(currentEvent.getValue(), JsonNode.class);
				if (currentValue.at("/action").asText().equals("started") || currentValue.at("/action").asText().equals("resumed")) {
					runtime += nextEvent.getTimestampMillis() - currentEvent.getTimestampMillis();
				}
			} catch (JsonProcessingException e) {
				log.error("Error parsing event value", e);
			}
		}
		return runtime;
	}

	@GET
	@Path("/download")
	public String getCSV(@QueryParam("username") final String username,
											 @QueryParam("name") final String name) throws IOException {

		final List<Event> events = Event.findAllByUsername(username);

		// Find the list of timeranges for the scenario
		final List<Event> scenarioEvents = events.stream()
			.filter(event -> event.getType() == EventType.EVALUATION_SCENARIO)
			.filter(event -> {
				try {
					final JsonNode value = mapper.readValue(event.getValue(), JsonNode.class);
					return value.at("/name").asText().equals(name);
				} catch (JsonProcessingException e) {
					log.error("Error parsing event value", e);
				}
				return false;
			})
			.toList();
		final List<Range> ranges = getRangesForScenario(scenarioEvents);

		final List<Event> filteredEvents = events.stream()
			.filter(event -> ranges.stream().anyMatch(r -> r.inRange(event.getTimestampMillis())))
			.toList();

		final List<String> headers = new ArrayList<>(Arrays.asList("timestamp", "projectId", "username", "type", "value"));

		// Iterate through the events and calculate the top level field access for the value type in jackson format
		final Set<String> topLevelFields = new HashSet<>();
		filteredEvents.forEach(event -> {
			if (event == null || event.getValue() == null) {
				return;
			}

			final JsonNode node;
			try {
				node = mapper.readTree(event.getValue());
				node.fieldNames().forEachRemaining(s -> topLevelFields.add("/" + s));
			} catch (JsonProcessingException e) {
				log.error("Error parsing event value", e);
			}

		});
		headers.addAll(topLevelFields);



		final String[] HEADERS = { "timestamp", "projectId", "username", "type", "value"};
		StringWriter sw = new StringWriter();
		CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
			.setHeader(headers.toArray(new String[0]))
			.build();

		// Write the CSV, filtering out events that occurred outside of the scenario runtime
		try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
			events.stream()
				.filter(event -> ranges.stream().anyMatch(r -> r.inRange(event.getTimestampMillis())))
				.forEach(event -> {
				try {
					final List<String> values = new ArrayList<>();
					values.add(event.getTimestampMillis().toString());
					values.add(event.getProjectId() != null ? event.getProjectId().toString() : "");
					values.add(event.getUsername());
					values.add(event.getType().toString());
					values.add(event.getValue());

					try {
						final JsonNode valueNode = mapper.readTree(event.getValue());
						for (int i = 5; i < headers.size(); i++) {
							final String header = headers.get(i);
							final JsonNode node = valueNode.at(header);
							if (node.isMissingNode()) {
								values.add("");
							} else {
								values.add(node.asText());
							}
						}
					} catch (Exception ignored) {
						for (int i = 5; i < headers.size(); i++) {
							values.add("");
						}
					}
					printer.printRecord(values);
				} catch (IOException e) {
					log.error("Error writing event to CSV", e);
				}
			});
		}
		return sw.toString();
	}

	@Accessors(chain = true)
	@NoArgsConstructor
	@Data
	private static class Range {
		private long start;
		private long end;

		public boolean inRange(long timestamp) {
			return timestamp >= start && timestamp <= end;
		}
	}

	/**
	 * Gets a list of start/end timestamps for which the scenario was not paused. We use this to filter out events that
	 * occurred outside of the scenario runtime.
	 * @param scenarioEvents	All events for the scenario of type EVALUATION_SCENARIO
	 * @return								A list of ranges for which the scenario was not paused
	 */
	private List<Range> getRangesForScenario(final List<Event> scenarioEvents) {
		final List<Range> ranges = new ArrayList<>();
		for (int i = 0; i < scenarioEvents.size() - 1; i++) {
			final Event currentEvent = scenarioEvents.get(i);
			final Event nextEvent = scenarioEvents.get(i+1);

			try {
				final JsonNode currentValue = mapper.readValue(currentEvent.getValue(), JsonNode.class);
				if (currentValue.at("/action").asText().equals(EvaluationScenarioStatus.STARTED.toString()) || currentValue.at("/action").asText().equals(EvaluationScenarioStatus.RESUMED.toString())) {
					ranges.add(new Range().setStart(currentEvent.getTimestampMillis()).setEnd(nextEvent.getTimestampMillis()));
				}
			} catch (JsonProcessingException e) {
				log.error("Error parsing event value", e);
			}
		}
		return ranges;
	}
}
