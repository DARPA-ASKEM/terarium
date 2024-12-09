package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.GroundedSemantic;
import software.uncharted.terarium.hmiserver.models.mira.DKG;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.service.data.DKGService;

@Slf4j
public class TaskUtilities {

	public static TaskRequest getEnrichAMRTaskRequest(String userId, DocumentAsset document, Model model, UUID projectId)
		throws IOException {
		final ObjectMapper objectMapper = new ObjectMapper();

		final EnrichAmrResponseHandler.Input input = new EnrichAmrResponseHandler.Input();
		if (document != null) {
			try {
				input.setResearchPaper(objectMapper.writeValueAsString(document.getExtractions()));
			} catch (JsonProcessingException e) {
				throw new IOException("Unable to serialize document text");
			}
		}

		input.setAmr(model.serializeWithoutTerariumFields(null, null));

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskRequest.TaskType.GOLLM);
		req.setScript(EnrichAmrResponseHandler.NAME);
		req.setUserId(userId);

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			throw new IOException("Unable to serialize input");
		}

		req.setProjectId(projectId);

		final EnrichAmrResponseHandler.Properties props = new EnrichAmrResponseHandler.Properties();
		props.setProjectId(projectId);
		if (document != null) props.setDocumentId(document.getId());
		props.setModelId(model.getId());
		req.setAdditionalProperties(props);

		return req;
	}

	public static TaskRequest getEnrichDatasetTaskRequest(
		String userId,
		DocumentAsset document,
		Dataset dataset,
		UUID projectId,
		Boolean overwrite
	) throws IOException {
		final ObjectMapper objectMapper = new ObjectMapper();

		final EnrichDatasetResponseHandler.Input input = new EnrichDatasetResponseHandler.Input();
		if (document != null) {
			try {
				input.setResearchPaper(objectMapper.writeValueAsString(document.getExtractions()));
			} catch (JsonProcessingException e) {
				throw new IOException("Unable to serialize document text");
			}
		}

		// Serialize the dataset columns
		final List<DatasetColumn> columns = dataset.getColumns();
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setConfig(mapper.getSerializationConfig().with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
		final ArrayNode columnsNode = mapper.convertValue(columns, ArrayNode.class);

		// Remove the fields that are not needed
		for (JsonNode column : columnsNode) {
			((ObjectNode) column).remove("id");
			((ObjectNode) column).remove("createdOn");
			((ObjectNode) column).remove("updatedOn");
			((ObjectNode) column).remove("grounding");
			((ObjectNode) column).remove("metadata");
			((ObjectNode) column).remove("dataType");
			((ObjectNode) column).remove("description");
		}
		final String serializedColumns = mapper.writeValueAsString(columnsNode);

		input.setDataset(serializedColumns);

		// Create the task
		final TaskRequest taskRequest = new TaskRequest();
		taskRequest.setType(TaskRequest.TaskType.GOLLM);
		taskRequest.setScript(EnrichDatasetResponseHandler.NAME);
		taskRequest.setUserId(userId);

		try {
			taskRequest.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			throw new IOException("Unable to serialize input");
		}

		taskRequest.setProjectId(projectId);

		final EnrichDatasetResponseHandler.Properties properties = new EnrichDatasetResponseHandler.Properties();
		properties.setProjectId(projectId);
		if (document != null) properties.setDocumentId(document.getId());
		properties.setDatasetId(dataset.getId());
		properties.setOverwrite(overwrite);
		taskRequest.setAdditionalProperties(properties);

		return taskRequest;
	}

	public static TaskRequest getModelCardTask(String userId, DocumentAsset document, Model model, UUID projectId)
		throws IOException {
		final ObjectMapper objectMapper = new ObjectMapper();

		final ModelCardResponseHandler.Input input = new ModelCardResponseHandler.Input();
		input.setAmr(model.serializeWithoutTerariumFields(null, new String[] { "gollmCard" }));

		if (document != null) {
			try {
				input.setResearchPaper(objectMapper.writeValueAsString(document.getExtractions()));
			} catch (JsonProcessingException e) {
				throw new IOException("Unable to serialize document text");
			}
		} else {
			input.setResearchPaper("");
		}

		// Create the task
		final TaskRequest req = new TaskRequest();
		req.setType(TaskRequest.TaskType.GOLLM);
		req.setScript(ModelCardResponseHandler.NAME);
		req.setUserId(userId);

		try {
			req.setInput(objectMapper.writeValueAsBytes(input));
		} catch (final Exception e) {
			throw new IOException("Unable to serialize input");
		}

		req.setProjectId(projectId);

		final ModelCardResponseHandler.Properties props = new ModelCardResponseHandler.Properties();
		props.setProjectId(projectId);
		if (document != null) props.setDocumentId(document.getId());
		props.setModelId(model.getId());
		req.setAdditionalProperties(props);

		return req;
	}

	@Observed(name = "function_profile")
	public static void performDKGSearchAndSetGrounding(DKGService dkgService, List<? extends GroundedSemantic> parts) {
		List<String> searchTerms = parts
			.stream()
			.filter(part -> part != null && part.getId() != null && !part.getId().isEmpty())
			.map(TaskUtilities::getSearchTerm)
			.collect(Collectors.toList());

		List<DKG> curies = new ArrayList<>();
		try {
			curies = dkgService.knnSearchEpiDKG(0, 100, 1, searchTerms, null);
		} catch (Exception e) {
			log.warn("Unable to find DKG for semantics: {}", searchTerms, e);
			return;
		}

		for (int i = 0; i < curies.size(); i++) {
			DKG dkg = curies.get(i);
			GroundedSemantic part = parts.get(i);
			if (part.getGrounding() == null) part.setGrounding(new ModelGrounding());
			if (part.getGrounding().getIdentifiers() == null) part.getGrounding().setIdentifiers(new HashMap<>());
			String[] currieId = dkg.getCurie().split(":");
			part.getGrounding().getIdentifiers().put(currieId[0], currieId[1]);
		}
	}

	private static String getSearchTerm(GroundedSemantic part) {
		return (part.getDescription() == null || part.getDescription().isEmpty()) ? part.getId() : part.getDescription();
	}
}
