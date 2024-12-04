package software.uncharted.terarium.hmiserver.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.controller.knowledge.KnowledgeController;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Observable;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.State;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Transition;
import software.uncharted.terarium.hmiserver.models.dataservice.regnet.RegNetVertex;
import software.uncharted.terarium.hmiserver.models.task.CompoundTask;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.service.data.DKGService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.notification.NotificationGroupInstance;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskUtilities;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrichmentService {

	private final DKGService dkgService;
	private final ModelService modelService;
	private final TaskService taskService;

	/**
	 * Enriches the model with the data from the document.
	 *
	 * @param projectId the project ID
	 * @param document the document
	 * @param model the model
	 * @param currentUserId the current user ID
	 * @param permission the permission
	 */
	@Async
	public void modelWithDocument(
		UUID projectId,
		DocumentAsset document,
		Model model,
		String currentUserId,
		Schema.Permission permission,
		NotificationGroupInstance<KnowledgeController.NotificationProperties> notificationInterface
	) {
		log.info("YOHANN 2/4 - start async enrichment");
		try {
			// Stripping the metadata before it's sent since it can cause GoLLM to fail with massive inputs
			model.setMetadata(new ModelMetadata());
			notificationInterface.sendMessage("Model metadata stripped.");

			// Create the tasks
			final TaskRequest enrichAmrRequest = TaskUtilities.getEnrichAMRTaskRequest(
				currentUserId,
				document,
				model,
				projectId
			);

			final TaskRequest modelCardRequest = TaskUtilities.getModelCardTask(currentUserId, document, model, projectId);
			final TaskRequest taskRequest = new CompoundTask(enrichAmrRequest, modelCardRequest);

			try {
				notificationInterface.sendMessage("Starting Model enrichment and model card.");
				taskService.runTask(TaskService.TaskMode.SYNC, taskRequest);
			} catch (final Exception e) {
				final String errorString = String.format("Task failed: %s", e);
				log.warn(errorString);
				throw new IOException(errorString);
			}

			notificationInterface.sendMessage("Model enrichment and model card complete.");

			// Get the enriched model
			final Model enrichedModel = modelService
				.getAsset(model.getId(), permission)
				.orElseThrow(() -> {
					final String errorString = String.format("Enriched Model %s not found", model.getId());
					log.warn(errorString);
					return new IOException(errorString);
				});

			notificationInterface.sendMessage("Updating model grounding...");

			// Update State Grounding
			final List<State> states = enrichedModel.getStates();
			TaskUtilities.performDKGSearchAndSetGrounding(dkgService, states);
			enrichedModel.setStates(states);

			// Update Observable Grounding
			if (enrichedModel.getObservables() != null && !enrichedModel.getObservables().isEmpty()) {
				final List<Observable> observables = enrichedModel.getObservables();
				TaskUtilities.performDKGSearchAndSetGrounding(dkgService, observables);
				enrichedModel.setObservables(observables);
			}

			// Update Parameter Grounding
			if (enrichedModel.getParameters() != null && !enrichedModel.getParameters().isEmpty()) {
				final List<ModelParameter> parameters = enrichedModel.getParameters();
				TaskUtilities.performDKGSearchAndSetGrounding(dkgService, parameters);
				enrichedModel.setParameters(parameters);
			}

			// Update Transition Grounding
			if (enrichedModel.getTransitions() != null && !enrichedModel.getTransitions().isEmpty()) {
				final List<Transition> transitions = enrichedModel.getTransitions();
				TaskUtilities.performDKGSearchAndSetGrounding(dkgService, transitions);
				enrichedModel.setTransitions(transitions);
			}

			// Update the model
			try {
				modelService.updateAsset(enrichedModel, projectId, permission);
			} catch (final IOException e) {
				final String errorString = String.format("Failed to update model %s", model.getId());
				log.warn(errorString);
				throw new IOException(errorString);
			}

			notificationInterface.sendMessage("Model enriched using document extraction and grounded");
			log.info(
				"YOHANN 3/4 - Model {} enriched using document {} extraction and grounded.",
				model.getId(),
				document.getId()
			);
		} catch (final IOException e) {
			log.error("Error enriching model {} with document {}", model.getId(), document.getId(), e);
		}
	}
}
