package software.uncharted.terarium.hmiserver.controller.task;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.annotations.IgnoreRequestLogging;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.hmiserver.models.task.TaskResponse;
import software.uncharted.terarium.hmiserver.models.task.TaskStatus;

@Slf4j
public class TaskController {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private RabbitAdmin rabbitAdmin;

	@Autowired
	protected Config config;

	@Autowired
	protected ObjectMapper objectMapper;

	Map<String, TaskResponseHandler> responseHandlers = new ConcurrentHashMap<>();
	Map<UUID, SseEmitter> taskIdToEmitter = new ConcurrentHashMap<>();

	// NOTE: these are used for TESTS ONLY, DON'T USE THESE IN CODE!
	Map<UUID, BlockingQueue<TaskResponse>> responseQueues = new ConcurrentHashMap<>();

	@Value("${terarium.taskrunner.request-queue}")
	private String TASK_RUNNER_REQUEST_QUEUE;

	@Value("${terarium.taskrunner.response-queue}")
	private String TASK_RUNNER_RESPONSE_QUEUE;

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

	@PostConstruct
	void init() {
		declareQueue(TASK_RUNNER_REQUEST_QUEUE);
		declareQueue(TASK_RUNNER_RESPONSE_QUEUE);
	}

	public void sendTaskRequest(TaskRequest req) throws JsonProcessingException {
		// create the cancellation queue _BEFORE_ sending the request, because a
		// cancellation can be send before the request is consumed on the other end if
		// there is contention. We need this queue to exist to hold the message.
		String queueName = req.getId().toString();
		String routingKey = req.getId().toString();
		declareAndBindTransientQueueWithRoutingKey(TASK_RUNNER_CANCELLATION_EXCHANGE, queueName, routingKey);

		try {
			// send the request to the task runner
			final String jsonStr = objectMapper.writeValueAsString(req);
			rabbitTemplate.convertAndSend(TASK_RUNNER_REQUEST_QUEUE, jsonStr);
		} catch (Exception e) {
			rabbitAdmin.deleteQueue(queueName);
			throw e;
		}
	}

	public void addResponseHandler(String script, TaskResponseHandler handler) {
		responseHandlers.put(script, handler);
	}

	@PutMapping("/{task-id}")
	@IgnoreRequestLogging
	public ResponseEntity<Void> cancelTask(@PathVariable("task-id") final UUID taskId) {
		// send the cancellation to the task runner
		final String msg = "";
		rabbitTemplate.convertAndSend(TASK_RUNNER_CANCELLATION_EXCHANGE, msg);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/{task-id}")
	@IgnoreRequestLogging
	public SseEmitter subscribe(@PathVariable("task-id") final UUID taskId) {
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

	public BlockingQueue<TaskResponse> waitforResponses(UUID taskId) {
		BlockingQueue<TaskResponse> queue = new ArrayBlockingQueue<>(64);
		responseQueues.put(taskId, queue);
		return queue;
	}

	@RabbitListener(queues = {
			"${terarium.taskrunner.response-queue}" }, concurrency = "1")
	void onTaskResponse(final Message message, final Channel channel) throws IOException, InterruptedException {
		try {
			TaskResponse resp = decodeMessage(message, TaskResponse.class);
			if (resp == null) {
				return;
			}

			try {
				if (responseQueues.containsKey(resp.getId())) {
					BlockingQueue<TaskResponse> queue = responseQueues.get(resp.getId());
					queue.put(resp);
				}
			} catch (Exception e) {
				log.error("Error occured while writing to response queue for task {}", resp.getId(), e);
			}

			try {
				responseHandlers.get(resp.getScript()).handle(resp);
			} catch (Exception e) {
				log.error("Error occured while executing response handler for task {}", resp.getId(), e);
			}

			final SseEmitter emitter = taskIdToEmitter.get(resp.getId());
			synchronized (taskIdToEmitter) {
				if (emitter != null) {
					try {
						emitter.send(resp);
					} catch (IllegalStateException | ClientAbortException e) {
						log.warn("Error sending task response for task {}. User likely disconnected", resp.getId());
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

	public TaskResponse createEchoTask(UUID taskId, JsonNode input)
			throws JsonProcessingException, IOException {

		byte[] bytes = objectMapper.writeValueAsBytes(input);

		TaskRequest req = new TaskRequest();
		req.setId(taskId);
		req.setScript("/echo.py");
		req.setInput(bytes);

		String additionalStuff = "Test additional props";
		req.setAdditionalProperties(additionalStuff);

		// send the request
		sendTaskRequest(req);

		TaskResponse resp = new TaskResponse();
		resp.setId(req.getId());
		resp.setStatus(TaskStatus.QUEUED);
		return resp;
	}

}
