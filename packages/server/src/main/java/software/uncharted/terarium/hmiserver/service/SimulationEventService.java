package software.uncharted.terarium.hmiserver.service;

import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.models.simulationservice.ScimlStatusUpdate;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationEventService {

    private final ClientEventService clientEventService;
    private final Config config;
    private final RabbitAdmin rabbitAdmin;

    private final Map<String, Set<String>> simulationIdToUserIds = new ConcurrentHashMap<>();

		@Value("${terarium.sciml-queue}")
		private String SCIML_QUEUE;

		@Value("${terarium.simulation-status}")
		private String PYCIEMSS_QUEUE;


    Queue scimlQueue;
    Queue pyciemssQueue;


    @PostConstruct
    void init() {
        scimlQueue = new Queue(SCIML_QUEUE, config.getDurableQueues(), false, false);
        rabbitAdmin.declareQueue(scimlQueue);

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
        for (final String simulationId : simulationIds)
            simulationIdToUserIds.get(simulationId).remove(user.getId());
    }


    /**
     * Listens for messages to send to a user and if we have the SSE connection, send it
     *
     * @param message the message to send
     * @param channel the channel to send the message on
     * @throws IOException if there was an error sending the message
     */
    @RabbitListener(
            queues = "${terarium.sciml-queue}",
            concurrency = "1")
    private void onScimlSendToUserEvent(final Message message, final Channel channel) throws IOException {

        final ScimlStatusUpdate update = ClientEventService.decodeMessage(message, ScimlStatusUpdate.class);
				if(update == null)
					return;
        final ClientEvent<ScimlStatusUpdate> status = ClientEvent.<ScimlStatusUpdate>builder().type(ClientEventType.SIMULATION_SCIML).data(update).build();
        simulationIdToUserIds.get(update.getId()).forEach(userId -> {
            clientEventService.sendToUser(status, userId);
        });

    }

    /**
     * Lisens for messages to send to a user and if we have the SSE connection, send it
     *
     * @param message the message to send
     * @param channel the channel to send the message on
     * @throws IOException if there was an error sending the message
     */
    @RabbitListener(
            queues = "${terarium.simulation-status}",
            concurrency = "1")
    private void onPyciemssSendToUserEvent(final Message message, final Channel channel) throws IOException {

			/*
				//TODO implement PyciemssStatusUpdate
				final PyciemssStatusUpdate update = ClientEventService.decodeMessage(message, PyciemssStatusUpdate.class);
				if(update == null)
					return;
        ClientEvent<PyciemssStatusUpdate> status = ClientEvent.<PyciemssStatusUpdate>builder().type(ClientEventType.SIMULATION_PYCIEMSS).data(update).build();
        simulationIdToUserIds.get(update.getId()).forEach(userId -> {
            clientEventService.sendToUser(status, userId);
        });
			 */

    }



}
