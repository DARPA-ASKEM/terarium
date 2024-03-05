package software.uncharted.terarium.hmiserver.service.tasks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.task.TaskFuture;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

	static public enum TaskMode {
		SYNC("sync"),
		ASYNC("async");

		private final String value;

		TaskMode(final String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}
	}

	// I don't people setting the task id themselves since it may be overridden if
	// the task is already in the cache. So we have this private class to contain
	// the id. This will prevent siutations where someone creates an id, does
	// something with it, and then sends the request expected the response to match
	// it.
	@Accessors(chain = true)
	@NoArgsConstructor
	@Data
	static private class TaskRequestWithId extends TaskRequest {
		private UUID id;

		TaskRequestWithId(final TaskRequest req) {
			id = UUID.randomUUID();
			type = req.getType();
			script = req.getScript();
			input = req.getInput();
			timeoutMinutes = req.getTimeoutMinutes();
			additionalProperties = req.getAdditionalProperties();
		}

		public TaskResponse createResponse(final TaskStatus status) {
			return new TaskResponse()
					.setId(id)
					.setStatus(status)
					.setScript(getScript())
					.setAdditionalProperties(getAdditionalProperties());
		}
	}

	static final private String RESPONSE_CACHE_KEY = "task-service-response-cache";
	static final private String TASK_ID_CACHE_KEY = "task-service-task-id-cache";
	static final private String LOCK_KEY = "task-service-distributed-lock";
	static final private long CACHE_TTL_SECONDS = 60 * 60 * 24; // 24 hours
	static final private long CACHE_MAX_IDLE_SECONDS = 60 * 60 * 2; // 2 hours

	// Always use a lease time for distributed locks to prevent application wide
	// deadlocks. If for whatever reason the lock has not been released within a
	// N seconds, it will automatically free itself.
	static final private long REDIS_LOCK_LEASE_SECONDS = 60; // 1 minute

	private final RabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;
	private final Config config;
	private final ObjectMapper objectMapper;

	private final Map<String, TaskResponseHandler> responseHandlers = new ConcurrentHashMap<>();
	private final Map<UUID, SseEmitter> taskIdToEmitter = new ConcurrentHashMap<>();

	private final Map<UUID, CompletableFuture<TaskResponse>> responseFutures = new ConcurrentHashMap<>();

	private final RedissonClient redissonClient;
	private RMapCache<String, UUID> taskIdCache;
	private RMapCache<UUID, TaskResponse> responseCache;
	private RLock rLock;

	// The queue name that the taskrunner will consume on for requests.
	@Value("${terarium.taskrunner.request-queue}")
	private String TASK_RUNNER_REQUEST_QUEUE;

	// The exchange name that the taskrunner will publish responses to.
	@Value("${terarium.taskrunner.response-exchange}")
	private String TASK_RUNNER_RESPONSE_EXCHANGE;

	// The _shared_ response exchange that each hmi-server instance will consume on.
	// NOTE: messages will round robin between hmi-server instances.
	@Value("${terarium.taskrunner.response-queue}")
	private String TASK_RUNNER_RESPONSE_QUEUE;

	// Once a single instance of the hmi-server has processed a task response, it
	// will publish to this exchange to broadcast the response to all other
	// instances. This will direct the message to all hmi-instances so that the
	// correct instance holding the sse can forward the response to the user.
	@Value("${terarium.taskrunner.response-broadcast-exchange}")
	private String TASK_RUNNER_RESPONSE_BROADCAST_EXCHANGE;

	// The exchange name to publish cancellations to.
	@Value("${terarium.taskrunner.cancellation-exchange}")
	private String TASK_RUNNER_CANCELLATION_EXCHANGE;

	@PostConstruct
	void init() {
		// use a distributed cache and lock so that these can be synchronized across
		// multiple instances of the hmi-server
		responseCache = redissonClient.getMapCache(RESPONSE_CACHE_KEY);
		taskIdCache = redissonClient.getMapCache(TASK_ID_CACHE_KEY);
		rLock = redissonClient.getLock(LOCK_KEY);
	}

	private void declareAndBindTransientQueueWithRoutingKey(final String exchangeName, final String queueName,
			final String routingKey) {
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

			if (latestResp != null &&
					(latestResp.getStatus() == TaskStatus.SUCCESS ||
							latestResp.getStatus() == TaskStatus.FAILED ||
							latestResp.getStatus() == TaskStatus.CANCELLED)) {

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
	@RabbitListener(bindings = @QueueBinding(value = @org.springframework.amqp.rabbit.annotation.Queue(autoDelete = "true", exclusive = "false", durable = "${terarium.taskrunner.durable-queues}"), exchange = @Exchange(value = "${terarium.taskrunner.response-broadcast-exchange}", durable = "${terarium.taskrunner.durable-queues}", autoDelete = "false", type = ExchangeTypes.DIRECT), key = ""), concurrency = "1")
	private void onTaskResponseAllInstanceReceive(final Message message) {
		try {
			final TaskResponse resp = decodeMessage(message, TaskResponse.class);
			if (resp == null) {
				return;
			}

			if (resp.getOutput() != null) {
				log.info("Received response {} for task {}", new String(resp.getOutput()), resp.getId());
			}

			rLock.lock(REDIS_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
			try {
				final CompletableFuture<TaskResponse> future = responseFutures
						.get(resp.getId());
				if (future != null &&
						(resp.getStatus() == TaskStatus.SUCCESS ||
								resp.getStatus() == TaskStatus.CANCELLED ||
								resp.getStatus() == TaskStatus.FAILED)) {
					// complete the future
					log.debug("Completing future for task id {} with status {}", resp.getId(), resp.getStatus());
					future.completeresp;

					// remove the future from the map
					log.debug("Removing future for task id {}", resp.getId());
					responseFutures.remove(resp.getId());
				}
			} catch (final Exception e) {
				log.error("Error occured while writing to response queue for task {}",
						resp.getId(), e);
			} finally {
				rLock.unlock();
			}

			final SseEmitter emitter = taskIdToEmitter.get(resp.getId());
			synchronized (taskIdToEmitter) {
				if (emitter != null) {
					try {
						emitter.sendresp;
					} catch (IllegalStateException | ClientAbortException e) {
						log.warn("Error sending task response for task {}. User likely disconnected",
								resp.getId());
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
	@RabbitListener(bindings = @QueueBinding(value = @org.springframework.amqp.rabbit.annotation.Queue(value = "${terarium.taskrunner.response-queue}", autoDelete = "true", exclusive = "false", durable = "${terarium.taskrunner.durable-queues}"), exchange = @Exchange(value = "${terarium.taskrunner.response-exchange}", durable = "${terarium.taskrunner.durable-queues}", autoDelete = "false", type = ExchangeTypes.DIRECT), key = ""), concurrency = "1")
	private void onTaskResponseOneInstanceReceives(final Message message) {
		try {
			final TaskResponse resp = decodeMessage(message, TaskResponse.class);
			if (resp == null) {
				return;
			}

			try {
				// execute the handler
				if (responseHandlers.containsKey(resp.getScript())) {
					responseHandlers.get(resp.getScript()).handleresp;
				}
			} catch (final Exception e) {
				log.error("Error occured while executing response handler for task {}",
						resp.getId(), e);

				// if the handler fails processing a success, convert it to a failure
				resp.setStatus(TaskStatus.FAILED);
				resp.setOutput(e.getMessage().getBytes());
			}

			rLock.lock(REDIS_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
			try {
				// add to the response cache
				log.debug("Writing response for task id {} for status {} to cache", resp.getId(),
						resp.getStatus());
				responseCache.put(resp.getId(), resp, CACHE_TTL_SECONDS, TimeUnit.SECONDS, CACHE_MAX_IDLE_SECONDS,
						TimeUnit.SECONDS);
			} finally {
				rLock.unlock();
			}

			// once the handler has executed and the response cache is up to date, we now
			// will broadcast to all hmi-server instances to dispatch the clientside events
			broadcastTaskResponseToAllInstancesresp;

		} catch (final Exception e) {
			log.error("Error processing task response message", e);
		}
	}

	private void broadcastTaskResponseToAllInstances(final TaskResponse resp) {
		try {
			final String jsonStr = objectMapper.writeValueAsStringresp;
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
				log.error("Error decoding message as either {} or {}. Raw message is: {}", clazz.getName(),
						JsonNode.class.getName(), message.getBody());
				log.error("", e1);
				return null;
			}
		}
	}

	private TaskFuture runTaskAsyncInternal(final TaskRequest r)
			throws JsonProcessingException {

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
			final UUID existingId = taskIdCache.putIfAbsent(hash, req.getId(), CACHE_TTL_SECONDS, TimeUnit.SECONDS,
					CACHE_MAX_IDLE_SECONDS, TimeUnit.SECONDS);

			if (existingId != null) {
				// a task id already exits for the SHA256, this means the request has already
				// been dispatched.
				log.debug("Task id: {} already exists for SHA: {}", existingId, hash);
				final TaskResponse resp = responseCache.get(existingId);

				// only return responese if they have not failed or been cancelled
				if (resp != null &&
						resp.getStatus() != TaskStatus.CANCELLING &&
						resp.getStatus() != TaskStatus.CANCELLED &&
						resp.getStatus() != TaskStatus.FAILED) {

					// if the response is in the cache, return it
					log.debug("Response for task id: {} with status: {} already exists, returning", existingId,
							resp.getStatus());

					final TaskFuture future = new TaskFuture();
					future.setId(existingId);
					future.setLatestResponseresp;
					if (resp.getStatus() != TaskStatus.SUCCESS) {
						// if the task has not completed, create a future to capture that
						future.setCompleteFuture(
								responseFutures.computeIfAbsent(existingId, v -> new CompletableFuture<>()));
					}
					return future;
				}
				// otherwise dispatch it again
				log.debug("No cached task response found for task id: {} for SHA: {}, creating new task", existingId,
						hash);
			}

			// create the response future
			final CompletableFuture<TaskResponse> completeFuture = new CompletableFuture<>();
			responseFutures.put(req.getId(), completeFuture);

			// no cached request, create the response cache entry
			final TaskResponse queuedResponse = req.createResponse(TaskStatus.QUEUED);
			responseCache.put(req.getId(), queuedResponse, CACHE_TTL_SECONDS, TimeUnit.SECONDS, CACHE_MAX_IDLE_SECONDS,
					TimeUnit.SECONDS);

			// now send request
			final String requestQueue = String.format("%s-%s", TASK_RUNNER_REQUEST_QUEUE, req.getType().toString());

			log.debug("Readying task: {} with SHA: {} to send on queue: {}", req.getId(), hash,
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

				// set the latest response to queued and return
				final TaskFuture future = new TaskFuture();
				future.setId(req.getId());
				future.setCompleteFuture(completeFuture);
				future.setLatestResponse(queuedResponse);
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

	public TaskResponse runTaskAsync(final TaskRequest req)
			throws JsonProcessingException {

		final TaskFuture future = runTaskAsyncInternal(req);
		if (future.getLatestResponse() == null) {
			throw new RuntimeException("Task was not dispatched properly");
		}
		return future.getLatestResponse();
	}

	public TaskResponse runTaskSync(final TaskRequest req, final long timeoutSeconds)
			throws JsonProcessingException, TimeoutException, InterruptedException, ExecutionException {

		// send the request
		final TaskFuture future = runTaskAsyncInternal(req);

		// check the response in case it was cached
		if (future.getLatestResponse().getStatus() == TaskStatus.SUCCESS) {
			log.debug("Task was already completed successfully, returning response with id: {}",
					future.getLatestResponse().getId());
			return future.getLatestResponse();
		}

		// if we are here, then the task is pending

		try {
			// wait for the response
			log.debug("Waiting for response for task id: {}", future.getId());
			final TaskResponse resp = future.getCompleteFuture().get(timeoutSeconds, TimeUnit.SECONDS);
			if (resp.getStatus() == TaskStatus.CANCELLED) {
				throw new InterruptedException("Task was cancelled");
			}
			if (resp.getStatus() == TaskStatus.FAILED) {
				throw new RuntimeException("Task failed: " + new String(resp.getOutput()));
			}
			if (resp.getStatus() != TaskStatus.SUCCESS) {
				throw new RuntimeException("Task did not complete successfully");
			}
			log.debug("Future completed for task: {}", future.getId());
			return resp;
		} catch (final TimeoutException e) {
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
			return runTaskAsync(req);
		} else {
			throw new IllegalArgumentException("Invalid task mode: " + mode);
		}
	}

}
