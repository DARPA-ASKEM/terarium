package software.uncharted.terarium.hmiserver.service.tasks;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
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
import org.apache.catalina.connector.ClientAbortException;
import org.redisson.api.RLock;
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
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
	// themselves since it may be overridden if the task is already in the cache.
	// This will prevent situations where someone creates an id, does something with
	// it, and then sends the request expected the response to match it.
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

		public TaskResponse createResponse(final TaskStatus status) {
			return new TaskResponse()
					.setId(id)
					.setStatus(status)
					.setUserId(userId)
					.setProjectId(projectId)
					.setScript(getScript())
					.setAdditionalProperties(getAdditionalProperties());
		}
	}

	// This private subclass exists to prevent anything outside of this service from
	// mucking with the futures internal state.
	private static class CompletableTaskFuture extends TaskFuture {

		public CompletableTaskFuture(final UUID id, final TaskResponse resp) {
			this.id = id;
			this.latestResponse = resp;
			this.future = new CompletableFuture<>();
			if (this.latestResponse.getStatus() == TaskStatus.SUCCESS) {
				// if the task has already completed successfully, but the future is being
				// created then complete it. This would occur if another instance of the
				// hmi-server has already dispatched and processed the task response.
				future.complete(this.latestResponse);
			}
		}

		public synchronized void update(final TaskResponse resp) {
			if (future.isDone()) {
				throw new IllegalStateException("TaskFuture is already complete");
			}
			latestResponse = resp;
		}

		public synchronized void complete(final TaskResponse resp) {
			latestResponse = resp;
			future.complete(resp);
		}
	}

	private static final String RESPONSE_CACHE_KEY = "task-service-response-cache";
	private static final String TASK_ID_CACHE_KEY = "task-service-task-id-cache";
	private static final String LOCK_KEY = "task-service-distributed-lock";

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
	private final Map<UUID, SseEmitter> taskIdToEmitter = new ConcurrentHashMap<>();

	private final RedissonClient redissonClient;

	// NOTE: We require a distributed lock to keep the following three caches in
	// sync across instances. Anytime these caches are written or read from, the
	// lock must be acquired.
	// DO NOT TOUCH THESE CACHES, UNLESS YOU KNOW WHAT YOU ARE DOING.
	// There be dragons.
	// vvvvvvvvvvvvvvvvvvv
	private RLock rLock;
	private RMapCache<String, UUID> taskIdCache;
	private RMapCache<UUID, TaskResponse> responseCache;
	private final Map<UUID, CompletableTaskFuture> futures = new HashMap<>();
	// ^^^^^^^^^^^^^^^^^^^

	// The queue name that the taskrunner will consume on for requests.
	@Value("${terarium.taskrunner.request-queue}")
	private String TASK_RUNNER_REQUEST_QUEUE;

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
		taskIdCache = redissonClient.getMapCache(TASK_ID_CACHE_KEY);
		rLock = redissonClient.getLock(LOCK_KEY);

		if (isRunningLocalProfile()) {
			// sanity check for local development to clear the caches
			rLock.lock();
			responseCache.clear();
			taskIdCache.clear();
			rLock.unlock();
		}
	}

	private void declareAndBindTransientQueueWithRoutingKey(
			final String exchangeName, final String queueName, final String routingKey) {
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

	public SseEmitter subscribe(final UUID taskId) {
		final SseEmitter emitter = new SseEmitter();
		if (taskIdToEmitter.containsKey(taskId)) {
			try {
				taskIdToEmitter.get(taskId).complete();
			} catch (final IllegalStateException ignored) {
			}
		}

		try {
			rLock.lock(REDIS_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);

			taskIdToEmitter.put(taskId, emitter);

			final TaskResponse latestResp = responseCache.get(taskId);

			if (latestResp != null
					&& (latestResp.getStatus() == TaskStatus.SUCCESS
							|| latestResp.getStatus() == TaskStatus.FAILED
							|| latestResp.getStatus() == TaskStatus.CANCELLED)) {

				// if this task has already resolved, send the response to the emitter
				emitter.send(latestResp);
			}
		} catch (final Exception e) {
			log.error("Error occured while attemping to send latest response to emitter", e);
		} finally {
			rLock.unlock();
		}

		return emitter;
	}

	// This is an anonymous queue, every instance the hmi-server will receive a
	// message. Any operation that must occur on _every_ instance of the hmi-server
	// should be triggered here.
	@RabbitListener(
			bindings =
					@QueueBinding(
							value =
									@org.springframework.amqp.rabbit.annotation.Queue(
											autoDelete = "true",
											exclusive = "false",
											durable = "${terarium.taskrunner.durable-queues}"),
							exchange =
									@Exchange(
											value = "${terarium.taskrunner.response-broadcast-exchange}",
											durable = "${terarium.taskrunner.durable-queues}",
											autoDelete = "false",
											type = ExchangeTypes.DIRECT),
							key = ""),
			concurrency = "1")
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

			rLock.lock(REDIS_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
			try {
				final CompletableTaskFuture future = futures.get(resp.getId());
				if (future != null) {
					if (resp.getStatus() == TaskStatus.SUCCESS
							|| resp.getStatus() == TaskStatus.CANCELLED
							|| resp.getStatus() == TaskStatus.FAILED) {
						// complete the future
						log.info("Completing future for task id {} with status {}", resp.getId(), resp.getStatus());
						future.complete(resp);

						// remove the future from the map
						futures.remove(resp.getId());
					} else {
						// update the future with the latest response
						future.update(resp);
					}
				}
			} catch (final Exception e) {
				log.error("Error occured while writing to response queue for task {}", resp.getId(), e);
			} finally {
				rLock.unlock();
			}

			// if the task failed, lets log the stdout / stderr
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

			final SseEmitter emitter = taskIdToEmitter.get(resp.getId());
			synchronized (taskIdToEmitter) {
				if (emitter != null) {
					try {
						emitter.send(resp);
					} catch (IllegalStateException | ClientAbortException e) {
						log.warn("Error sending task response for task {}. User likely disconnected", resp.getId());
						taskIdToEmitter.remove(resp.getId());
					} catch (final IOException e) {
						log.error("Error sending task response for task {}", resp.getId(), e);
					}
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
			bindings =
					@QueueBinding(
							value =
									@org.springframework.amqp.rabbit.annotation.Queue(
											value = "${terarium.taskrunner.response-queue}",
											autoDelete = "false",
											exclusive = "false",
											durable = "${terarium.taskrunner.durable-queues}"),
							exchange =
									@Exchange(
											value = "${terarium.taskrunner.response-exchange}",
											durable = "${terarium.taskrunner.durable-queues}",
											autoDelete = "false",
											type = ExchangeTypes.DIRECT),
							key = ""),
			concurrency = "1")
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

			rLock.lock(REDIS_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
			try {
				// add to the response cache
				log.info("Writing response for task id {} for status {} to cache", resp.getId(), resp.getStatus());
				responseCache.put(
						resp.getId(),
						resp,
						CACHE_TTL_SECONDS,
						TimeUnit.SECONDS,
						CACHE_MAX_IDLE_SECONDS,
						TimeUnit.SECONDS);
			} finally {
				rLock.unlock();
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
						.type(clientEventType)
						.data(resp)
						.build();
				clientEventService.sendToUser(clientEvent, resp.getUserId());

			} catch (final Exception e) {
				log.error("Failed to send client event for for task {}", resp.getId(), e);
			}

			log.info("Broadcasting task response for task id {} and status {}", resp.getId(), resp.getStatus());

			// once the handler has executed and the response cache is up to date, we now
			// will broadcast to all hmi-server instances to dispatch the clientside events
			broadcastTaskResponseToAllInstances(resp);

		} catch (final Exception e) {
			log.error("Error processing task response message", e);
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
						message.getBody());
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

		rLock.lock(REDIS_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
		try {
			// generate the task id
			final TaskRequestWithId req = new TaskRequestWithId(r);

			// create sha256 hash of the request
			final String hash = req.getSHA256();

			// check if there is an id associated with the hash of the request already
			final UUID existingId = taskIdCache.putIfAbsent(
					hash, req.getId(), CACHE_TTL_SECONDS, TimeUnit.SECONDS, CACHE_MAX_IDLE_SECONDS, TimeUnit.SECONDS);

			if (existingId != null) {
				// a task id already exits for the SHA256, this means the request has already
				// been dispatched.
				log.info("Task id: {} found in cache for SHA: {}", existingId, hash);
				final TaskResponse resp = responseCache.get(existingId);

				// only return responese if they have not failed or been cancelled
				if (resp != null
						&& resp.getStatus() != TaskStatus.CANCELLING
						&& resp.getStatus() != TaskStatus.CANCELLED
						&& resp.getStatus() != TaskStatus.FAILED) {

					// if the response is in the cache, return it
					log.info("Response for task id: {} with status: {} found in cache", existingId, resp.getStatus());

					if (!futures.containsKey(existingId)) {
						// create the future if need be
						final CompletableTaskFuture future = new CompletableTaskFuture(existingId, resp);
						futures.put(existingId, future);
						return future;
					}

					// future already exists on this instance
					return futures.get(existingId);
				}

				// otherwise dispatch it again, and overwrite the id
				log.info(
						"No viable cached response found for task id: {} for SHA: {}, creating new task with id {}",
						existingId,
						hash,
						req.getId());

				taskIdCache.put(
						hash,
						req.getId(),
						CACHE_TTL_SECONDS,
						TimeUnit.SECONDS,
						CACHE_MAX_IDLE_SECONDS,
						TimeUnit.SECONDS);
			}

			try {
				log.info("Creating notification group under id: {}", req.getId());

				// create the notification group for the task
				final NotificationGroup group = new NotificationGroup();
				group.setId(req.getId()); // use the task id
				group.setType(
						TaskNotificationEventTypes.getTypeFor(req.getScript()).toString());
				group.setUserId(req.getUserId());
				group.setProjectId(req.getProjectId());

				notificationService.createNotificationGroup(group);

			} catch (final Exception e) {
				log.error("Failed to create notificaiton group for id: {}", req.getId(), e);
			}

			// now send request
			final String requestQueue = String.format(
					"%s-%s", TASK_RUNNER_REQUEST_QUEUE, req.getType().toString());

			log.info(
					"Readying task: {} with SHA: {} to send on queue: {}",
					req.getId(),
					hash,
					req.getType().toString());

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
				log.info("Dispatching request: {} for task id: {}", new String(req.getInput()), req.getId());
				final String jsonStr = objectMapper.writeValueAsString(req);
				rabbitTemplate.convertAndSend(requestQueue, jsonStr);

				// put the response in redis after it is queued in the case the id is reserved
				// but the server is shutdown before it dispatches the request, which would
				// cause servers to wait on requests that were never sent.
				final TaskResponse queuedResponse = req.createResponse(TaskStatus.QUEUED);
				responseCache.put(
						req.getId(),
						queuedResponse,
						CACHE_TTL_SECONDS,
						TimeUnit.SECONDS,
						CACHE_MAX_IDLE_SECONDS,
						TimeUnit.SECONDS);

				// create and return the future
				final CompletableTaskFuture future = new CompletableTaskFuture(req.getId(), queuedResponse);
				futures.put(req.getId(), future);
				return future;

			} catch (final Exception e) {
				// ensure cancellation queue is removed on failure
				rabbitAdmin.deleteQueue(queueName);
				throw e;
			}

		} finally {
			rLock.unlock();
		}
	}

	public TaskResponse runTaskSync(final TaskRequest req, final long timeoutSeconds)
			throws JsonProcessingException, TimeoutException, InterruptedException, ExecutionException {

		// send the request
		final TaskFuture future = runTaskAsync(req);

		try {
			// wait for the response
			log.info("Waiting for response for task id: {}", future.getId());
			final TaskResponse resp = future.get(timeoutSeconds, TimeUnit.SECONDS);
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

			// if we time out, something has probably gone wrong, lets remove it from the
			// SHA lookup
			rLock.lock(REDIS_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
			try {
				// check if there is an id associated with the hash of the request already
				taskIdCache.remove(req.getSHA256());
			} finally {
				rLock.unlock();
			}

			// if the task is still running, or hasn't started yet, lets cancel it
			cancelTask(future.getId());

			throw new TimeoutException(
					"Task " + future.getId().toString() + " did not complete within " + timeoutSeconds + " seconds");
		}
	}

	public TaskResponse runTaskSync(final TaskRequest req)
			throws JsonProcessingException, TimeoutException, ExecutionException, InterruptedException {

		final int DEFAULT_TIMEOUT_SECONDS = 60 * 5; // 5 minutes
		return runTaskSync(req, DEFAULT_TIMEOUT_SECONDS);
	}

	public TaskResponse runTask(final TaskMode mode, final TaskRequest req)
			throws JsonProcessingException, TimeoutException, InterruptedException, ExecutionException {
		if (mode == TaskMode.SYNC) {
			return runTaskSync(req);
		} else if (mode == TaskMode.ASYNC) {
			// return the latest received response held in the future
			return runTaskAsync(req).poll();
		} else {
			throw new IllegalArgumentException("Invalid task mode: " + mode);
		}
	}
}
