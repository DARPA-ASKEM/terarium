package software.uncharted.terarium.taskrunner.service;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import software.uncharted.terarium.taskrunner.TaskRunnerApplicationTests;
import software.uncharted.terarium.taskrunner.models.task.TaskRequest;

public class TaskTests extends TaskRunnerApplicationTests {

	@Test
	public void testTaskSuccess() throws Exception {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setTaskKey("ml");
		req.setInput(new String("{\"research_paper\": \"Test research paper\"}").getBytes());

		int ONE_MINUTE = 1;

		Task task = new Task(req.getId(), req.getTaskKey());
		try {
			task.start();
			task.writeInputWithTimeout(req.getInput(), ONE_MINUTE);

			byte[] output = task.readOutputWithTimeout(ONE_MINUTE);

			System.out.println(output);

			task.waitFor(ONE_MINUTE);

		} catch (Exception e) {
			throw e;
		} finally {
			task.cleanup();
		}
	}

	@Test
	public void testTaskFailure() throws Exception {

		TaskRequest req = new TaskRequest();
		req.setId(UUID.randomUUID());
		req.setTaskKey("ml");
		req.setInput(new String("{\"should_fail\": true}").getBytes());

		int ONE_MINUTE = 1;

		Task task = new Task(req.getId(), req.getTaskKey());
		try {
			task.start();
			task.writeInputWithTimeout(req.getInput(), ONE_MINUTE);

			byte[] output = task.readOutputWithTimeout(ONE_MINUTE);

			System.out.println(output);

			task.waitFor(ONE_MINUTE);

		} catch (Exception e) {
			// this should happen
			return;
		} finally {
			task.cleanup();
		}

		throw new Exception("Task should have failed");

	}
}
