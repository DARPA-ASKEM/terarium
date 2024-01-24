package software.uncharted.terarium.taskrunner.service;

import java.io.IOException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.taskrunner.configuration.Config;
import software.uncharted.terarium.taskrunner.models.task.TaskRequest;
import software.uncharted.terarium.taskrunner.models.task.TaskResponse;
import software.uncharted.terarium.taskrunner.models.task.TaskStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskRunnerService {

	private final RabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;
	private final Config config;

	@Value("${terarium.task-runner-request-exchange}")
	public String TASK_RUNNER_REQUEST_EXCHANGE;

	@Value("${terarium.task-runner-request-queue}")
	public String TASK_RUNNER_REQUEST_QUEUE;

	@Value("${terarium.task-runner-response-exchange}")
	public String TASK_RUNNER_RESPONSE_EXCHANGE;

	@Value("${terarium.task-runner-response-queue}")
	public String TASK_RUNNER_RESPONSE_QUEUE;

	@Value("${terarium.task-runner-cancellation-exchange}")
	public String TASK_RUNNER_CANCELLATION_EXCHANGE;

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

	private void declareAndBindQueue(String exchangeName, String queueName) {
		// Declare a direct exchange
		DirectExchange exchange = new DirectExchange(exchangeName, config.getDurableQueues(), false);
		rabbitAdmin.declareExchange(exchange);

		// Declare a queue
		Queue queue = new Queue(queueName, config.getDurableQueues(), false, false);
		rabbitAdmin.declareQueue(queue);

		// Bind the queue to the exchange with a routing key
		Binding binding = BindingBuilder.bind(queue).to(exchange).with("");
		rabbitAdmin.declareBinding(binding);
	}

	@PostConstruct
	void init() {
		declareAndBindQueue(TASK_RUNNER_REQUEST_EXCHANGE, TASK_RUNNER_REQUEST_QUEUE);
		declareAndBindQueue(TASK_RUNNER_RESPONSE_EXCHANGE, TASK_RUNNER_RESPONSE_QUEUE);
	}

	@RabbitListener(queues = {
			"${terarium.task-runner-request-queue}" }, concurrency = "${terarium.task-runner-request-concurrency}")
	void onTaskRequest(final Message message, final Channel channel) throws IOException, InterruptedException {
		TaskRequest req = decodeMessage(message, TaskRequest.class);
		if (req == null) {
			return;
		}

		// for now just support a basic single input, single output task
		dispatchSingleInputSingleOutputTask(req);
	}

	private void dispatchSingleInputSingleOutputTask(TaskRequest req) throws IOException, InterruptedException {

		Task task = new Task(req.getId(), req.getTaskKey());

		SimpleMessageListenerContainer cancellationConsumer = createCancellationQueueConsumer(task);

		try {
			// start listening for cancellation requests
			cancellationConsumer.start();

			// start the task
			task.start();

			// send that we have started the task
			TaskResponse runningResp = new TaskResponse();
			runningResp.setId(task.getId());
			runningResp.setStatus(TaskStatus.RUNNING);
			rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, "", runningResp);

			// write the input to the task
			task.writeInputWithTimeout(req.getInput(), req.getTimeoutMinutes());

			// block and wait for input
			byte[] output = task.readOutputWithTimeout(req.getTimeoutMinutes());

			// wait for the process to finish
			task.waitFor(req.getTimeoutMinutes());

			TaskResponse successResp = new TaskResponse();
			successResp.setId(task.getId());
			successResp.setStatus(TaskStatus.SUCCESS);
			successResp.setOutput(output);
			rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, "", successResp);

		} catch (Exception e) {
			log.error("Task failed", e);

			TaskResponse failedResp = new TaskResponse();
			failedResp.setId(task.getId());
			failedResp.setStatus(task.getStatus() == TaskStatus.CANCELLED ? TaskStatus.CANCELLED : TaskStatus.FAILED);
			rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, "", failedResp);
		} finally {
			cancellationConsumer.stop();
			task.cleanup();
		}
	}

	private SimpleMessageListenerContainer createCancellationQueueConsumer(Task task) {
		String queueName = task.getId().toString();
		String routingKey = task.getId().toString();

		declareAndBindTransientQueueWithRoutingKey(TASK_RUNNER_CANCELLATION_EXCHANGE, queueName, routingKey);

		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(
				rabbitTemplate.getConnectionFactory());
		container.setQueueNames(queueName);
		container.setMessageListener(message -> {
			log.info("Received cancellation for task {}", task.getId());

			if (task.cancel()) {
				TaskResponse resp = new TaskResponse();
				resp.setId(task.getId());
				resp.setStatus(TaskStatus.CANCELLING);
				rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, "", resp);
			}
		});
		return container;
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

}
