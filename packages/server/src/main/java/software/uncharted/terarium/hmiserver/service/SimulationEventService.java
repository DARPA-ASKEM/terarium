package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.models.simulationservice.ScimlStatusUpdate;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationEventService {


	private final ObjectMapper mapper;
  private final ClientEventService clientEventService;
  private final Config config;
  private final RabbitAdmin rabbitAdmin;

	private final Map<String, Set<String>> simulationIdToUserIds = new HashMap<>();

  private final static String SCIML_QUEUE = "sciml-queue";
  private final static String PYCIEMSS_QUEUE = "simulation-status";


  Queue scimlQueue;
  Queue pyciemssQueue;


  @PostConstruct
  void init() {
    scimlQueue = new Queue(SCIML_QUEUE, config.getDurableQueues());
    rabbitAdmin.declareQueue(scimlQueue);

    pyciemssQueue = new Queue(PYCIEMSS_QUEUE, config.getDurableQueues());
    rabbitAdmin.declareQueue(pyciemssQueue);

  }
	public void subscribe(List<String> simulationIds, User user){
		for(String simulationId : simulationIds) {
			if(!simulationIdToUserIds.containsKey(simulationId)) {
				simulationIdToUserIds.put(simulationId, new HashSet<>());
			}
			simulationIdToUserIds.get(simulationId).add(user.getId());
		}

	}

	public void unsubscribe(List<String> simulationIds, User user){
		for(String simulationId : simulationIds)
			simulationIdToUserIds.get(simulationId).remove(user.getId());
	}


  /**
   * Lisens for messages to send to a user and if we have the SSE connection, send it
   * @param message       the message to send
   * @param channel       the channel to send the message on
   * @throws IOException  if there was an error sending the message
   */
  @RabbitListener(
    queues = {SCIML_QUEUE},
    exclusive = true,
    concurrency = "1")
  private void onScimlSendToUserEvent(final Message message, final Channel channel) throws IOException {

		final ScimlStatusUpdate update = decodeMessage(message, ScimlStatusUpdate.class);
		ClientEvent<ScimlStatusUpdate> status = ClientEvent.<ScimlStatusUpdate>builder().type(ClientEventType.SIMULATION_SCIML).data(update).build();

		simulationIdToUserIds.get(update.getId()).forEach(userId -> {
			clientEventService.sendToUser(status, userId);
		});

  }

  /**
   * Lisens for messages to send to a user and if we have the SSE connection, send it
   * @param message       the message to send
   * @param channel       the channel to send the message on
   * @throws IOException  if there was an error sending the message
   */
  @RabbitListener(
          queues = {PYCIEMSS_QUEUE},
          exclusive = true,
          concurrency = "1")
  private void onPyciemssSendToUserEvent(final Message message, final Channel channel) throws IOException {
    //SimulationIntermediateResultsCiemss
		final JsonNode messageJson = decodeMessage(message, JsonNode.class);
		ClientEvent<Object> status = ClientEvent.builder().type(ClientEventType.SIMULATION_PYCIEMSS).data(messageJson).build();
		clientEventService.sendToAllUsers(status);
  }

	private <T>T decodeMessage(final Message message, Class<T> clazz) {

			try {
					return mapper.readValue(message.getBody(), clazz);
			} catch (IOException e) {
					log.error("Error decoding message", e);
					return null;
			}
	}

}
