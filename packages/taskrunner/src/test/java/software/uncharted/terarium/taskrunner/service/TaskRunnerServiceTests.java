package software.uncharted.terarium.taskrunner.service;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import software.uncharted.terarium.taskrunner.TaskRunnerApplicationTests;

public class TaskRunnerServiceTests extends TaskRunnerApplicationTests {
	@Autowired
	TaskRunnerService taskRunnerService;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Test
	public void testRunTask() {

	}
}
