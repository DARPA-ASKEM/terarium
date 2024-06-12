package software.uncharted.terarium.hmiserver.service.notification;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationEngine;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationType;
import software.uncharted.terarium.hmiserver.service.ClientEventService;
import software.uncharted.terarium.hmiserver.service.data.SimulationService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;

public class SimulationRequestStatusNotifier {

	private int POLLING_INTERVAL_SECONDS = 5;

	private UUID simulationId;
	private UUID projectId;
	private Schema.Permission permission;
	private JsonNode metadata; // Arbitrary metadata to be sent to the client

	private final ScheduledExecutorService executor;
	private final ClientEventService clientEventService;
	private final NotificationService notificationService;
	private final SimulationService simulationService;

	@Data
	@TSModel
	private static class SimulationNotificationData {
		private final UUID simulationId;
		private final SimulationType simulationType;
		private final SimulationEngine simulationEngine;
		private final JsonNode metadata;
	}

	public SimulationRequestStatusNotifier(
		final NotificationService notificationService,
		final ClientEventService clientEventService,
		final SimulationService simulationService,
		final UUID simulationId,
		final UUID projectId,
		final Schema.Permission permission,
		final JsonNode metadata
	) {
		this.clientEventService = clientEventService;
		this.notificationService = notificationService;
		this.simulationService = simulationService;
		this.simulationId = simulationId;
		this.projectId = projectId;
		this.permission = permission;
		this.metadata = metadata;

		this.executor = Executors.newScheduledThreadPool(1);
	}

	private void sendStatusMessage(final NotificationGroupInstance<SimulationNotificationData> notificationInterface, final Simulation simulation) {
		final String statusMessage = simulation.getStatusMessage();
		final ProgressState status = simulation.getStatus();
		if (status.equals(ProgressState.FAILED) || status.equals(ProgressState.ERROR)) {
			throw new RuntimeException((statusMessage == null || statusMessage.isEmpty()) ? "Failed running simulation " + simulation.getId() : statusMessage);
		} else if (status.equals(ProgressState.CANCELLED)) {
			notificationInterface.sendFinalMessage("Simulation has been cancelled.", ProgressState.CANCELLED);
			this.executor.shutdown();
		}	else if (status.equals(ProgressState.COMPLETE)) {
			notificationInterface.sendFinalMessage("Simulation has completed.", ProgressState.COMPLETE);
			this.executor.shutdown();
		} else if (status.equals(ProgressState.QUEUED)) {
			notificationInterface.sendMessage("Simulation is queued...", ProgressState.QUEUED);
		} else {
			notificationInterface.sendMessage((statusMessage == null || statusMessage.isEmpty()) ? "Simulation is running..." : statusMessage);
		}
	}

	public void startPolling() {
		final Optional<Simulation> simAsset = simulationService.getAsset(this.simulationId, this.permission);
		if (simAsset.isEmpty()) {
			throw new RuntimeException("Simulation object is empty.");
		}
		final Simulation sim = simAsset.get();

		final NotificationGroupInstance<SimulationNotificationData> notificationInterface = new NotificationGroupInstance<SimulationNotificationData>(
			clientEventService,
			notificationService,
			ClientEventType.SIMULATION_NOTIFICATION,
			this.projectId,
			new SimulationNotificationData(this.simulationId, sim.getType(), sim.getEngine(), this.metadata)
		);
		this.sendStatusMessage(notificationInterface, sim);

		final Runnable poller = () -> {
			try {
				final Optional<Simulation> result = simulationService.getAsset(this.simulationId, this.permission);
				if (result.isEmpty()) {
					throw new RuntimeException("Simulation object is empty.");
				}
				final Simulation simulation = result.get();
				this.sendStatusMessage(notificationInterface, simulation);
			} catch (final RuntimeException e) {
				notificationInterface.sendFinalMessage(e.getMessage(), ProgressState.FAILED);
				this.executor.shutdown();
			} catch (final Exception e) {
				notificationInterface.sendFinalMessage("Unexpected error occurred while checking the simulation status.", ProgressState.FAILED);
				this.executor.shutdown();
			}
		};
		executor.scheduleAtFixedRate(poller, POLLING_INTERVAL_SECONDS, POLLING_INTERVAL_SECONDS, TimeUnit.SECONDS);
	}
}
