package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.User;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ServerSentEventService {


  private static final ObjectMapper mapper = new ObjectMapper();

  @Getter
  final Map<String, SseEmitter> userIdToEmitter = new HashMap<>();

  @Data
  @Accessors(chain = true)
  @NoArgsConstructor
  public static class UserClientEvent<T> implements Serializable {
    private String userId;
    private ClientEvent<T> event;
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
   * Decodes a message into a JsonNode
   * @param message the message to decode
   * @return        the decoded message, null if there was an error
   */
  public static <T>T decodeMessage(final Message message, Class<T> type) {
    try {
      return mapper.readValue(message.getBody(), type);
    } catch (IOException e) {
      log.error("Error decoding message", e);
      return null;
    }
  }

  /**
   * Rejects a message
   * @param message the message to reject
   * @param channel the channel to reject the message on
   */
  public static void nack(final Message message, final Channel channel) {
    try {
      channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
    } catch (IOException ex) {
      log.error("Error rejecting message", ex);
    }
  }
}
