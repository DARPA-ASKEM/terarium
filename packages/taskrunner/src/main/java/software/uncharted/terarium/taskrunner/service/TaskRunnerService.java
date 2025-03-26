package software.uncharted.terarium.taskrunner.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.taskrunner.configuration.Config;
import software.uncharted.terarium.taskrunner.models.task.TaskRequest;
import software.uncharted.terarium.taskrunner.models.task.TaskResponse;
import software.uncharted.terarium.taskrunner.models.task.TaskStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskRunnerService {

	private final ObjectMapper mapper;
	private final RabbitTemplate rabbitTemplate;
	private final RabbitAdmin rabbitAdmin;
	private final Config config;

	@Value("${terarium.taskrunner.request-queue}-${terarium.taskrunner.request-type}")
	public String TASK_RUNNER_REQUEST_QUEUE;

	@Value("${terarium.taskrunner.response-exchange}")
	public String TASK_RUNNER_RESPONSE_EXCHANGE;

	@Value("${terarium.taskrunner.cancellation-exchange}")
	public String TASK_RUNNER_CANCELLATION_EXCHANGE;

	public void declareAndBindTransientQueueWithRoutingKey(
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

	public void declareQueue(final String queueName) {
		final Queue queue = new Queue(queueName, config.getDurableQueues(), false, false);
		rabbitAdmin.declareQueue(queue);
	}

	public void declareQueues() {
		log.info("Declaring request queue: {}", TASK_RUNNER_REQUEST_QUEUE);
		declareQueue(TASK_RUNNER_REQUEST_QUEUE);
	}

	public void destroyQueues() {
		rabbitAdmin.deleteQueue(TASK_RUNNER_REQUEST_QUEUE);
	}

	@PostConstruct
	void init() {
		declareQueues();
	}

	@RabbitListener(
		queues = { "${terarium.taskrunner.request-queue}-${terarium.taskrunner.request-type}" },
		concurrency = "${terarium.taskrunner.request-concurrency}"
	)
	void onTaskRequest(final Message message, final Channel channel) throws IOException, InterruptedException {
		final TaskRequest req = decodeMessage(message, TaskRequest.class);
		if (req == null) {
			return;
		}

		// for now just support a basic single input, single output task
		dispatchSingleInputSingleOutputTask(req);
	}

	private void dispatchSingleInputSingleOutputTask(final TaskRequest req) throws IOException, InterruptedException {
		Task task;
		DirectMessageListenerContainer cancellationConsumer;

		try {
			// ensure that the cancellation queue exists
			declareCancellationQueue(req);

			// lets see if the task has already been cancelled
			final boolean wasCancelled = checkForCancellation(req);
			if (wasCancelled) {
				// send cancellation response and return
				final TaskResponse resp = req.createResponse(TaskStatus.CANCELLED, "", "");
				final String cancelJson = mapper.writeValueAsString(resp);
				rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, resp.getRoutingKey(), cancelJson);
				return;
			}

			// create the task
			task = new Task(req);

			// create the cancellation consumer
			cancellationConsumer = createCancellationQueueConsumer(task);
		} catch (final Exception e) {
			log.error("Unable to setup task", e);

			// send failure and return
			final TaskResponse failedResp = req.createResponse(TaskStatus.ERROR, "", "");
			// append error
			failedResp.setOutput(e.getMessage().getBytes());
			final String failedJson = mapper.writeValueAsString(failedResp);
			rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, failedResp.getRoutingKey(), failedJson);
			return;
		}

		try {
			// start listening for cancellation requests
			cancellationConsumer.start();

			// start the task
			task.start();

			// send that we have started the task
			final TaskResponse runningResp = req.createResponse(TaskStatus.RUNNING, "", "");

			final String runningJson = mapper.writeValueAsString(runningResp);
			rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, runningResp.getRoutingKey(), runningJson);

			// write the input to the task
			task.writeInputWithTimeout(req.getInput(), req.getTimeoutMinutes());

			while (true) {
				// block and wait for progress from the task
				final byte[] output = task.readProgressWithTimeout(req.getTimeoutMinutes());
				if (output == null) {
					// no more progress
					break;
				}

				final TaskResponse progressResp = task.createResponse(TaskStatus.RUNNING);
				progressResp.setOutput(output);
				final String progressJson = mapper.writeValueAsString(progressResp);
				rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, progressResp.getRoutingKey(), progressJson);
			}

			// block and wait for output from the task
			final byte[] output = task.readOutputWithTimeout(req.getTimeoutMinutes());

			// wait for the process to finish
			task.waitFor(req.getTimeoutMinutes());

			final TaskResponse successResp = task.createResponse(TaskStatus.COMPLETE);
			successResp.setOutput(output);
			final String successJson = mapper.writeValueAsString(successResp);
			rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, successResp.getRoutingKey(), successJson);
		} catch (final Exception e) {
			if (task.getStatus() == TaskStatus.ERROR) {
				log.error("Task {} failed", task.getId(), e);
			} else if (task.getStatus() != TaskStatus.CANCELLED) {
				log.error("Unexpected failure for task {}", task.getId(), e);
			}

			final TaskResponse failedResp = task.createResponse(
				task.getStatus() == TaskStatus.CANCELLED ? TaskStatus.CANCELLED : TaskStatus.ERROR
			);
			if (task.getStatus() == TaskStatus.ERROR) {
				// append error
				failedResp.setOutput(e.getMessage().getBytes());
			}
			final String failedJson = mapper.writeValueAsString(failedResp);
			rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, failedResp.getRoutingKey(), failedJson);
		} finally {
			cancellationConsumer.stop();
			task.cleanup();
		}
	}

	private void declareCancellationQueue(final TaskRequest req) {
		final String queueName = req.getId().toString();
		final String routingKey = req.getId().toString();

		declareAndBindTransientQueueWithRoutingKey(TASK_RUNNER_CANCELLATION_EXCHANGE, queueName, routingKey);
	}

	private boolean checkForCancellation(final TaskRequest req) {
		final String queueName = req.getId().toString();

		final Object message = rabbitTemplate.receiveAndConvert(queueName);
		if (message != null) {
			log.info("Request for task {} has already been cancelled", req.getId());
			return true;
		}
		return false;
	}

	private DirectMessageListenerContainer createCancellationQueueConsumer(final Task task) {
		final String queueName = task.getId().toString();

		final DirectMessageListenerContainer container = new DirectMessageListenerContainer(
			rabbitTemplate.getConnectionFactory()
		);
		container.setPrefetchCount(1);
		container.setQueueNames(queueName);
		container.setMessageListener(message -> {
			try {
				log.info("Received cancellation for task {}", task.getId());
				if (task.flagAsCancelling()) {
					// send that we are cancelling
					final TaskResponse resp = task.createResponse(TaskStatus.CANCELLED);
					final String cancelJson = mapper.writeValueAsString(resp);
					rabbitTemplate.convertAndSend(TASK_RUNNER_RESPONSE_EXCHANGE, resp.getRoutingKey(), cancelJson);

					// then cancel
					task.cancel();
				}
			} catch (final JsonProcessingException e) {
				log.error("Error responding after cancelling task {}", task.getId(), e);
			}
		});
		return container;
	}

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
}
