package software.uncharted.terarium.hmiserver.aspects;

import jakarta.transaction.Transactional;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

public class HasProjectAccessAspectMethodTests extends TerariumApplicationTests {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private HasProjectAccessAspectMethodTestService testService;

	@Test
	@WithUserDetails(MockUser.ADAM)
	@Transactional
	public void testReadAndDefaultArgument() {
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

	/*
	TODO: Our mock users are set up differently than pantera. Once we complete the migration come back to this test or remove it.
  @Test
  @WithUserDetails(MockUser.URSULA)
  @Transactional
  public void testForbidden() {
		final Project p;
		try {
			p = projectService.createProject(
				(Project) new Project()
					.setName("Test Project")
					.setDescription("Test Project Description")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

    // Make ursula the owner and remove adam
    projectService.addUser(
      p,
      userService.getByUsername(MockUser.URSULA),
      ProjectPermissionLevel.OWNER
    );
    projectService.removeUser(p, currentUser());


    Assertions.assertThrows(AccessDeniedException.class, () ->
      testService.defaultParameterName(String.valueOf(p.getId()))
    );
  }*/

	@Test
	@WithUserDetails(MockUser.URSULA)
	@Transactional
	public void testNotFound() {
		Assertions.assertThrows(ResponseStatusException.class, () ->
			testService.defaultParameterName("5fc03087-d265-11e7-b8c6-83e29cd24f4c")
		);
	}

	@Test
	@WithUserDetails(MockUser.ADAM)
	@Transactional
	public void testCustomParameterName() {
		final Project p;
		try {
			p = projectService.createProject(
				(Project) new Project().setName("Test Project").setDescription("Test Project Description")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		testService.customParameterName(String.valueOf(p.getId()));
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

	@Test
	@WithUserDetails(MockUser.ADAM)
	@Transactional
	public void testOwnerLevelWithCustomParameterName() {
		final Project p;
		try {
			p = projectService.createProject(
				(Project) new Project().setName("Test Project").setDescription("Test Project Description")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		testService.ownerLevelWithCustomParameterName(String.valueOf(p.getId()));
	}

	//@Test
	@WithUserDetails(MockUser.URSULA)
	@Transactional
	public void testIncorrectSpel() {
		final Project p;
		try {
			p = projectService.createProject(
				(Project) new Project().setName("Test Project").setDescription("Test Project Description")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Assertions.assertThrows(RuntimeException.class, () -> testService.incorrectSpel(String.valueOf(p.getId())));
	}

	//@Test
	@WithUserDetails(MockUser.URSULA)
	@Transactional
	public void testIncorrectSpel2() {
		final Project p;
		try {
			p = projectService.createProject(
				(Project) new Project().setName("Test Project").setDescription("Test Project Description")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Assertions.assertThrows(RuntimeException.class, () -> testService.incorrectSpel2(String.valueOf(p.getId())));
	}

	@Test
	@WithUserDetails(MockUser.ADAM)
	@Transactional
	public void testIncorrectSpel3() {
		final Project p;
		try {
			p = projectService.createProject(
				(Project) new Project().setName("Test Project").setDescription("Test Project Description")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Assertions.assertThrows(RuntimeException.class, () -> testService.incorrectSpel3(String.valueOf(p.getId())));
	}

	//@Test
	@WithUserDetails(MockUser.URSULA)
	@Transactional
	public void testNullSpel() {
		Assertions.assertThrows(RuntimeException.class, () -> testService.defaultParameterName(null));
	}

	@Test
	@WithUserDetails(MockUser.ADAM)
	@Transactional
	public void testObjectSpel() {
		final Project p;
		try {
			p = projectService.createProject(
				(Project) new Project().setName("Test Project").setDescription("Test Project Description")
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		testService.objectSpel(p);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	@Transactional
	public void testNullObjectSpel() {
		final Project p = null;
		Assertions.assertThrows(RuntimeException.class, () -> testService.objectSpel(p));
	}
}
