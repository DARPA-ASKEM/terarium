package software.uncharted.terarium.taskrunner.service;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import software.uncharted.terarium.hmiserver.models.task.TaskRequest;
import software.uncharted.terarium.taskrunner.TaskRunnerApplicationTests;

public class TaskTests extends TaskRunnerApplicationTests {

	@Test
	public void testTask() {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setTaskKey("ml");

		Task task = new Task(req.getId(), req.getTaskKey());

		try {
			task.setup();
			task.writeInput(req.getInput());
			task.run();
			byte[] output = task.readOutput();

			System.out.println(output);

		} catch (Exception e) {
		} finally {
			task.teardown();
		}

	}
}
