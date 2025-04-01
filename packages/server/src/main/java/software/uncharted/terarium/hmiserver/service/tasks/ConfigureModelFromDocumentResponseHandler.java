package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.enrichment.Enrichment;
import software.uncharted.terarium.hmiserver.models.dataservice.enrichment.EnrichmentSource;
import software.uncharted.terarium.hmiserver.models.dataservice.enrichment.EnrichmentTarget;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService;
import software.uncharted.terarium.hmiserver.service.data.ModelConfigurationService.ModelConfigurationUpdate;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigureModelFromDocumentResponseHandler extends LlmTaskResponseHandler {

	public static final String NAME = "gollm:configure_model_from_document";

	private final ObjectMapper objectMapper;
	private final ModelConfigurationService modelConfigurationService;
	private final ProvenanceService provenanceService;
	private final DocumentAssetService documentAssetService;
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
	public static class Properties {

		UUID projectId;
		UUID documentId;
		UUID modelId;
		UUID workflowId;
		UUID nodeId;
	}

	@Data
	private static class ContentAndExtractionItems {

		JsonNode content;
		String[] extractionItemIds;
	}

	@Data
	private static class Condition {

		ContentAndExtractionItems name;
		ContentAndExtractionItems description;
		List<ContentAndExtractionItems> initialSemanticList;
		List<ContentAndExtractionItems> parameterSemanticList;
	}

	@Data
	private static class Response {

		List<Condition> conditions;
	}

	private List<Enrichment> createEnrichments(final Condition condition, final Properties props) {
		final List<Enrichment> enrichments = new ArrayList<>();

		final Enrichment descriptionEnrichment = new Enrichment();
		descriptionEnrichment.setId(UUID.randomUUID());
		descriptionEnrichment.setTarget(EnrichmentTarget.DESCRIPTION);
		descriptionEnrichment.setSource(EnrichmentSource.GOLLM);
		descriptionEnrichment.setContent(condition.getDescription().getContent());
		descriptionEnrichment.setExtractionAssetId(props.documentId);
		descriptionEnrichment.setExtractionItemIds(condition.description.extractionItemIds);
		descriptionEnrichment.setIncluded(false);
		enrichments.add(descriptionEnrichment);

		condition.initialSemanticList.forEach(initial -> {
			final Enrichment enrichment = new Enrichment();
			enrichment.setId(UUID.randomUUID());
			enrichment.setTarget(EnrichmentTarget.STATE);
			enrichment.setSource(EnrichmentSource.GOLLM);
			enrichment.setContent(initial.getContent());
			enrichment.setExtractionAssetId(props.documentId);
			enrichment.setExtractionItemIds(initial.getExtractionItemIds());
			enrichment.setIncluded(false);
			enrichments.add(enrichment);
		});
		condition.parameterSemanticList.forEach(parameter -> {
			final Enrichment enrichment = new Enrichment();
			enrichment.setId(UUID.randomUUID());
			enrichment.setTarget(EnrichmentTarget.PARAMETER);
			enrichment.setSource(EnrichmentSource.GOLLM);
			enrichment.setContent(parameter.getContent());
			enrichment.setExtractionAssetId(props.documentId);
			enrichment.setExtractionItemIds(parameter.getExtractionItemIds());
			enrichment.setIncluded(false);
			enrichments.add(enrichment);
		});
		return enrichments;
	}

	@Override
	public TaskResponse onSuccess(final TaskResponse resp) {
		try {
			final Properties props = resp.getAdditionalProperties(Properties.class);
			final JsonNode node = objectMapper.readValue(resp.getOutput(), JsonNode.class);
			final Response response = objectMapper.treeToValue(node.get("response"), Response.class);

			final Optional<Model> model = modelService.getAsset(props.modelId);
			final ModelConfiguration configuration = ModelConfigurationService.modelConfigurationFromAMR(
				model.get(),
				new ModelConfigurationUpdate()
			);

			// For each configuration, create a new model configuration
			for (final Condition condition : response.conditions) {
				final ModelConfiguration clonedConfiguration = configuration.clone();

				clonedConfiguration.setName(condition.name.content.asText());
				// create and set enrichments
				List<Enrichment> enrichments = createEnrichments(condition, props);
				clonedConfiguration.setEnrichments(enrichments);
				// Set the extraction document id
				clonedConfiguration.setExtractionDocumentId(props.documentId);

				final ModelConfiguration newConfig = modelConfigurationService.createAsset(
					clonedConfiguration,
					props.projectId
				);

				// add provenance
				provenanceService.createProvenance(
					new Provenance()
						.setLeft(newConfig.getId())
						.setLeftType(ProvenanceType.MODEL_CONFIGURATION)
						.setRight(props.documentId)
						.setRightType(ProvenanceType.DOCUMENT)
						.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
				);
			}
		} catch (final Exception e) {
			log.error("Failed to configure model", e);
			throw new RuntimeException(e);
		}
		log.info("Model configured successfully");
		return resp;
	}
}
