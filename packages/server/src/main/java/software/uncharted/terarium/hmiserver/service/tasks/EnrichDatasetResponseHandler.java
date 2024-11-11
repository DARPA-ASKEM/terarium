package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.DatasetService;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrichDatasetResponseHandler extends TaskResponseHandler {

	public static final String NAME = "gollm:enrich_dataset";

	private final ObjectMapper objectMapper;
	private final DatasetService datasetService;

	@Override
	public String getName() {
		return NAME;
	}

	@Data
	public static class Input {

		@JsonProperty("research_paper")
		String researchPaper;

		@JsonProperty("dataset")
		String dataset;
	}

	@Data
	private static class Enrichment {
		// TODO
	}

	@Data
	public static class Response {

		Enrichment response;
	}

	@Data
	public static class Properties {

		UUID projectId;
		UUID documentId;
		UUID datasetId;
		Boolean overwrite;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties properties = resp.getAdditionalProperties(Properties.class);
			final Response response = objectMapper.readValue(resp.getOutput(), Response.class);

			final Dataset dataset = datasetService
				.getAsset(properties.getDatasetId(), ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER)
				.orElseThrow();

			// Create the metadata for the dataset if it doesn't exist
			if (dataset.getMetadata() == null) {
				dataset.setMetadata(objectMapper.createObjectNode());
			}

			// Update the dataset with the new card
			((ObjectNode) dataset.getMetadata()).set("dataCard", response.response.card);

			// Update the dataset with the new descriptions
			//			for (final DescriptionsAndUnits state : response.response.states) {
			//				dataset
			//					.getInitials()
			//					.stream()
			//					.filter(initial -> initial.getTarget().equalsIgnoreCase(state.id))
			//					.findFirst()
			//					.ifPresent(initial -> {
			//						initial.setDescription(state.description);
			//						StreamSupport.stream(dataset.getModel().get("states").spliterator(), false)
			//							.filter(stateNode -> stateNode.path("id").asText().equals(state.id))
			//							.findFirst()
			//							.ifPresent(stateNode -> {
			//								((ObjectNode) stateNode).put("description", state.description);
			//								if (state.units != null) {
			//									((ObjectNode) stateNode).put("units", objectMapper.createObjectNode());
			//									((ObjectNode) stateNode.get("units")).put("expression", state.units.expression);
			//									((ObjectNode) stateNode.get("units")).put("expression_mathml", state.units.expressionMathml);
			//								}
			//							});
			//					});
			// 			}

			//			for (final DescriptionsAndUnits parameter : response.response.parameters) {
			//				dataset
			//					.getParameters()
			//					.stream()
			//					.filter(param -> param.getId().equalsIgnoreCase(parameter.id))
			//					.findFirst()
			//					.ifPresent(param -> {
			//						param.setDescription(parameter.description);
			//						if (parameter.units != null) {
			//							param.setUnits(
			//								new ModelUnit()
			//									.setExpression(parameter.units.expression)
			//									.setExpressionMathml(parameter.units.expressionMathml)
			//							);
			//						}
			//					});
			//			}

			//			for (final DescriptionsAndUnits observable : response.response.observables) {
			//				dataset
			//					.getObservables()
			//					.stream()
			//					.filter(observe -> observe.getId().equalsIgnoreCase(observable.id))
			//					.findFirst()
			//					.ifPresent(observe -> {
			//						observe.setDescription(observable.description);
			//						if (observable.units != null) {
			//							observe.setUnits(
			//								new ModelUnit()
			//									.setExpression(observable.units.expression)
			//									.setExpressionMathml(observable.units.expressionMathml)
			//							);
			//						}
			//					});
			//			}

			//			for (final Descriptions transition : response.response.transitions) {
			//				dataset
			//					.getRates()
			//					.stream()
			//					.filter(rate -> rate.getTarget().equalsIgnoreCase(transition.id))
			//					.findFirst()
			//					.ifPresent(trans -> {
			//						trans.setDescription(transition.description);
			//
			//						StreamSupport.stream(dataset.getModel().get("transitions").spliterator(), false)
			//							.filter(transitionNode -> transitionNode.path("id").asText().equals(transition.id))
			//							.findFirst()
			//							.ifPresent(transitionNode -> {
			//								((ObjectNode) transitionNode).put("description", transition.description);
			//							});
			//					});
			//			}

			datasetService.updateAsset(dataset, dataset.getId(), ASSUME_WRITE_PERMISSION_ON_BEHALF_OF_USER);
		} catch (final Exception e) {
			log.error("Failed to enrich dataset", e);
			throw new RuntimeException(e);
		}

		log.info("Dataset enriched successfully");
		return resp;
	}
}
