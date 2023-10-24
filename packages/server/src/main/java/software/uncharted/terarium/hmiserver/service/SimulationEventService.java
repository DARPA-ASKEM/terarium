package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.configuration.Config;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimulationEventService {

  private final ServerSentEventService serverSentEventService;

  private final Config config;
  private final RabbitTemplate rabbitTemplate;
  private final RabbitAdmin rabbitAdmin;
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
    final JsonNode messageJson = ServerSentEventService.decodeMessage(message, JsonNode.class);
    if (messageJson == null) {
      ServerSentEventService.nack(message, channel);
      return;
    }
    final SseEmitter emitter = serverSentEventService.getUserIdToEmitter().get(messageJson.at("/userId").asText());
    synchronized (serverSentEventService.getUserIdToEmitter()) {
      if (emitter != null) {
        try {
          emitter.send(messageJson);
        } catch (IllegalStateException | ClientAbortException e) {
          log.warn("Error sending user message to user {}. User likely disconnected", messageJson.at("/userId").asText());
          serverSentEventService.getUserIdToEmitter().remove(messageJson.at("/userId").asText());
        } catch (IOException e) {
          log.error("Error sending user message to user {}", messageJson.at("/userId").asText(), e);
        }
      }
    }
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
    final JsonNode messageJson = ServerSentEventService.decodeMessage(message, JsonNode.class);
    if (messageJson == null) {
      ServerSentEventService.nack(message, channel);
      return;
    }
    final SseEmitter emitter = serverSentEventService.getUserIdToEmitter().get(messageJson.at("/userId").asText());
    synchronized (serverSentEventService.getUserIdToEmitter()) {
      if (emitter != null) {
        try {
          emitter.send(messageJson);
        } catch (IllegalStateException | ClientAbortException e) {
          log.warn("Error sending user message to user {}. User likely disconnected", messageJson.at("/userId").asText());
          serverSentEventService.getUserIdToEmitter().remove(messageJson.at("/userId").asText());
        } catch (IOException e) {
          log.error("Error sending user message to user {}", messageJson.at("/userId").asText(), e);
        }
      }
    }
  }
}
