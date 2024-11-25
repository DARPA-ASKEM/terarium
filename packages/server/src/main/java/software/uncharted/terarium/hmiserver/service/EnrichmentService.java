package software.uncharted.terarium.hmiserver.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.controller.knowledge.KnowledgeController;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Observable;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.State;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Transition;
import software.uncharted.terarium.hmiserver.models.task.CompoundTask;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.service.data.DKGService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.notification.NotificationGroupInstance;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskUtilities;
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrichmentService {

	private final DKGService dkgService;
	private final DocumentAssetService documentService;
	private final ModelService modelService;
	private final TaskService taskService;

	private final Messages messages;

	/**
	 * Enriches the model with the data from the document.
	 *
	 * @param projectId the project ID
	 * @param documentId the document ID
	 * @param model the model
	 * @param currentUserId the current user ID
	 * @param permission the permission
	 */
	@Async
	public void modelWithDocument(
		UUID projectId,
		UUID documentId,
		Model model,
		String currentUserId,
		Schema.Permission permission,
		NotificationGroupInstance<KnowledgeController.Properties> notificationInterface
	) {
		try {
			// Get the Document
			final DocumentAsset document = documentService
				.getAsset(documentId, permission)
				.orElseThrow(() ->
					new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						messages.get("An error occurred while trying to get the document.")
					)
				);

			// Make sure there is text in the document
			if (document.getText() == null || document.getText().isBlank()) {
				final String errorString = String.format("Document %s has no extracted text", documentId);
				throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, messages.get("errorString"));
			}
			notificationInterface.sendMessage("Document text found.");

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
			final List<State> states = enrichedModel
				.getStates()
				.stream()
				.map(state -> state == null ? new State() : state)
				.collect(Collectors.toList());
			states.forEach(state -> {
				TaskUtilities.performDKGSearchAndSetGrounding(dkgService, state);
			});
			enrichedModel.setStates(states);
			notificationInterface.sendMessage("State grounding complete.");

			// Update Observable Grounding
			if (enrichedModel.getObservables() != null && !enrichedModel.getObservables().isEmpty()) {
				final List<Observable> observables = enrichedModel.getObservables();
				observables.forEach(observable -> {
					if (observable == null) {
						observable = new Observable();
					}
					TaskUtilities.performDKGSearchAndSetGrounding(dkgService, observable);
				});
				enrichedModel.setObservables(observables);
				notificationInterface.sendMessage("Observable grounding complete.");
			}

			// Update Parameter Grounding
			if (enrichedModel.getParameters() != null && !enrichedModel.getParameters().isEmpty()) {
				final List<ModelParameter> parameters = enrichedModel.getParameters();
				parameters.forEach(parameter -> {
					if (parameter == null) {
						parameter = new ModelParameter();
					}
					TaskUtilities.performDKGSearchAndSetGrounding(dkgService, parameter);
				});
				enrichedModel.setParameters(parameters);
				notificationInterface.sendMessage("Parameter grounding complete.");
			}

			// Update Transition Grounding
			if (enrichedModel.getTransitions() != null && !enrichedModel.getTransitions().isEmpty()) {
				final List<Transition> transitions = enrichedModel.getTransitions();
				transitions.forEach(transition -> {
					if (transition == null) {
						transition = new Transition();
					}
					TaskUtilities.performDKGSearchAndSetGrounding(dkgService, transition);
				});
				enrichedModel.setTransitions(transitions);
				notificationInterface.sendMessage("Transition grounding complete.");
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
			log.info("Model {} enriched using document {} extraction and grounded.", model.getId(), documentId);
		} catch (final IOException e) {
			log.error("Error enriching model {} with document {}", model.getId(), documentId, e);
		}
	}
}
