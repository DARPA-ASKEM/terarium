package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.notification.NotificationEvent;
import software.uncharted.terarium.hmiserver.models.notification.NotificationGroup;
import software.uncharted.terarium.hmiserver.models.task.TaskFuture;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;
import software.uncharted.terarium.hmiserver.service.ClientEventService;
import software.uncharted.terarium.hmiserver.service.notification.NotificationService;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

	public static enum TaskMode {
		@JsonAlias("sync")
		SYNC("sync"),
		@JsonAlias("async")
		ASYNC("async");

		private final String value;

		TaskMode(final String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}
	}

	// This private subclass is to prevent people from setting the task id
	// themselves since many other resources are tied to the id and it is assumed
	// to be unique.
	@NoArgsConstructor
	@Data
	@EqualsAndHashCode(callSuper = true)
	private static class TaskRequestWithId extends TaskRequest {

		private UUID id;

		TaskRequestWithId(final TaskRequest req) {
			id = UUID.randomUUID();
			type = req.getType();
			script = req.getScript();
			input = req.getInput();
			userId = req.getUserId();
			projectId = req.getProjectId();
			timeoutMinutes = req.getTimeoutMinutes();
			additionalProperties = req.getAdditionalProperties();
		}

		public TaskResponse createResponse(final TaskStatus status, final String stdout, final String stderr) {
			return new TaskResponse()
				.setId(id)
				.setStatus(status)
				.setScript(getScript())
				.setUserId(userId)
				.setProjectId(projectId)
				.setAdditionalProperties(getAdditionalProperties())
				.setStdout(stdout)
				.setStderr(stderr)
				.setRequestSHA256(getSHA256());
		}
	}

	// This private subclass exists to prevent anything outside of this service from
	// mucking with the futures internal state.
	private static class CompletableTaskFuture extends TaskFuture {

		public CompletableTaskFuture(final UUID id) {
			this.id = id;
			this.future = new CompletableFuture<>();
		}

		public CompletableTaskFuture(final TaskRequestWithId req, final TaskResponse resp) {
			this.id = req.getId();

			// We are re-using a cached response, so lets create a TaskResponse from the
			// actual TaskRequest
			// and then copy over the response payload.
			final TaskResponse actualResp = req.createResponse(resp.getStatus(), resp.getStdout(), resp.getStderr());
			actualResp.setOutput(resp.getOutput());

			this.future = new CompletableFuture<>();
			this.future.complete(actualResp);
			this.latestResponse = actualResp;
		}

		public synchronized void complete(final TaskResponse resp) {
			this.latestResponse = resp;
			this.future.complete(resp);
		}
	}

	private static final String RESPONSE_CACHE_KEY = "task-service-response-cache";

	// TTL = Time to live, the maximum time a key will be in the cache before it is
	// evicted, regardless of activity.
	@Value("${terarium.taskrunner.response-cache-ttl-seconds:43200}") // 12 hours
	private long CACHE_TTL_SECONDS;

	// Max idle = The maximum time a key can be idle in the cache before it is
	// evicted.
	@Value("${terarium.taskrunner.response-cache-max-idle-seconds:7200}") // 2 hours
	private long CACHE_MAX_IDLE_SECONDS;

	// Always use a lease time for distributed locks to prevent application wide
	// deadlocks. If for whatever reason the lock has not been released within a
	// N seconds, it will automatically free itself.
	@Value("${terarium.taskrunner.redis-lock-lease-seconds:10}") // 10 seconds
	private long REDIS_LOCK_LEASE_SECONDS;

	private final RabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;
	private final Config config;
	private final ObjectMapper objectMapper;
	private final NotificationService notificationService;
	private final ClientEventService clientEventService;

	private final Map<String, TaskResponseHandler> responseHandlers = new ConcurrentHashMap<>();

	private final RedissonClient redissonClient;

	private RMapCache<String, TaskResponse> responseCache;
	private final Map<UUID, CompletableTaskFuture> futures = new ConcurrentHashMap<>();

	// The queue name that the taskrunner will consume on for requests.
	@Value("${terarium.taskrunner.request-queue}")
	private String TASK_RUNNER_REQUEST_QUEUE;

	// The exchange that the task responses are published to.
	@Value("${terarium.taskrunner.response-exchange}")
	private String TASK_RUNNER_RESPONSE_EXCHANGE;

	// Once a single instance of the hmi-server has processed a task response, it
	// will publish to this exchange to broadcast the response to all other
	// instances. This will direct the message to all hmi-instances so that the
	// correct instance holding the sse can forward the response to the user.
	@Value("${terarium.taskrunner.response-broadcast-exchange}")
	private String TASK_RUNNER_RESPONSE_BROADCAST_EXCHANGE;

	// The exchange name to publish cancellations to.
	@Value("${terarium.taskrunner.cancellation-exchange}")
	private String TASK_RUNNER_CANCELLATION_EXCHANGE;

	private final Environment env;

	private boolean isRunningLocalProfile() {
		final String[] activeProfiles = env.getActiveProfiles();
		for (final String profile : activeProfiles) {
			if ("local".equals(profile)) {
				return true;
			}
		}
		return false;
	}

	@PostConstruct
	void init() {
		// use a distributed cache and lock so that these can be synchronized across
		// multiple instances of the hmi-server
		responseCache = redissonClient.getMapCache(RESPONSE_CACHE_KEY);

		if (isRunningLocalProfile()) {
			// sanity check for local development to clear the caches
			responseCache.clear();
		}
	}

	private void declareAndBindTransientQueueWithRoutingKey(
		final String exchangeName,
		final String queueName,
		final String routingKey
	) {
		// Declare a direct exchange
		final DirectExchange exchange = new DirectExchange(exchangeName, config.getDurableQueues(), false);
		rabbitAdmin.declareExchange(exchange);

		// Declare a queue
		final Queue queue = new Queue(queueName, config.getDurableQueues(), false, true);
		rabbitAdmin.declareQueue(queue);

		// Bind the queue to the exchange with a routing key
		final Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
		rabbitAdmin.declareBinding(binding);
	}

	private void declareQueue(final String queueName) {
		// Declare a queue
		final Queue queue = new Queue(queueName, config.getDurableQueues(), false, false);
		rabbitAdmin.declareQueue(queue);
	}

	public void addResponseHandler(final TaskResponseHandler handler) {
		responseHandlers.put(handler.getName(), handler);
	}

	public void cancelTask(final UUID taskId) {
		// send the cancellation to the task runner
		final String msg = "";
		rabbitTemplate.convertAndSend(TASK_RUNNER_CANCELLATION_EXCHANGE, taskId.toString(), msg);
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
				value = "${terarium.taskrunner.response-broadcast-exchange}",
				durable = "${terarium.taskrunner.durable-queues}",
				autoDelete = "false",
				type = ExchangeTypes.DIRECT
			),
			key = ""
		),
		concurrency = "1"
	)
	private void onTaskResponseAllInstanceReceive(final Message message) {
		try {
			final TaskResponse resp = decodeMessage(message, TaskResponse.class);
			if (resp == null) {
				return;
			}

			log.info("Received response status {} for task {}", resp.getStatus(), resp.getId());
			if (resp.getOutput() != null) {
				log.info("Received response output {} for task {}", new String(resp.getOutput()), resp.getId());
			}

			if (
				resp.getStatus() == TaskStatus.SUCCESS ||
				resp.getStatus() == TaskStatus.CANCELLED ||
				resp.getStatus() == TaskStatus.FAILED
			) {
				final CompletableTaskFuture future = futures.remove(resp.getId());
				if (future != null) {
					log.info("Found promise for task id: {}", resp.getId());
					// complete the future
					log.info("Completing future for task id {} with status {}", resp.getId(), resp.getStatus());
					future.complete(resp);
				} else {
					log.info("Did not find promise for task id: {}", resp.getId());
				}
			} else {
				final CompletableTaskFuture future = futures.get(resp.getId());
				if (future != null) {
					log.info("Updating latest response on task id: {} future to {}", resp.getId(), resp.getStatus());
					future.setLatest(resp);
				}
			}
		} catch (final Exception e) {
			log.error("Error processing task response message", e);
		}
	}

	// This is a shared queue, messages will round robin between every instance of
	// the hmi-server. Any operation that must occur once and only once should be
	// triggered here.
	@RabbitListener(
		bindings = @QueueBinding(
			value = @org.springframework.amqp.rabbit.annotation.Queue(
				value = "${terarium.taskrunner.response-queue}",
				autoDelete = "false",
				exclusive = "false",
				durable = "${terarium.taskrunner.durable-queues}"
			),
			exchange = @Exchange(
				value = "${terarium.taskrunner.response-exchange}",
				durable = "${terarium.taskrunner.durable-queues}",
				autoDelete = "false",
				type = ExchangeTypes.DIRECT
			),
			key = ""
		),
		concurrency = "1"
	)
	private void onTaskResponseOneInstanceReceives(final Message message) {
		try {
			TaskResponse resp = decodeMessage(message, TaskResponse.class);
			if (resp == null) {
				return;
			}

			log.info("Received response status {} for task {}", resp.getStatus(), resp.getId());

			try {
				// execute the handler
				if (responseHandlers.containsKey(resp.getScript())) {
					// handle the response
					resp = responseHandlers.get(resp.getScript()).handle(resp);
				}
			} catch (final Exception e) {
				log.error("Error occured while executing response handler for task {}", resp.getId(), e);

				// if the handler fails processing a success, convert it to a failure
				resp.setStatus(TaskStatus.FAILED);
				resp.setOutput(e.getMessage().getBytes());
			}

			if (resp.getStatus() == TaskStatus.SUCCESS) {
				try {
					// add to the response cache
					log.info(
						"Writing SUCCESS response for task id {} to cache under SHA: {} for script {}",
						resp.getId(),
						resp.getRequestSHA256(),
						resp.getScript()
					);
					responseCache.put(
						resp.getRequestSHA256(),
						resp,
						CACHE_TTL_SECONDS,
						TimeUnit.SECONDS,
						CACHE_MAX_IDLE_SECONDS,
						TimeUnit.SECONDS
					);
				} catch (final Exception e) {
					log.error("Failed to write response to response cache {}", resp.getId(), e);
				}
			}

			try {
				// create the notification event
				final NotificationEvent event = new NotificationEvent();
				event.setData(resp);

				log.info("Creating notification event under group id: {}", resp.getId());

				notificationService.createNotificationEvent(resp.getId(), event);
			} catch (final Exception e) {
				log.error("Failed to persist notification event for for task {}", resp.getId(), e);
			}

			try {
				// send the client event
				final ClientEventType clientEventType = TaskNotificationEventTypes.getTypeFor(resp.getScript());
				log.info("Sending client event with type {} for task {} ", clientEventType.toString(), resp.getId());

				final ClientEvent<TaskResponse> clientEvent = ClientEvent.<TaskResponse>builder()
					.notificationGroupId(resp.getId())
					.projectId(resp.getProjectId())
					.type(clientEventType)
					.data(resp)
					.build();
				clientEventService.sendToUser(clientEvent, resp.getUserId());
			} catch (final Exception e) {
				log.error("Failed to send client event for for task {}", resp.getId(), e);
			}

			// if the task failed, log to stdout / stderr
			if (resp.getStatus() == TaskStatus.FAILED) {
				if (resp.getStdout() != null && resp.getStdout().length() > 0) {
					log.error("Task {} failed, logging stdout", resp.getId());
					System.out.print(resp.getStdout());
				} else {
					log.error("Task {} failed, stdout is empty, nothing to log", resp.getId());
				}
				if (resp.getStderr() != null && resp.getStderr().length() > 0) {
					log.error("Task {} failed, logging stdout", resp.getId());
					System.out.print(resp.getStderr());
				} else {
					log.error("Task {} failed, stderr is empty, nothing to log", resp.getId());
				}
			}

			log.info("Broadcasting task response for task id {} and status {}", resp.getId(), resp.getStatus());

			// once the handler has executed and the response cache is up to date, we now
			// will broadcast to all hmi-server instances to dispatch the clientside events
			broadcastTaskResponseToAllInstances(resp);
		} catch (final Exception e) {
			log.error("Error processing task response message", e);
		}
	}

	private void processCachedTaskResponse(final TaskRequestWithId req, TaskResponse resp) {
		try {
			log.info("Creating notification group under id: {}", req.getId());

			// create the notification group for the task
			final NotificationGroup group = new NotificationGroup();
			group.setId(req.getId()); // use the task id
			group.setType(TaskNotificationEventTypes.getTypeFor(req.getScript()).toString());
			group.setUserId(req.getUserId());
			group.setProjectId(req.getProjectId());

			notificationService.createNotificationGroup(group);
		} catch (final Exception e) {
			log.error("Failed to create notificaiton group for id: {}", req.getId(), e);
		}

		try {
			// execute the handler
			if (responseHandlers.containsKey(resp.getScript())) {
				// handle the response
				resp = responseHandlers.get(resp.getScript()).handle(resp);
			}
		} catch (final Exception e) {
			log.error("Error occured while executing response handler for task {}", resp.getId(), e);

			// if the handler fails processing a success, convert it to a failure
			resp.setStatus(TaskStatus.FAILED);
			resp.setOutput(e.getMessage().getBytes());
		}

		try {
			log.info("Creating notification group under id: {}", req.getId());

			// create the notification group for the task
			final NotificationGroup group = new NotificationGroup();
			group.setId(req.getId()); // use the task id
			group.setType(TaskNotificationEventTypes.getTypeFor(req.getScript()).toString());
			group.setUserId(req.getUserId());
			group.setProjectId(req.getProjectId());

			notificationService.createNotificationGroup(group);
			// create the notification event
			final NotificationEvent event = new NotificationEvent();
			event.setData(resp);

			log.info("Creating notification event under group id: {}", resp.getId());

			notificationService.createNotificationEvent(resp.getId(), event);
		} catch (final Exception e) {
			log.error("Failed to persist notification event for for task {}", resp.getId(), e);
		}

		try {
			// send the client event
			final ClientEventType clientEventType = TaskNotificationEventTypes.getTypeFor(resp.getScript());
			log.info("Sending client event with type {} for task {} ", clientEventType.toString(), resp.getId());

			final ClientEvent<TaskResponse> clientEvent = ClientEvent.<TaskResponse>builder()
				.notificationGroupId(resp.getId())
				.projectId(resp.getProjectId())
				.type(clientEventType)
				.data(resp)
				.build();
			clientEventService.sendToUser(clientEvent, resp.getUserId());
		} catch (final Exception e) {
			log.error("Failed to send client event for for task {}", resp.getId(), e);
		}
	}

	private void broadcastTaskResponseToAllInstances(final TaskResponse resp) {
		try {
			final String jsonStr = objectMapper.writeValueAsString(resp);
			rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_BROADCAST_EXCHANGE, "", jsonStr);
		} catch (final JsonProcessingException e) {
			log.error("Error serializing handler error response", e);
		}
	}

	private static <T> T decodeMessage(final Message message, final Class<T> clazz) {
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

	public TaskFuture runTaskAsync(final TaskRequest r) throws JsonProcessingException {
		if (r.getType() == null) {
			throw new RuntimeException("TaskRequest must have a type set");
		}
		if (r.getScript().isEmpty()) {
			throw new RuntimeException("TaskRequest must have a script set");
		}

		final TaskRequestWithId req = new TaskRequestWithId(r);

		// create sha256 hash of the request
		final String hash = req.getSHA256();

		log.info("Checking for cached response under SHA: {} for {} for script: {}", hash, req.getId(), req.getScript());

		// check if there is an existing response for the hash
		final TaskResponse resp = responseCache.get(hash);

		if (resp != null) {
			// a task id already exits for the SHA256, this means the request has already
			// been dispatched.
			log.info("Task response found in cache for SHA: {}", hash);

			// create and return a completed task future
			final CompletableTaskFuture future = new CompletableTaskFuture(req, resp);

			// process the cached response as if it were a new response
			processCachedTaskResponse(req, future.getLatest());

			return future;
		}

		// no cache entry for task, send a new one

		try {
			log.info("Creating notification group under id: {}", req.getId());

			// create the notification group for the task
			final NotificationGroup group = new NotificationGroup();
			group.setId(req.getId()); // use the task id
			group.setType(TaskNotificationEventTypes.getTypeFor(req.getScript()).toString());
			group.setUserId(req.getUserId());
			group.setProjectId(req.getProjectId());

			notificationService.createNotificationGroup(group);
		} catch (final Exception e) {
			log.error("Failed to create notificaiton group for id: {}", req.getId(), e);
		}

		// now send request
		final String requestQueue = String.format("%s-%s", TASK_RUNNER_REQUEST_QUEUE, req.getType().toString());

		log.info("Readying task: {} with SHA: {} to send on queue: {}", req.getId(), hash, req.getType().toString());

		// ensure the request queue exists
		declareQueue(requestQueue);

		// create the cancellation queue _BEFORE_ sending the request, because a
		// cancellation can be send before the request is consumed on the other end if
		// there is contention. We need this queue to exist to hold the message.
		final String queueName = req.getId().toString();
		final String routingKey = req.getId().toString();
		declareAndBindTransientQueueWithRoutingKey(TASK_RUNNER_CANCELLATION_EXCHANGE, queueName, routingKey);

		try {
			// send the request to the task runner
			log.info("Dispatching request for task id: {}", req.getId());
			final String jsonStr = objectMapper.writeValueAsString(req);
			rabbitTemplate.convertAndSend(requestQueue, jsonStr);

			// publish the queued task response
			final TaskResponse queuedResponse = req.createResponse(TaskStatus.QUEUED, "", "");
			final String respJsonStr = objectMapper.writeValueAsString(queuedResponse);
			rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, "", respJsonStr);

			// create and return the future
			final CompletableTaskFuture future = new CompletableTaskFuture(req.getId());
			future.setLatest(queuedResponse);
			log.info("Adding future for task id: {} to the futures map", req.getId());
			futures.put(req.getId(), future);
			return future;
		} catch (final Exception e) {
			// ensure cancellation queue is removed on failure
			rabbitAdmin.deleteQueue(queueName);
			throw e;
		}
	}

	public TaskResponse runTaskSync(final TaskRequest req)
		throws JsonProcessingException, TimeoutException, InterruptedException, ExecutionException {
		// send the request
		final TaskFuture future = runTaskAsync(req);

		try {
			// wait for the response
			log.info("Waiting for response for task id: {}", future.getId());
			final TaskResponse resp = future.getFinal(req.getTimeoutMinutes(), TimeUnit.MINUTES);
			if (resp.getStatus() == TaskStatus.CANCELLED) {
				throw new InterruptedException("Task was cancelled");
			}
			if (resp.getStatus() == TaskStatus.FAILED) {
				throw new RuntimeException("Task failed: " + new String(resp.getOutput()));
			}
			if (resp.getStatus() != TaskStatus.SUCCESS) {
				throw new RuntimeException("Task did not complete successfully");
			}
			log.info("Future completed for task: {}", future.getId());
			return resp;
		} catch (final TimeoutException e) {
			// remove the future
			futures.remove(future.getId());
			try {
				// if the task is still running, or hasn't started yet, lets cancel it
				cancelTask(future.getId());
			} catch (final Exception ee) {
				log.warn("Failed to cancel task: {}", future.getId(), ee);
			}
			throw new TimeoutException(
				"Task " + future.getId().toString() + " did not complete within " + req.getTimeoutMinutes() + " minutes"
			);
		}
	}

	public TaskResponse runTask(final TaskMode mode, final TaskRequest req)
		throws JsonProcessingException, TimeoutException, InterruptedException, ExecutionException {
		if (mode == TaskMode.SYNC) {
			return runTaskSync(req);
		} else if (mode == TaskMode.ASYNC) {
			return runTaskAsync(req).getLatest();
		} else {
			throw new IllegalArgumentException("Invalid task mode: " + mode);
		}
	}
}
