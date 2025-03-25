package software.uncharted.terarium.hmiserver.service.tasks;

import static software.uncharted.terarium.hmiserver.utils.JsonToHTML.renderJsonToHTML;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.enrichment.Enrichment;
import software.uncharted.terarium.hmiserver.models.dataservice.enrichment.EnrichmentSource;
import software.uncharted.terarium.hmiserver.models.dataservice.enrichment.EnrichmentTarget;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelUnit;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Observable;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.State;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Transition;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.utils.JsonToHTML;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrichModelResponseHandler extends LlmTaskResponseHandler {

	public static final String NAME = "gollm:enrich_model";

	private final ObjectMapper objectMapper;
	private final ModelService modelService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input extends LlmTaskResponseHandler.Input {

		@JsonProperty("document")
		String document;

		@JsonProperty("amr")
		String amr;
	}

	@Data
	private static class Unit {

		String expression;
		String expressionMathml;
	}

	@Data
	private static class DescriptionsAndUnits {

		String id;
		String description;
		Unit units;
	}

	@Data
	private static class ContentAndExtractionItems {

		JsonNode content;
		String[] extractionItemIds;
	}

	@Data
	private static class Descriptions {

		String id;
		String description;
	}

	@Data
	public static class Response {

		JsonNode modelCard;
		List<ContentAndExtractionItems> states;
		List<ContentAndExtractionItems> parameters;
		List<ContentAndExtractionItems> observables;
		List<ContentAndExtractionItems> transitions;
	}

	@Data
	public static class Properties {

		UUID projectId;
		UUID documentId;
		UUID modelId;
		Boolean overwrite;
	}

	private List<Enrichment> modelEnrichmentJSONToEnrichmentsList(final Response response, final Properties props) {
		final List<Enrichment> enrichments = new ArrayList<>();

		// add model card enrichments to map

		final JsonNode modelCard = response.modelCard;
		// loop through keys of modelcard and add to enrichment map
		modelCard
			.fieldNames()
			.forEachRemaining(key -> {
				final Enrichment enrichment = new Enrichment();
				enrichment.setId(UUID.randomUUID());
				enrichment.setLabel(JsonToHTML.formatTitle(key));
				enrichment.setSource(EnrichmentSource.GOLLM);
				enrichment.setTarget(EnrichmentTarget.MODEL_CARD);
				enrichment.setContent(modelCard.get(key).get("content"));
				if (props.documentId != null) {
					enrichment.setExtractionAssetId(props.documentId);
					JsonNode sourcesNode = modelCard.get(key).get("extractionItemIds");
					if (sourcesNode != null && sourcesNode.isArray()) {
						String[] sources = new ObjectMapper().convertValue(sourcesNode, String[].class);
						enrichment.setExtractionItemIds(sources);
					}
				}
				enrichment.setIncluded(false);
				enrichments.add(enrichment);
			});

		// add states enrichments to map
		for (final ContentAndExtractionItems state : response.states) {
			final Enrichment enrichment = new Enrichment();
			enrichment.setId(UUID.randomUUID());
			enrichment.setLabel(state.getContent().get("id").asText());
			enrichment.setSource(EnrichmentSource.GOLLM);
			enrichment.setTarget(EnrichmentTarget.STATE);
			enrichment.setContent(state.getContent());
			if (props.documentId != null) {
				enrichment.setExtractionAssetId(props.documentId);
				enrichment.setExtractionItemIds(state.getExtractionItemIds());
			}
			enrichment.setIncluded(false);
			enrichments.add(enrichment);
		}

		// add parameters enrichments to map
		for (final ContentAndExtractionItems parameter : response.parameters) {
			final Enrichment enrichment = new Enrichment();
			enrichment.setId(UUID.randomUUID());
			enrichment.setLabel(parameter.getContent().get("id").asText());
			enrichment.setSource(EnrichmentSource.GOLLM);
			enrichment.setTarget(EnrichmentTarget.PARAMETER);
			enrichment.setContent(parameter.getContent());
			if (props.documentId != null) {
				enrichment.setExtractionAssetId(props.documentId);
				enrichment.setExtractionItemIds(parameter.getExtractionItemIds());
			}
			enrichment.setIncluded(false);
			enrichments.add(enrichment);
		}

		// add observables enrichments to map
		for (final ContentAndExtractionItems observable : response.observables) {
			final Enrichment enrichment = new Enrichment();
			enrichment.setId(UUID.randomUUID());
			enrichment.setLabel(observable.getContent().get("id").asText());
			enrichment.setSource(EnrichmentSource.GOLLM);
			enrichment.setTarget(EnrichmentTarget.OBSERVABLE);
			enrichment.setContent(observable.getContent());
			if (props.documentId != null) {
				enrichment.setExtractionAssetId(props.documentId);
				enrichment.setExtractionItemIds(observable.getExtractionItemIds());
			}
			enrichment.setIncluded(false);
			enrichments.add(enrichment);
		}

		// add transitions enrichments to map
		for (final ContentAndExtractionItems transition : response.transitions) {
			final Enrichment enrichment = new Enrichment();
			enrichment.setId(UUID.randomUUID());
			enrichment.setLabel(transition.getContent().get("id").asText());
			enrichment.setSource(EnrichmentSource.GOLLM);
			enrichment.setTarget(EnrichmentTarget.TRANSITION);
			enrichment.setContent(transition.getContent());
			if (props.documentId != null) {
				enrichment.setExtractionAssetId(props.documentId);
				enrichment.setExtractionItemIds(transition.getExtractionItemIds());
			}
			enrichment.setIncluded(false);
			enrichments.add(enrichment);
		}

		return enrichments;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final JsonNode node = objectMapper.readValue(resp.getOutput(), JsonNode.class);
			final Response response = objectMapper.treeToValue(node.get("response"), Response.class);

			final Model model = modelService.getAsset(props.getModelId()).orElseThrow();

			if (!props.overwrite) {
				final List<Enrichment> enrichments = modelEnrichmentJSONToEnrichmentsList(response, props);
				final ModelMetadata metadata = model.getMetadata();
				if (metadata == null) {
					model.setMetadata(new ModelMetadata());
					model.getMetadata().setEnrichments(enrichments);
				} else {
					metadata.setEnrichments(enrichments);
				}
				modelService.updateAsset(model, model.getId());
				return resp;
			}

			// FIXME: Updating the entire model maybe going away, so everything below this comment may be removed
			// Update the model card
			final JsonNode card = response.modelCard;
			if (model.getMetadata() == null) {
				model.setMetadata(new ModelMetadata());
			}
			model.getMetadata().setGollmCard(card);
			model.getMetadata().setDescription(renderJsonToHTML(card).getBytes(StandardCharsets.UTF_8));

			// Update the model with the enriched data
			for (final ContentAndExtractionItems state : response.states) {
				final String id = state.content.get("id").asText();
				final String description = state.content.get("description").asText();
				final JsonNode units = state.content.get("units");
				model
					.getInitials()
					.stream()
					.filter(initial -> initial.getTarget().equalsIgnoreCase(id))
					.findFirst()
					.ifPresent(initial -> {
						initial.setDescription(description);
						StreamSupport.stream(model.getModel().get("states").spliterator(), false)
							.filter(stateNode -> stateNode.path("id").asText().equals(id))
							.findFirst()
							.ifPresent(stateNode -> {
								((ObjectNode) stateNode).put("description", description);
								if (units != null) {
									((ObjectNode) stateNode).put("units", objectMapper.createObjectNode());
									((ObjectNode) stateNode.get("units")).put("expression", units.get("expression").asText());
									((ObjectNode) stateNode.get("units")).put(
											"expression_mathml",
											units.get("expressionMathml").asText()
										);
								}
							});
					});
			}

			for (final ContentAndExtractionItems parameter : response.parameters) {
				final String id = parameter.content.get("id").asText();
				final String description = parameter.content.get("description").asText();
				final JsonNode units = parameter.content.get("units");
				model
					.getParameters()
					.stream()
					.filter(param -> param.getConceptReference().equalsIgnoreCase(id))
					.findFirst()
					.ifPresent(param -> {
						param.setDescription(description);
						if (units != null) {
							param.setUnits(
								new ModelUnit()
									.setExpression(units.get("expression").asText())
									.setExpressionMathml(units.get("expressionMathml").asText())
							);
						}
					});
			}

			for (final ContentAndExtractionItems observable : response.observables) {
				final String id = observable.content.get("id").asText();
				final String description = observable.content.get("description").asText();
				final JsonNode units = observable.content.get("units");
				model
					.getObservables()
					.stream()
					.filter(observe -> observe.getConceptReference().equalsIgnoreCase(id))
					.findFirst()
					.ifPresent(observe -> {
						observe.setDescription(description);
						if (units != null) {
							observe.setUnits(
								new ModelUnit()
									.setExpression(units.get("expression").asText())
									.setExpressionMathml(units.get("expressionMathml").asText())
							);
						}
					});
			}

			for (final ContentAndExtractionItems transition : response.transitions) {
				final String id = transition.content.get("id").asText();
				final String description = transition.content.get("description").asText();
				model
					.getRates()
					.stream()
					.filter(rate -> rate.getTarget().equalsIgnoreCase(id))
					.findFirst()
					.ifPresent(trans -> {
						trans.setDescription(description);

						StreamSupport.stream(model.getModel().get("transitions").spliterator(), false)
							.filter(transitionNode -> transitionNode.path("id").asText().equals(id))
							.findFirst()
							.ifPresent(transitionNode -> {
								((ObjectNode) transitionNode).put("description", description);
							});
					});
			}

			// Update State Grounding
			if (model.getStates() != null && !model.getStates().isEmpty()) {
				final List<State> states = model.getStates();
				TaskUtilities.getCuratedGrounding(states);
				model.setStates(states);
			}

			// Update Observable Grounding
			if (model.getObservables() != null && !model.getObservables().isEmpty()) {
				final List<Observable> observables = model.getObservables();
				TaskUtilities.getCuratedGrounding(observables);
				model.setObservables(observables);
			}

			// Update Parameter Grounding
			if (model.getParameters() != null && !model.getParameters().isEmpty()) {
				final List<ModelParameter> parameters = model.getParameters();
				TaskUtilities.getCuratedGrounding(parameters);
				model.setParameters(parameters);
			}

			// Update Transition Grounding
			if (model.getTransitions() != null && !model.getTransitions().isEmpty()) {
				final List<Transition> transitions = model.getTransitions();
				TaskUtilities.getCuratedGrounding(transitions);
				model.setTransitions(transitions);
			}

			modelService.updateAsset(model, model.getId());
		} catch (final Exception e) {
			log.error("Failed to enrich amr", e);
			throw new RuntimeException(e);
		}
		log.info("Model enriched successfully");
		return resp;
	}
}
