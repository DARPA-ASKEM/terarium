package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.User;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientEventService{

  private final ServerSentEventService serverSentEventService;

  private final ObjectMapper mapper = new ObjectMapper();
  private static final String CLIENT_USER_EVENT_QUEUE = "clientUserEventQueue";
  private static final String CLIENT_ALL_USERS_EVENT_QUEUE = "clientAllUsersEventQueue";


  private final Config config;
  private final RabbitTemplate rabbitTemplate;
  private final RabbitAdmin rabbitAdmin;

  Queue allUsersQueue;
  Queue userQueue;

  @PostConstruct
  void init() {
    allUsersQueue = new Queue(CLIENT_ALL_USERS_EVENT_QUEUE, config.getDurableQueues());
    rabbitAdmin.declareQueue(allUsersQueue);

    userQueue = new Queue(CLIENT_USER_EVENT_QUEUE, config.getDurableQueues());
    rabbitAdmin.declareQueue(userQueue);
  }

  /**
   * Sends a message to all users
   * @param event the event to send
   * @param <T>   the type of the event
   */
  public <T> void sendToAllUsers(final ClientEvent<T> event) {
    try {
      final String jsonStr = mapper.writeValueAsString(event);
      rabbitTemplate.convertAndSend(allUsersQueue.getName(), jsonStr);
    } catch (IOException e) {
      log.error("Error sending all users message", e);
    }
  }

  /**
   * Sends a message to a user
   * @param event   the event to send
   * @param userId  the id of the user to send the message to
   * @param <T>     the type of the event
   */
  public <T> void sendToUser(final ClientEvent<T> event, final String userId) {
    try {
      final String jsonStr = mapper.writeValueAsString(new ServerSentEventService.UserClientEvent<T>().setEvent(event).setUserId(userId));
      rabbitTemplate.convertAndSend(userQueue.getName(), jsonStr);
    } catch (JsonProcessingException e) {
      log.error("Error sending all users message", e);
    }
  }

  /**
   * Send the message to all users connected
   * @param message the message to send
   * @param channel the channel to send the message on
   */
  @RabbitListener(
    queues = {CLIENT_ALL_USERS_EVENT_QUEUE},
    exclusive = true,
    concurrency = "1")
  private void onSendToAllUsersEvent(final Message message, final Channel channel) {
    final JsonNode messageJson = ServerSentEventService.decodeMessage(message, JsonNode.class);
    if (messageJson == null) {
      //ServerSentEventService.nack(message, channel);
      return;
    }
    synchronized (serverSentEventService.getUserIdToEmitter()) {
      // Send the message to each user connected and remove disconnected users
      final Set<String> userIdsToRemove = new HashSet<>();
      serverSentEventService.getUserIdToEmitter().forEach((userId, emitter) -> {
        try {
          emitter.send(messageJson);
        } catch (IllegalStateException | ClientAbortException e) {
          log.warn("Error sending all users message to user {}. User likely disconnected", userId);
          userIdsToRemove.add(userId);
        } catch (IOException e) {
          log.error("Error sending all users message to user {}", userId, e);
        }
      });
      // Clean up and remove disconnected users
      userIdsToRemove.forEach(serverSentEventService.getUserIdToEmitter()::remove);
    }
  }

  /**
   * Listens for messages to send to a user and if we have the SSE connection, send it
   * @param message       the message to send
   * @param channel       the channel to send the message on
   * @throws IOException  if there was an error sending the message
   */
  @RabbitListener(
    queues = {CLIENT_USER_EVENT_QUEUE},
    exclusive = true,
    concurrency = "1")
  private void onSendToUserEvent(final Message message, final Channel channel) throws IOException {
    final JsonNode messageJson = ServerSentEventService.decodeMessage(message, JsonNode.class);
    if (messageJson == null) {
      ServerSentEventService.nack(message, channel);
      return;
    }
    final SseEmitter emitter = serverSentEventService.getUserIdToEmitter().get(messageJson.at("/userId").asText());
    synchronized (serverSentEventService.getUserIdToEmitter()) {
      final String userId = messageJson.at("/userId").asText();
      if (emitter != null) {
        try {
          emitter.send(messageJson.at("/event"));
        } catch (IllegalStateException | ClientAbortException e) {
          log.warn("Error sending user message to user {}. User likely disconnected", messageJson.at("/userId").asText());
          serverSentEventService.getUserIdToEmitter().remove(userId);
        } catch (IOException e) {
          log.error("Error sending user message to user {}", messageJson.at("/userId").asText(), e);
        }
      }
    }
  }

  @Scheduled(fixedDelay = 5000)
  public void testSendToAll() {
    final ClientEvent<String> event = new ClientEvent<>();
    event.setType(ClientEventType.NOTIFICATION)
      .setData("Hello, world!");

    sendToAllUsers(event);
  }

  @Scheduled(fixedDelay = 5000)
  public void testSendToUser() {
    // Get a random connected userid
    final String[] userIds = serverSentEventService.getUserIdToEmitter().keySet().toArray(new String[0]);
    if (userIds.length == 0) {
      return;
    }
    final String userId = userIds[(int) (Math.random() * userIds.length)];

    final ClientEvent<String> event = new ClientEvent<>();
    event.setType(ClientEventType.NOTIFICATION)
      .setData("Hello user " + userId + "!");

    sendToUser(event, userId);
  }
}
