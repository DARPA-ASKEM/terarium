package software.uncharted.terarium.hmiserver.aspects;

import jakarta.transaction.Transactional;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

public class HasProjectAccessAspectClassTests extends TerariumApplicationTests {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private HasProjectAccessAspectClassTestService testService;

	@Test
	@WithUserDetails(MockUser.ADAM)
	@Transactional
	public void testClassLevelAnnotation() {
		final Project p;
		try {
			p = projectService.createProject(
				(Project) new Project().setName("Test Project").setDescription("Test Project Description")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		testService.defaultParameterName(String.valueOf(p.getId()));
	}

	@Test
	@WithUserDetails(MockUser.ADAM)
	@Transactional
	public void testParameterNameOverride() {
		final Project p;
		try {
			p = projectService.createProject(
				(Project) new Project().setName("Test Project").setDescription("Test Project Description")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		testService.methodOverride(String.valueOf(p.getId()));
	}

	@Test
	@WithUserDetails(MockUser.ADAM)
	@Transactional
	public void testOwnerLevel() {
		final Project p;
		try {
			p = projectService.createProject(
				(Project) new Project().setName("Test Project").setDescription("Test Project Description")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		testService.ownerLevel(String.valueOf(p.getId()));
	}
}
