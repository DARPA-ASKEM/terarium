package software.uncharted.terarium.hmiserver.service.notification;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.Simulation;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationEngine;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationType;
import software.uncharted.terarium.hmiserver.service.ClientEventService;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;
import software.uncharted.terarium.hmiserver.service.data.SimulationService;

@Accessors(chain = true)
@Slf4j
public class SimulationRequestStatusNotifier {

	private final int DEFAULT_POLLING_INTERVAL_SECONDS = 5;
	private final int DEFAULT_POLLING_THRESHOLD = 500; // 500 * 5 seconds = 2500 seconds = 41 minutes
	private final Double DEFAULT_HALF_TIME_SECONDS = 2.0;

	private final UUID simulationId;
	private final UUID projectId;
	private final JsonNode metadata; // Arbitrary metadata to be sent to the client along with the notification event.

	@Setter
	private int interval = DEFAULT_POLLING_INTERVAL_SECONDS;

	@Setter
	private int threshold = DEFAULT_POLLING_THRESHOLD;

	/*
	 * Estimated time it takes for the simulation to reach half of its completion.
	 * This is used to calculate the estimated time remaining for the simulation to complete.
	 */
	@Setter
	private Double halfTimeSeconds = DEFAULT_HALF_TIME_SECONDS;

	private int pollAttempts = 0;

	private final ScheduledExecutorService executor;
	private final ClientEventService clientEventService;
	private final CurrentUserService currentUserService;
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
		final CurrentUserService currentUserService,
		final SimulationService simulationService,
		final UUID simulationId,
		final UUID projectId,
		final JsonNode metadata
	) {
		this.clientEventService = clientEventService;
		this.currentUserService = currentUserService;
		this.notificationService = notificationService;
		this.simulationService = simulationService;
		this.simulationId = simulationId;
		this.projectId = projectId;
		this.metadata = metadata;
		this.executor = Executors.newScheduledThreadPool(1);
	}

	private void sendStatusMessage(
		final NotificationGroupInstance<SimulationNotificationData> notificationInterface,
		final Simulation simulation
	) {
		final String statusMessage = simulation.getStatusMessage() != null ? simulation.getStatusMessage() : "";
		final ProgressState status = simulation.getStatus();
		if (status.equals(ProgressState.FAILED) || status.equals(ProgressState.ERROR)) {
			throw new RuntimeException("Failed running simulation " + simulation.getId() + "\n" + statusMessage);
		} else if (status.equals(ProgressState.CANCELLED)) {
			notificationInterface.sendFinalMessage("Simulation has been cancelled.", ProgressState.CANCELLED);
			this.executor.shutdown();
		} else if (status.equals(ProgressState.COMPLETE)) {
			notificationInterface.sendFinalMessage("Simulation has completed.", ProgressState.COMPLETE);
			this.executor.shutdown();
		} else if (status.equals(ProgressState.QUEUED)) {
			notificationInterface.sendMessage("Simulation is queued...", ProgressState.QUEUED);
		} else {
			notificationInterface.sendMessage((statusMessage.isEmpty()) ? "Simulation is running..." : statusMessage);
		}
	}

	public void startPolling() {
		final Optional<Simulation> simAsset = simulationService.getAsset(this.simulationId);
		if (simAsset.isEmpty()) {
			throw new RuntimeException("Simulation object is empty.");
		}
		final Simulation sim = simAsset.get();

		final NotificationGroupInstance<SimulationNotificationData> notificationInterface = new NotificationGroupInstance<
			SimulationNotificationData
		>(
			clientEventService,
			notificationService,
			ClientEventType.SIMULATION_NOTIFICATION,
			this.projectId,
			new SimulationNotificationData(this.simulationId, sim.getType(), sim.getEngine(), this.metadata),
			this.halfTimeSeconds,
			sim.getId(),
			currentUserService.get().getId()
		);
		log.info("Starting polling for simulation {} every {} seconds", this.simulationId, this.interval);
		this.sendStatusMessage(notificationInterface, sim);

		final Runnable poller = () -> {
			try {
				pollAttempts++;
				if (pollAttempts > this.threshold) {
					throw new RuntimeException("Timeout while waiting for simulation to complete.");
				}
				final Optional<Simulation> result = simulationService.getAsset(this.simulationId);
				if (result.isEmpty()) {
					throw new RuntimeException("Simulation object is empty.");
				}
				final Simulation simulation = result.get();
				this.sendStatusMessage(notificationInterface, simulation);
				log.info(
					"Polling simulation {} with status {} for the {} time",
					this.simulationId,
					simulation.getStatus(),
					pollAttempts
				);
			} catch (final Exception e) {
				notificationInterface.sendFinalMessage(e.getMessage(), ProgressState.FAILED);
				this.executor.shutdown();
				log.error("Error occurred while polling for simulation {}\n{}", this.simulationId, e.getMessage());
			}
		};
		executor.scheduleAtFixedRate(poller, this.interval, this.interval, TimeUnit.SECONDS);
	}
}
