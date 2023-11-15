package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.User;


import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientEventService{
  private final ObjectMapper mapper;
  private final RabbitTemplate rabbitTemplate;
  private final RabbitAdmin rabbitAdmin;
  private final Config config;

  @Value("${terarium.client-user-event-queue}")
  private String CLIENT_USER_EVENT_QUEUE;

  @Value("${terarium.client-all-user-event-queue}")
  private String CLIENT_ALL_USERS_EVENT_QUEUE;

  @Value("${terarium.queue.suffix:${terarium.userqueue.suffix}}")
  private String queueSuffix;

  @Value("${terarium.queue.suffix:#{null}}")
  private String isUserDev;



  final Map<String, SseEmitter> userIdToEmitter = new ConcurrentHashMap<>();

  Queue allUsersQueue;
  Queue userQueue;

  @Data
  @Accessors(chain = true)
  @NoArgsConstructor
  public static class UserClientEvent<T> implements Serializable {
    private String userId;
    private ClientEvent<T> event;
  }


  @PostConstruct
  void init() {
    allUsersQueue = new Queue(CLIENT_ALL_USERS_EVENT_QUEUE+queueSuffix, config.getDurableQueues(), false, isUserDev == null);
    rabbitAdmin.declareQueue(allUsersQueue);

    userQueue = new Queue(CLIENT_USER_EVENT_QUEUE+queueSuffix, config.getDurableQueues(), false, isUserDev == null);
    rabbitAdmin.declareQueue(userQueue);
  }

  /**
   * Connects a user to the SSE service
   * @param user  the user to connect
   * @return      the emitter to send messages to the user
   */
  public SseEmitter connect(final User user) {
    final SseEmitter emitter = new SseEmitter();
    if (userIdToEmitter.containsKey(user.getId())) {
      try {
        userIdToEmitter.get(user.getId()).complete();
      } catch (IllegalStateException ignored) { }
    }
    userIdToEmitter.put(user.getId(), emitter);
    return emitter;
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
      final String jsonStr = mapper.writeValueAsString(new UserClientEvent<T>().setEvent(event).setUserId(userId));
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
          queues = "${terarium.client-all-user-event-queue}${terarium.queue.suffix:${terarium.userqueue.suffix}}",
          concurrency = "1")
  void onSendToAllUsersEvent(final Message message, final Channel channel) {
    final JsonNode messageJson = decodeMessage(message, JsonNode.class);
    if (messageJson == null) {
      return;
    }
    synchronized (userIdToEmitter) {
      // Send the message to each user connected and remove disconnected users
      final Set<String> userIdsToRemove = new HashSet<>();
      userIdToEmitter.forEach((userId, emitter) -> {
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
      userIdsToRemove.forEach(userIdToEmitter::remove);
    }
  }

  /**
   * Lisens for messages to send to a user and if we have the SSE connection, send it
   * @param message       the message to send
   * @param channel       the channel to send the message on
   * @throws IOException  if there was an error sending the message
   */
  @RabbitListener(
          queues = {"${terarium.client-user-event-queue}${terarium.queue.suffix:${terarium.userqueue.suffix}}"},
          concurrency = "1")
  void onSendToUserEvent(final Message message, final Channel channel) throws IOException {
    final JsonNode messageJson = decodeMessage(message, JsonNode.class);
    if (messageJson == null) {
      return;
    }
    final SseEmitter emitter = userIdToEmitter.get(messageJson.at("/userId").asText());
    synchronized (userIdToEmitter) {
      if (emitter != null) {
        final String userId = messageJson.at("/userId").asText();
        try {
          emitter.send(messageJson.at("/event"));
        } catch (IllegalStateException | ClientAbortException e) {
          log.warn("Error sending user message to user {}. User likely disconnected", userId);
          userIdToEmitter.remove(userId);
        } catch (IOException e) {
          log.error("Error sending user message to user {}", userId, e);
        }
      }
    }
  }

	/**
	 * Decodes a message into the given class. If there is an issue parsing to this class we will attempt to just
	 * parse it as a JsonNode and log the error. If that fails we will log the error and hope for the best
	 * @param message the message to decode
	 * @param clazz  the class to decode the message to
	 * @return 		 the decoded message or null if there was an error
	 * @param <T>
	 */
	public static <T> T decodeMessage(final Message message, Class<T> clazz) {

		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.readValue(message.getBody(), clazz);
		} catch (Exception e) {
			try {
				JsonNode jsonMessage =  mapper.readValue(message.getBody(), JsonNode.class);
				log.error("Unable to parse message as {}. Message: {}", clazz.getName(), jsonMessage.toPrettyString());
				return null;
			} catch (Exception e1) {
				log.error("Error decoding message as either {} or {}. Raw message is: {}", clazz.getName(), JsonNode.class.getName(), message.getBody());
				log.error("",e1);
				return null;
			}
		}
	}

  /**
   * Heartbeat to ensure that the clients are subscribed to the SSE service. If the client does
   * not receive a heartbeat within the configured interval, it will attempt to reconnect.
   */
  @Scheduled(fixedDelayString = "${terarium.clientConfig.sseHeartbeatIntervalMillis}")
  public void sendHeartbeat() {
    final ClientEvent<Void> event = ClientEvent.<Void>builder()
            .type(ClientEventType.HEARTBEAT)
            .data(null)
            .build();
    sendToAllUsers(event);
  }

}
