package software.uncharted.terarium.hmiserver.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.catalina.connector.ClientAbortException;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {

	static public enum TaskType {
		GOLLM("gollm"),
		MIRA("mira");

		private String value;

		TaskType(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}
	}

	private final RabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;
	private final Config config;
	private final ObjectMapper objectMapper;

	private Map<String, TaskResponseHandler> responseHandlers = new ConcurrentHashMap<>();
	private Map<UUID, SseEmitter> taskIdToEmitter = new ConcurrentHashMap<>();

	private Map<UUID, BlockingQueue<TaskResponse>> responseQueues = new ConcurrentHashMap<>();

	@Value("${terarium.taskrunner.request-queue}")
	private String TASK_RUNNER_REQUEST_QUEUE;

	@Value("${terarium.taskrunner.response-exchange}")
	private String TASK_RUNNER_RESPONSE_EXCHANGE;

	@Value("${terarium.taskrunner.cancellation-exchange}")
	private String TASK_RUNNER_CANCELLATION_EXCHANGE;

	private void declareAndBindTransientQueueWithRoutingKey(String exchangeName, String queueName, String routingKey) {
		// Declare a direct exchange
		DirectExchange exchange = new DirectExchange(exchangeName, config.getDurableQueues(), false);
		rabbitAdmin.declareExchange(exchange);

		// Declare a queue
		Queue queue = new Queue(queueName, config.getDurableQueues(), false, true);
		rabbitAdmin.declareQueue(queue);

		// Bind the queue to the exchange with a routing key
		Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
		rabbitAdmin.declareBinding(binding);
	}

	private void declareQueue(String queueName) {
		// Declare a queue
		Queue queue = new Queue(queueName, config.getDurableQueues(), false, false);
		rabbitAdmin.declareQueue(queue);
	}

	public void sendTaskRequest(TaskRequest req, TaskType requestType) throws JsonProcessingException {

		String requestQueue = String.format("%s-%s", TASK_RUNNER_REQUEST_QUEUE, requestType.toString());

		// ensure the request queue exists
		declareQueue(requestQueue);

		// create the cancellation queue _BEFORE_ sending the request, because a
		// cancellation can be send before the request is consumed on the other end if
		// there is contention. We need this queue to exist to hold the message.
		String queueName = req.getId().toString();
		String routingKey = req.getId().toString();
		declareAndBindTransientQueueWithRoutingKey(TASK_RUNNER_CANCELLATION_EXCHANGE, queueName, routingKey);

		try {
			// send the request to the task runner
			log.info("Dispatching request {} on queue: {}", new String(req.getInput()), requestQueue);
			final String jsonStr = objectMapper.writeValueAsString(req);
			rabbitTemplate.convertAndSend(requestQueue, jsonStr);
		} catch (Exception e) {
			rabbitAdmin.deleteQueue(queueName);
			throw e;
		}
	}

	public void addResponseHandler(String script, TaskResponseHandler handler) {
		responseHandlers.put(script, handler);
	}

	public void cancelTask(final UUID taskId) {
		// send the cancellation to the task runner
		final String msg = "";
		rabbitTemplate.convertAndSend(TASK_RUNNER_CANCELLATION_EXCHANGE, msg);
	}

	public SseEmitter subscribe(final UUID taskId) {
		final SseEmitter emitter = new SseEmitter();
		if (taskIdToEmitter.containsKey(taskId)) {
			try {
				taskIdToEmitter.get(taskId).complete();
			} catch (IllegalStateException ignored) {
			}
		}
		taskIdToEmitter.put(taskId, emitter);
		return emitter;
	}

	@RabbitListener(bindings = @QueueBinding(value = @org.springframework.amqp.rabbit.annotation.Queue(autoDelete = "true", exclusive = "false", durable = "${terarium.taskrunner.durable-queues}"), exchange = @Exchange(value = "${terarium.taskrunner.response-exchange}", durable = "${terarium.taskrunner.durable-queues}", autoDelete = "false", type = ExchangeTypes.DIRECT), key = ""))
	void onTaskResponse(final Message message) {
		try {
			TaskResponse resp = decodeMessage(message, TaskResponse.class);
			if (resp == null) {
				return;
			}

			log.info("Received response {} for task {}", new String(resp.getOutput()), resp.getId());

			try {
				if (responseQueues.containsKey(resp.getId())) {
					BlockingQueue<TaskResponse> queue = responseQueues.get(resp.getId());
					queue.put(resp);
				}
			} catch (Exception e) {
				log.error("Error occured while writing to response queue for task {}",
						resp.getId(), e);
			}

			try {
				if (responseHandlers.containsKey(resp.getScript())) {
					responseHandlers.get(resp.getScript()).handle(resp);
				}
			} catch (Exception e) {
				log.error("Error occured while executing response handler for task {}",
						resp.getId(), e);
			}

			final SseEmitter emitter = taskIdToEmitter.get(resp.getId());
			synchronized (taskIdToEmitter) {
				if (emitter != null) {
					try {
						emitter.send(resp);
					} catch (IllegalStateException | ClientAbortException e) {
						log.warn("Error sending task response for task {}. User likely disconnected",
								resp.getId());
						taskIdToEmitter.remove(resp.getId());
					} catch (IOException e) {
						log.error("Error sending task response for task {}", resp.getId(), e);
					}
				}
			}
		} catch (Exception e) {
			log.error("Error processing task response message", e);
		}
	}

	public static <T> T decodeMessage(final Message message, Class<T> clazz) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.readValue(message.getBody(), clazz);
		} catch (Exception e) {
			try {
				JsonNode jsonMessage = mapper.readValue(message.getBody(), JsonNode.class);
				log.error("Unable to parse message as {}. Message: {}", clazz.getName(), jsonMessage.toPrettyString());
				return null;
			} catch (Exception e1) {
				log.error("Error decoding message as either {} or {}. Raw message is: {}", clazz.getName(),
						JsonNode.class.getName(), message.getBody());
				log.error("", e1);
				return null;
			}
		}
	}

	public List<TaskResponse> runTaskBlocking(TaskRequest req, TaskType requestType, long timeoutSeconds)
			throws JsonProcessingException, IOException, InterruptedException {

		if (req.getId() == null) {
			req.setId(UUID.randomUUID());
		}

		try {
			// add to queue to wait on responses
			BlockingQueue<TaskResponse> queue = new ArrayBlockingQueue<>(8);
			responseQueues.put(req.getId(), queue);

			// send the request
			sendTaskRequest(req, requestType);

			// add the queued response
			List<TaskResponse> responses = new ArrayList<>();
			TaskResponse resp = req.createResponse(TaskStatus.QUEUED);
			queue.put(resp);

			while (true) {
				// wait for responses
				TaskResponse response = queue.poll(timeoutSeconds, TimeUnit.SECONDS);
				if (response == null) {
					throw new InterruptedException("Task did not complete within " + timeoutSeconds + " seconds");
				}

				log.info("Response id: {} status {}", response.getId(), response.getStatus());
				responses.add(response);

				if (response.getStatus() == TaskStatus.SUCCESS) {
					return responses;
				}

				if (response.getStatus() == TaskStatus.CANCELLED) {
					throw new InterruptedException("Task was cancelled");
				}

				if (response.getStatus() == TaskStatus.CANCELLED || response.getStatus() == TaskStatus.FAILED) {
					throw new IOException("Task failed: " + new String(response.getOutput()));
				}
			}
		} finally {
			// ensure we remove it from the queue when done
			responseQueues.remove(req.getId());
		}
	}

	public List<TaskResponse> runTaskBlocking(TaskRequest req, TaskType requestType)
			throws JsonProcessingException, IOException, InterruptedException {
		return runTaskBlocking(req, requestType, 60);
	}

}
