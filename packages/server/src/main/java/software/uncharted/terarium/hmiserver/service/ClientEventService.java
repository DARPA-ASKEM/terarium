package software.uncharted.terarium.hmiserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.authority.User;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientEventService {

	private final ObjectMapper mapper;
	private final RabbitTemplate rabbitTemplate;

	private static final String CLIENT_USER_EVENT_EXCHANGE = "client-user-event-exchange";
	private static final String CLIENT_ALL_USERS_EVENT_EXCHANGE = "client-all-users-event-exchange";

	final ClientEvent<Void> HEART_BEAT_EVENT = ClientEvent.<Void>builder()
		.type(ClientEventType.HEARTBEAT)
		.data(null)
		.build();

	/**
	 * Map of user id to the emitters for that user. Users can have multiple emitters if they have multiple tabs open or
	 * multiple devices connected
	 */
	final Map<String, java.util.Queue<SseEmitter>> userIdToEmitters = new ConcurrentHashMap<>();

	@Data
	@Accessors(chain = true)
	@NoArgsConstructor
	public static class UserClientEvent<T> implements Serializable {

		@Serial
		private static final long serialVersionUID = -7617118669979761035L;

		private String userId;
		private ClientEvent<T> event;
	}

	/**
	 * Connects a user to the SSE service
	 *
	 * @param user the user to connect
	 * @return the emitter to send messages to the user
	 */
	public SseEmitter connect(final User user) {
		final java.util.Queue<SseEmitter> emitters = userIdToEmitters.getOrDefault(
			user.getId(),
			new ConcurrentLinkedQueue<>()
		);
		final SseEmitter emitter = new SseEmitter();
		emitter.onError(e -> {
			emitter.complete();
			emitters.remove(emitter);
			if (emitters.isEmpty()) {
				userIdToEmitters.remove(user.getId());
			}
		});
		try {
			emitter.send(HEART_BEAT_EVENT);
		} catch (final IOException e) {
			log.error("Error sending init heartbeat", e);
		}
		emitters.add(emitter);
		userIdToEmitters.put(user.getId(), emitters);
		return emitter;
	}

	/**
	 * Disconnects a user from the sse service. Completes all emitters for the user and removes them
	 *
	 * @param user the user to disconnect
	 */
	public void disconnect(final User user) {
		final java.util.Queue<SseEmitter> userEmitters = userIdToEmitters.get(user.getId());
		if (userEmitters != null) {
			try {
				userEmitters.forEach(SseEmitter::complete);
			} catch (final Exception e) {
				log.error("Error disconnecting user", e);
			}
			userIdToEmitters.remove(user.getId());
		}
	}

	/**
	 * Sends a message to all users
	 *
	 * @param event the event to send
	 * @param <T> the type of the event
	 */
	public <T> void sendToAllUsers(final ClientEvent<T> event) {
		try {
			final String jsonStr = mapper.writeValueAsString(event);
			rabbitTemplate.convertAndSend(CLIENT_ALL_USERS_EVENT_EXCHANGE, "", jsonStr);
		} catch (final IOException e) {
			log.error("Error sending all users message", e);
		}
	}

	/**
	 * Sends a message to a user
	 *
	 * @param event the event to send
	 * @param userId the id of the user to send the message to
	 * @param <T> the type of the event
	 */
	public <T> void sendToUser(final ClientEvent<T> event, final String userId) {
		try {
			final String jsonStr = mapper.writeValueAsString(new UserClientEvent<T>().setEvent(event).setUserId(userId));
			rabbitTemplate.convertAndSend(CLIENT_USER_EVENT_EXCHANGE, "", jsonStr);
		} catch (final JsonProcessingException e) {
			log.error("Error sending all users message", e);
		}
	}

	public <T> void sendToUsers(final ClientEvent<T> event, final Collection<String> userIds) {
		for (final String userId : userIds) {
			sendToUser(event, userId);
		}
	}

	/**
	 * Send the message to all users connected
	 *
	 * @param message the message to send
	 * @param channel the channel to send the message on
	 */
	@RabbitListener(
		bindings = @QueueBinding(
			value = @Queue(
				value = "#{T(java.util.UUID).randomUUID().toString().concat('-all-users-event-queue')}",
				durable = "false",
				autoDelete = "true"
			),
			exchange = @Exchange(value = CLIENT_ALL_USERS_EVENT_EXCHANGE, type = "fanout")
		)
	)
	void onSendToAllUsersEvent(final Message message, final Channel channel) {
		final JsonNode messageJson = decodeMessage(message, JsonNode.class);
		if (messageJson == null) {
			return;
		}
		// Send the message to each user connected and remove disconnected users
		userIdToEmitters.forEach((userId, emitters) -> {
			final Set<SseEmitter> emittersToRemove = ConcurrentHashMap.newKeySet();
			for (final SseEmitter emitter : emitters) {
				try {
					emitter.send(messageJson);
				} catch (final Exception e) {
					log.warn("Error sending all users message to user {}. User likely disconnected", userId);
					emittersToRemove.add(emitter);
				}
			}
			emitters.removeAll(emittersToRemove);
		});

		// Remove users with no emitters
		userIdToEmitters.entrySet().removeIf(entry -> entry.getValue().isEmpty());
	}

	/**
	 * Lisens for messages to send to a user and if we have the SSE connection, send it
	 *
	 * @param message the message to send
	 * @param channel the channel to send the message on
	 * @throws IOException if there was an error sending the message
	 */
	@RabbitListener(
		bindings = @QueueBinding(
			value = @Queue(
				value = "#{T(java.util.UUID).randomUUID().toString().concat('-user-event-queue')}",
				durable = "false",
				autoDelete = "true"
			),
			exchange = @Exchange(value = CLIENT_USER_EVENT_EXCHANGE, type = "fanout")
		)
	)
	void onSendToUserEvent(final Message message, final Channel channel) throws IOException {
		final JsonNode messageJson = decodeMessage(message, JsonNode.class);
		if (messageJson == null) {
			return;
		}

		final String userId = messageJson.at("/userId").asText();
		final java.util.Queue<SseEmitter> emitters = userIdToEmitters.get(userId);
		if (emitters != null) {
			final Set<SseEmitter> emittersToRemove = ConcurrentHashMap.newKeySet();
			for (final SseEmitter emitter : emitters) {
				try {
					emitter.send(messageJson.at("/event"));
				} catch (final Exception e) {
					log.warn("Error sending user message to user {}. User likely disconnected", userId);
					emittersToRemove.add(emitter);
				}
			}
			emitters.removeAll(emittersToRemove);
		}

		// Remove users with no emitters
		userIdToEmitters.entrySet().removeIf(entry -> entry.getValue().isEmpty());
	}

	/**
	 * Decodes a message into the given class. If there is an issue parsing to this class we will attempt to just parse
	 * it as a JsonNode and log the error. If that fails we will log the error and hope for the best
	 *
	 * @param message the message to decode
	 * @param clazz the class to decode the message to
	 * @return the decoded message or null if there was an error
	 * @param <T>
	 */
	public static <T> T decodeMessage(final Message message, final Class<T> clazz) {
		final ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(message.getBody(), clazz);
		} catch (final Exception e) {
			try {
				final JsonNode jsonMessage = mapper.readValue(message.getBody(), JsonNode.class);
				log.error("Unable to parse message as {}. Message: {}", clazz.getName(), jsonMessage.toPrettyString());
				return null;
			} catch (final Exception e1) {
				log.error(
					"Error decoding message as either {} or {}. Raw message is: {}",
					clazz.getName(),
					JsonNode.class.getName(),
					message.getBody()
				);
				log.error("", e1);
				return null;
			}
		}
	}

	/**
	 * Heartbeat to ensure that the clients are subscribed to the SSE service. If the client does not receive a
	 * heartbeat within the configured interval, it will attempt to reconnect.
	 */
	@Scheduled(fixedDelay = 5000L)
	public void sendHeartbeat() {
		sendToAllUsers(HEART_BEAT_EVENT);
	}
}
