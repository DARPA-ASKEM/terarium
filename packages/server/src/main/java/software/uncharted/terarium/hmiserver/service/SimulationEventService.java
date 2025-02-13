package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.authority.User;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.SimulationUpdate;
import software.uncharted.terarium.hmiserver.models.simulationservice.statusupdates.CiemssCalibrateStatusUpdate;
import software.uncharted.terarium.hmiserver.models.simulationservice.statusupdates.CiemssOptimizeStatusUpdate;
import software.uncharted.terarium.hmiserver.models.simulationservice.statusupdates.CiemssStatusType;
import software.uncharted.terarium.hmiserver.models.simulationservice.statusupdates.CiemssStatusUpdate;
import software.uncharted.terarium.hmiserver.service.data.SimulationService;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationEventService {

	private final ClientEventService clientEventService;
	private final Config config;
	private final RabbitAdmin rabbitAdmin;
	private final RabbitTemplate rabbitTemplate;
	private final SimulationService simulationService;

	private final Map<String, Set<String>> simulationIdToUserIds = new ConcurrentHashMap<>();

	@Value("${terarium.simulation-status}")
	private String PYCIEMSS_QUEUE;

	// Once a single instance of the hmi-server has processed a sim response, it
	// will publish to this exchange to broadcast the response to all other
	// instances. This will direct the message to all hmi-instances so that the
	// correct instance holding the sse can forward the response to the user.
	@Value("${terarium.simulation.pyciemss-broadcast-exchange}")
	private String PYCIEMSS_BROADCAST_EXCHANGE;

	Queue pyciemssQueue;

	final Schema.Permission assumedPermission = Schema.Permission.WRITE;

	@PostConstruct
	void init() {
		pyciemssQueue = new Queue(PYCIEMSS_QUEUE, config.getDurableQueues(), false, false);
		rabbitAdmin.declareQueue(pyciemssQueue);
	}

	public void subscribe(final List<String> simulationIds, final User user) {
		for (final String simulationId : simulationIds) {
			if (!simulationIdToUserIds.containsKey(simulationId)) {
				simulationIdToUserIds.put(simulationId, new HashSet<>());
			}
			simulationIdToUserIds.get(simulationId).add(user.getId());
		}
	}

	public void unsubscribe(final List<String> simulationIds, final User user) {
		for (final String simulationId : simulationIds) simulationIdToUserIds.get(simulationId).remove(user.getId());
	}

	@RabbitListener(queues = "${terarium.simulation-status}", concurrency = "1")
	private void onPyciemssOneInstanceReceives(final Message message, final Channel channel) throws IOException {
		// Parse message to get message type:
		final String updateType = (ClientEventService.decodeMessage(message, JsonNode.class)).get("type").asText();
		if (updateType.equals(CiemssStatusType.OPTIMIZE.toString())) {
			final CiemssOptimizeStatusUpdate update = ClientEventService.decodeMessage(
				message,
				CiemssOptimizeStatusUpdate.class
			);
			updateAndSendPyciemssMessage(message, update);
		} else if (updateType.equals(CiemssStatusType.CALIBRATE.toString())) {
			final CiemssCalibrateStatusUpdate update = ClientEventService.decodeMessage(
				message,
				CiemssCalibrateStatusUpdate.class
			);
			updateAndSendPyciemssMessage(message, update);
		} else {
			log.error("message in simulation-status with unknown type: " + updateType);
		}
	}

	private void updateAndSendPyciemssMessage(final Message message, final CiemssStatusUpdate update) {
		if (update == null) {
			return;
		}
		try {
			final SimulationUpdate simulationUpdate = new SimulationUpdate();
			simulationUpdate.setData(update.getDataToPersist());
			simulationService.appendUpdateToSimulation(
				UUID.fromString(update.getJobId()),
				simulationUpdate,
				assumedPermission
			);
		} catch (final Exception e) {
			log.error("Error processing event", e);
		}

		rabbitTemplate.convertAndSend(PYCIEMSS_BROADCAST_EXCHANGE, "", message.getBody());
	}

	// This is an anonymous queue, every instance the hmi-server will receive a
	// message. Any operation that must occur on _every_ instance of the hmi-server
	// should be triggered here.
	@RabbitListener(
		bindings = @QueueBinding(
			value = @org.springframework.amqp.rabbit.annotation.Queue(
				autoDelete = "true",
				exclusive = "false",
				durable = "${terarium.taskrunner.durable-queues}"
			),
			exchange = @Exchange(
				value = "${terarium.simulation.pyciemss-broadcast-exchange}",
				durable = "${terarium.taskrunner.durable-queues}",
				autoDelete = "false",
				type = ExchangeTypes.DIRECT
			),
			key = ""
		),
		concurrency = "1"
	)
	private void onPyciemssAllInstanceReceive(final Message message) {
		try {
			// Parse message to get message type:
			final String updateType = (ClientEventService.decodeMessage(message, JsonNode.class)).get("type").asText();
			if (updateType.equals(CiemssStatusType.OPTIMIZE.toString())) {
				final CiemssOptimizeStatusUpdate update = ClientEventService.decodeMessage(
					message,
					CiemssOptimizeStatusUpdate.class
				);
				passPyciemssToUser(update);
			} else if (updateType.equals(CiemssStatusType.CALIBRATE.toString())) {
				final CiemssCalibrateStatusUpdate update = ClientEventService.decodeMessage(
					message,
					CiemssCalibrateStatusUpdate.class
				);
				passPyciemssToUser(update);
			} else {
				log.error("message in simulation-status with unknown type: " + updateType);
			}
		} catch (final Exception e) {
			log.error("Error processing event", e);
		}
	}

	private void passPyciemssToUser(final CiemssStatusUpdate update) {
		if (update == null) {
			return;
		}
		final ClientEvent<CiemssStatusUpdate> status = ClientEvent.<CiemssStatusUpdate>builder()
			.type(ClientEventType.SIMULATION_PYCIEMSS)
			.data(update)
			.build();

		final String id = update.getJobId();
		if (simulationIdToUserIds.containsKey(id)) {
			simulationIdToUserIds
				.get(id)
				.forEach(userId -> {
					clientEventService.sendToUser(status, userId);
				});
		}
	}
}
