package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.ProjectSearchService.ProjectDocument;

@Slf4j
public class ProjectSearchServiceTests extends TerariumApplicationTests {

	@Autowired
	private ProjectSearchService projectSearchService;

	@BeforeEach
	public void setup() throws IOException {
		projectSearchService.setupIndexAndAliasAndEnsureEmpty();
	}

	@AfterEach
	public void teardown() throws IOException {
		projectSearchService.teardownIndexAndAlias();
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanIndexProject() throws Exception {
		final Project project = new Project();
		project.setPublicAsset(false);
		project.setName("test-project-name");
		project.setDescription("my description");

		projectSearchService.indexProject(project);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSearchPublicProject() throws Exception {
		final String ownerId = "test-user-owner";
		final String searcherId = "test-user-searcher";

		final Project project = new Project();
		project.setPublicAsset(true);
		project.setUserId(ownerId);
		project.setName("test-project-name");
		project.setDescription("my description");

		projectSearchService.indexProject(project);
		projectSearchService.forceESRefresh();
		final List<ProjectDocument> docs = projectSearchService.searchProjectsForUser(searcherId, 0, 10, null);

		Assertions.assertEquals(1, docs.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSearchPrivateProjectAsReader() throws Exception {
		final String ownerId = "test-user-owner";
		final String searcherId = "test-user-searcher";

		final Project project = new Project();
		project.setPublicAsset(false);
		project.setUserId(ownerId);
		project.setName("test-project-name");
		project.setDescription("my description");

		projectSearchService.indexProject(project);
		projectSearchService.forceESRefresh();
		projectSearchService.addProjectPermission(project.getId(), searcherId);
		projectSearchService.forceESRefresh();

		final List<ProjectDocument> docs = projectSearchService.searchProjectsForUser(searcherId, 0, 10, null);

		Assertions.assertEquals(1, docs.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSearchPrivateProjectAsOwner() throws Exception {
		final String ownerId = "test-user-owner";

		final Project project = new Project();
		project.setPublicAsset(false);
		project.setUserId(ownerId);
		project.setName("test-project-name");
		project.setDescription("my description");

		projectSearchService.indexProject(project);
		projectSearchService.forceESRefresh();
		final List<ProjectDocument> docs = projectSearchService.searchProjectsForUser(ownerId, 0, 10, null);

		Assertions.assertEquals(1, docs.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testPermissionsWithMultipleProjects() throws Exception {
		final String ownerId = "test-user-owner";
		final String readerId = "test-user-reader";

		final int NUM_HIDDEN_PROJECTS = 16;
		for (int i = 0; i < NUM_HIDDEN_PROJECTS; i++) {
			final Project project = new Project();
			project.setPublicAsset(false);
			project.setUserId(ownerId);
			project.setName("test-project-name-H" + i);
			project.setDescription("my description");

			projectSearchService.indexProject(project);
		}

		final int NUM_VISIBLE_PROJECTS = 4;
		for (int i = 0; i < NUM_VISIBLE_PROJECTS; i++) {
			final Project project = new Project();
			project.setPublicAsset(false);
			project.setUserId(ownerId);
			project.setName("test-project-name-V" + i);
			project.setDescription("my description");

			projectSearchService.indexProject(project);
			projectSearchService.addProjectPermission(project.getId(), readerId);
		}

		final int NUM_PUBLIC_PROJECTS = 4;
		for (int i = 0; i < NUM_PUBLIC_PROJECTS; i++) {
			final Project project = new Project();
			project.setPublicAsset(true);
			project.setUserId(ownerId);
			project.setName("test-project-name-P" + i);
			project.setDescription("my description");

			projectSearchService.indexProject(project);
		}

		final int NUM_OWNED_PROJECTS = 4;
		for (int i = 0; i < NUM_OWNED_PROJECTS; i++) {
			final Project project = new Project();
			project.setPublicAsset(false);
			project.setUserId(readerId);
			project.setName("test-project-name-P" + i);
			project.setDescription("my description");

			projectSearchService.indexProject(project);
		}
		projectSearchService.forceESRefresh();

		final List<ProjectDocument> docs = projectSearchService.searchProjectsForUser(readerId, 0, 100, null);

		Assertions.assertEquals(NUM_VISIBLE_PROJECTS + NUM_PUBLIC_PROJECTS + NUM_OWNED_PROJECTS, docs.size());
	}
}
