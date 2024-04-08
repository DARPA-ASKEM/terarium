package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;

@Slf4j
public class WorkflowServiceTests extends TerariumApplicationTests {

	@Autowired
	private WorkflowService workflowService;

	@BeforeEach
	public void setup() throws IOException {
		workflowService.setupIndexAndAliasAndEnsureEmpty();
	}

	@AfterEach
	public void teardown() throws IOException {
		workflowService.teardownIndexAndAlias();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateWorkflow() throws Exception {

		Workflow workflow = new Workflow()
				.setName("test-workflow-name0")
				.setDescription("test-workflow-description");

		workflow = workflowService.createAsset(workflow);

		Assertions.assertNotNull(workflow.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflows() throws Exception {

		workflowService.createAsset(new Workflow()
				.setName("test-workflow-name0")
				.setDescription("test-workflow-description"));
		workflowService.createAsset(new Workflow()
				.setName("test-workflow-name1")
				.setDescription("test-workflow-description"));
		workflowService.createAsset(new Workflow()
				.setName("test-workflow-name2")
				.setDescription("test-workflow-description"));

		final List<Workflow> workflows = workflowService.getAssets(0, 100);

		Assertions.assertEquals(3, workflows.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetWorkflow() throws Exception {

		Workflow workflow = new Workflow()
				.setName("test-workflow-name0")
				.setDescription("test-workflow-description");

		workflow = workflowService.createAsset(workflow);

		final Workflow fetchedWorkflow = workflowService.getAsset(workflow.getId()).get();

		Assertions.assertEquals(workflow, fetchedWorkflow);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateWorkflow() throws Exception {

		Workflow workflow = new Workflow()
				.setName("test-workflow-name0")
				.setDescription("test-workflow-description");

		workflow = workflowService.createAsset(workflow);

		workflow.setName("new name");

		final Workflow updatedWorkflow = workflowService.updateAsset(workflow);

		Assertions.assertEquals(workflow, updatedWorkflow);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteWorkflow() throws Exception {

		Workflow workflow = new Workflow()
				.setName("test-workflow-name0")
				.setDescription("test-workflow-description");

		workflow = workflowService.createAsset(workflow);

		workflowService.deleteAsset(workflow.getId());

		final Optional<Workflow> deleted = workflowService.getAsset(workflow.getId());

		Assertions.assertTrue(deleted.isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSyncToNewIndex() throws Exception {

		final int NUM = 32;

		for (int i = 0; i < NUM; i++) {
			workflowService.createAsset(new Workflow()
					.setName("test-workflow-name-" + i)
					.setDescription("test-workflow-description-" + i));
		}

		final String currentIndex = workflowService.getCurrentAssetIndex();

		Assertions.assertEquals(NUM, workflowService.getAssets(0, NUM).size());

		workflowService.syncAllAssetsToNewIndex(true);

		final String newIndex = workflowService.getCurrentAssetIndex();

		Assertions.assertEquals(NUM, workflowService.getAssets(0, NUM).size());

		Assertions.assertNotEquals(currentIndex, newIndex);
	}
}
