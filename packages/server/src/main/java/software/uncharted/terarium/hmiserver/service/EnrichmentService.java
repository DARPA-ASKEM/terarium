package software.uncharted.terarium.hmiserver.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
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
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.service.data.DKGService;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ModelService;
import software.uncharted.terarium.hmiserver.service.notification.NotificationGroupInstance;
import software.uncharted.terarium.hmiserver.service.notification.NotificationService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskService;
import software.uncharted.terarium.hmiserver.service.tasks.TaskUtilities;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrichmentService {

	private final ClientEventService clientEventService;
	private final CurrentUserService currentUserService;
	private final DKGService dkgService;
	private final DocumentAssetService documentService;
	private final ModelService modelService;
	private final NotificationService notificationService;
	private final TaskService taskService;

	@Data
	private static class Properties {

		private final UUID modelId;
	}

	/**
	 * Enriches the model with the data from the document.
	 *
	 * @param projectId the project ID
	 * @param documentId the document ID
	 * @param modelId the model ID
	 * @param permission the permission
	 */
	@Async
	public void modelWithDocument(UUID projectId, UUID documentId, UUID modelId, Schema.Permission permission) {
		// Create the notification group
		final NotificationGroupInstance<Properties> notificationInterface = new NotificationGroupInstance<>(
			clientEventService,
			notificationService,
			ClientEventType.ENRICHMENT_MODEL,
			projectId,
			new Properties(modelId)
		);

		try {
			notificationInterface.sendMessage("Beginning model enrichment with using document extraction...");

			// Get the Document
			DocumentAsset document = documentService
				.getAsset(documentId, permission)
				.orElseThrow(() -> {
					final String errorString = String.format("Document %s not found", documentId);
					log.warn(errorString);
					return new IOException(errorString);
				});

			// Make sure there is text in the document
			if (document.getText() == null || document.getText().isBlank()) {
				final String errorString = String.format("Document %s has no extracted text", documentId);
				log.warn(errorString);
				throw new IOException(errorString);
			}

			notificationInterface.sendMessage("Document text found.");

			// Get the model
			Model model = modelService
				.getAsset(modelId, permission)
				.orElseThrow(() -> {
					final String errorString = String.format("Model %s not found", modelId);
					log.warn(errorString);
					return new IOException(errorString);
				});

			notificationInterface.sendMessage("Model found.");

			// Stripping the metadata before it's sent since it can cause GoLLM to fail with massive inputs
			model.setMetadata(new ModelMetadata());
			notificationInterface.sendMessage("Model metadata stripped.");

			// Create the tasks
			final TaskRequest enrichAmrRequest = TaskUtilities.getEnrichAMRTaskRequest(
				currentUserService.get().getId(),
				document,
				model,
				projectId
			);

			final TaskRequest modelCardRequest = TaskUtilities.getModelCardTask(
				currentUserService.get().getId(),
				document,
				model,
				projectId
			);

			final TaskRequest taskRequest = new CompoundTask(enrichAmrRequest, modelCardRequest);

			try {
				taskService.runTask(TaskService.TaskMode.SYNC, taskRequest);
			} catch (final Exception e) {
				final String errorString = String.format("Task failed: %s", e);
				log.warn(errorString);
				throw new IOException(errorString);
			}

			notificationInterface.sendMessage("Model enrichment and model card complete.");

			// Get the enriched model
			final Model enrichedModel = modelService
				.getAsset(modelId, permission)
				.orElseThrow(() -> {
					final String errorString = String.format("Enriched Model %s not found", modelId);
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
				final String errorString = String.format("Failed to update model %s", modelId);
				log.warn(errorString);
				throw new IOException(errorString);
			}

			notificationInterface.sendFinalMessage("Model enriched and grounded");
		} catch (final IOException e) {
			log.error("Error enriching model with document", e);
		}
	}
}
