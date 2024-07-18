package software.uncharted.terarium.hmiserver.controller.evaluation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.models.EventType;
import software.uncharted.terarium.hmiserver.models.evaluation.EvaluationScenarioStatus;
import software.uncharted.terarium.hmiserver.models.evaluation.EvaluationScenarioSummary;
import software.uncharted.terarium.hmiserver.models.user.Event;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.EventService;

@RequestMapping("/evaluation")
@RestController
@Slf4j
@RequiredArgsConstructor
public class EvaluationController {

	private final ObjectMapper mapper;

	private final EventService eventService;

	private final CurrentUserService currentUserService;

	/**
	 * Get a list of all evaluation scenarios
	 *
	 * @return A list of all evaluation scenarios
	 */
	@GetMapping("/scenarios")
	@Secured(Roles.USER)
	public ResponseEntity<List<EvaluationScenarioSummary>> getScenarios() {
		final Map<String, Map<String, EvaluationScenarioSummary>> usernameToScenarioNameToSummary = new HashMap<>();

		// Find the first event for each summary
		final List<Event> events = eventService.findEvents(
			EventType.EVALUATION_SCENARIO,
			null,
			currentUserService.get().getId(),
			null,
			1000
		);
		events.forEach(event -> {
			try {
				final JsonNode value = mapper.readValue(event.getValue(), JsonNode.class);
				final String userId = event.getUserId();
				final String scenarioName = value.at("/name").asText();
				final Long timestampMillis = event.getTimestampMillis();
				final Map<String, EvaluationScenarioSummary> scenarioNameToSummary =
					usernameToScenarioNameToSummary.getOrDefault(userId, new HashMap<>());
				final EvaluationScenarioSummary summary = scenarioNameToSummary.getOrDefault(scenarioName, null);

				// If this event is earlier than the current one, store it
				if (summary == null || summary.getTimestampMillis() < timestampMillis) {
					scenarioNameToSummary.put(
						scenarioName,
						new EvaluationScenarioSummary()
							.setName(scenarioName)
							.setTask(value.at("/task").asText())
							.setDescription(value.at("/description").asText())
							.setNotes(value.at("/notes").asText())
							.setMultipleUsers(value.at("/multipleUsers").asBoolean())
							.setTimestampMillis(timestampMillis)
							.setUserId(userId)
					);
				}
				usernameToScenarioNameToSummary.put(userId, scenarioNameToSummary);
			} catch (final JsonProcessingException e) {
				log.error("Error parsing event value", e);
			}
		});

		// Populate the final model
		final List<EvaluationScenarioSummary> scenarioSummaries = new ArrayList<>();
		usernameToScenarioNameToSummary.forEach((username, scenarioNameToSummary) -> {
			scenarioSummaries.addAll(scenarioNameToSummary.values());
		});
		return ResponseEntity.ok(scenarioSummaries);
	}

	/**
	 * Get the status of the given scenario
	 *
	 * @param name The name of the scenario
	 * @return The status of the scenario
	 */
	@GetMapping("/status")
	@Secured(Roles.USER)
	public ResponseEntity<String> getStatus(@RequestParam("name") final String name) {
		final List<Event> events = eventService.findEvents(
			EventType.EVALUATION_SCENARIO,
			null,
			currentUserService.get().getId(),
			null,
			1000
		);
		final Event latestEvent = events
			.stream()
			.filter(event -> {
				try {
					final JsonNode value = mapper.readValue(event.getValue(), JsonNode.class);
					return value.at("/name").asText().equals(name);
				} catch (final JsonProcessingException e) {
					log.error("Error parsing event value", e);
				}
				return false;
			})
			.findFirst()
			.orElse(null);

		if (latestEvent != null) {
			try {
				final JsonNode value = mapper.readValue(latestEvent.getValue(), JsonNode.class);
				return ResponseEntity.ok(value.at("/action").asText());
			} catch (final JsonProcessingException e) {
				log.error("Error parsing event value", e);
			}
		}
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/runtime")
	@Secured(Roles.USER)
	public ResponseEntity<Long> getRuntime(@RequestParam("name") final String name) {
		final List<Event> events = eventService.findEvents(
			EventType.EVALUATION_SCENARIO,
			null,
			currentUserService.get().getId(),
			null,
			1000
		);
		final List<Event> scenarioEvents = events
			.stream()
			.filter(event -> {
				try {
					final JsonNode value = mapper.readValue(event.getValue(), JsonNode.class);
					return value.at("/name").asText().equals(name);
				} catch (final JsonProcessingException e) {
					log.error("Error parsing event value", e);
				}
				return false;
			})
			.toList();

		if (scenarioEvents.size() == 1) {
			return ResponseEntity.ok(Instant.now().toEpochMilli() - scenarioEvents.get(0).getTimestampMillis());
		}

		long runtime = 0L;
		for (int i = 0; i < scenarioEvents.size() - 1; i++) {
			final Event currentEvent = scenarioEvents.get(i);
			final Event nextEvent = scenarioEvents.get(i + 1);

			try {
				final JsonNode currentValue = mapper.readValue(currentEvent.getValue(), JsonNode.class);
				if (
					currentValue.at("/action").asText().equals("started") || currentValue.at("/action").asText().equals("resumed")
				) {
					runtime += nextEvent.getTimestampMillis() - currentEvent.getTimestampMillis();
				}
			} catch (final JsonProcessingException e) {
				log.error("Error parsing event value", e);
			}
		}
		return ResponseEntity.ok(runtime);
	}

	@GetMapping("/download")
	@Secured(Roles.USER)
	public ResponseEntity<String> getCSV(
		@RequestParam("userId") final String userId,
		@RequestParam("name") final String name
	) throws IOException {
		final List<Event> events = eventService.findAllByUserId(userId);

		// Find the list of timeranges for the scenario
		final List<Event> scenarioEvents = events
			.stream()
			.filter(event -> event.getType() == EventType.EVALUATION_SCENARIO)
			.filter(event -> {
				try {
					final JsonNode value = mapper.readValue(event.getValue(), JsonNode.class);
					return value.at("/name").asText().equals(name);
				} catch (final JsonProcessingException e) {
					log.error("Error parsing event value", e);
				}
				return false;
			})
			.toList();
		final List<Range> ranges = getRangesForScenario(scenarioEvents);

		final List<Event> filteredEvents = events
			.stream()
			.filter(event -> ranges.stream().anyMatch(r -> r.inRange(event.getTimestampMillis())))
			.toList();

		final List<String> headers = new ArrayList<>(Arrays.asList("timestamp", "projectId", "userId", "type", "value"));

		// Iterate through the events and calculate the top level field access for the
		// value type in jackson format
		final Set<String> topLevelFields = new HashSet<>();
		filteredEvents.forEach(event -> {
			if (event == null || event.getValue() == null) {
				return;
			}

			final JsonNode node;
			try {
				node = mapper.readTree(event.getValue());
				node.fieldNames().forEachRemaining(s -> topLevelFields.add("/" + s));
			} catch (final JsonProcessingException e) {
				log.error("Error parsing event value", e);
			}
		});
		headers.addAll(topLevelFields);

		final StringWriter sw = new StringWriter();
		final CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setHeader(headers.toArray(new String[0])).build();

		// Write the CSV, filtering out events that occurred outside of the scenario
		// runtime
		try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
			events
				.stream()
				.filter(event -> ranges.stream().anyMatch(r -> r.inRange(event.getTimestampMillis())))
				.forEach(event -> {
					try {
						final List<String> values = new ArrayList<>();
						values.add(event.getTimestampMillis().toString());
						values.add(event.getProjectId() != null ? event.getProjectId().toString() : "");
						values.add(event.getUserId());
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
						} catch (final Exception ignored) {
							for (int i = 5; i < headers.size(); i++) {
								values.add("");
							}
						}
						printer.printRecord(values);
					} catch (final IOException e) {
						log.error("Error writing event to CSV", e);
					}
				});
		}
		return ResponseEntity.ok(sw.toString());
	}

	@Accessors(chain = true)
	@NoArgsConstructor
	@Data
	private static class Range {

		private long start;
		private long end;

		public boolean inRange(final long timestamp) {
			return timestamp >= start && timestamp <= end;
		}
	}

	/**
	 * Gets a list of start/end timestamps for which the scenario was not paused. We use this to filter out events that
	 * occurred outside of the scenario runtime.
	 *
	 * @param scenarioEvents All events for the scenario of type EVALUATION_SCENARIO
	 * @return A list of ranges for which the scenario was not paused
	 */
	private List<Range> getRangesForScenario(final List<Event> scenarioEvents) {
		final List<Range> ranges = new ArrayList<>();
		for (int i = 0; i < scenarioEvents.size() - 1; i++) {
			final Event currentEvent = scenarioEvents.get(i);
			final Event nextEvent = scenarioEvents.get(i + 1);

			try {
				final JsonNode currentValue = mapper.readValue(currentEvent.getValue(), JsonNode.class);
				if (currentValue.at("/action").asText().equals(EvaluationScenarioStatus.STARTED.toString())) {
					ranges.add(new Range().setStart(currentEvent.getTimestampMillis()).setEnd(nextEvent.getTimestampMillis()));
				}
			} catch (final JsonProcessingException e) {
				log.error("Error parsing event value", e);
			}
		}
		return ranges;
	}
}
