package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.code.CodeFile;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Dynamics;
import software.uncharted.terarium.hmiserver.models.dataservice.code.ProgrammingLanguage;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;

public class CodeServiceTests extends TerariumApplicationTests {

	@Autowired
	CodeService codeService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectSearchService projectSearchService;

	Project project;

	static ObjectMapper objectMapper = new ObjectMapper();

	static CodeFile createCodeFile(final String name) {
		return new CodeFile()
			.setFileName(name)
			.setLanguage(ProgrammingLanguage.PYTHON)
			.setDynamics(new Dynamics().setName("hello").setDescription("world"));
	}

	@BeforeEach
	public void setup() throws IOException {
		projectSearchService.setupIndexAndAliasAndEnsureEmpty();
		project = projectService.createProject(
			(Project) new Project().setPublicAsset(true).setName("test-project-name").setDescription("my description")
		);
	}

	@AfterEach
	public void teardown() throws IOException {
		projectSearchService.teardownIndexAndAlias();
	}

	Code createCode(final String key) {
		final Code code = new Code();
		code.setPublicAsset(true);
		code.setTemporary(false);
		code.setName("test-code-name-" + key);
		code.setDescription("test-code-description-" + key);
		code.setRepoUrl("https://github.com/DARPA-ASKEM/terarium");
		code.setFileNames(List.of("file1.py", "file2.py"));
		code.setFiles(new HashMap<>());
		code.getFiles().put("file1.py", createCodeFile("file1"));
		code.getFiles().put("file2.py", createCodeFile("file2"));
		code.setMetadata(new HashMap<>());
		code.getMetadata().put("dog", "puppy");
		code.getMetadata().put("cat", "kitten");
		code.getMetadata().put("otter", "kit");
		code.getMetadata().put("horse", "foal");

		for (final String filename : code.getFileNames()) {
			try {
				codeService.uploadFile(code.getId(), filename, ContentType.TEXT_PLAIN, new String("Test content").getBytes());
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}

		return code;
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateCode() {
		final Code before = (Code) createCode("0").setId(UUID.randomUUID());
		try {
			final Code after = codeService.createAsset(before, project.getId());

			Assertions.assertEquals(before.getId(), after.getId());
			Assertions.assertNotNull(after.getId());
			Assertions.assertNotNull(after.getCreatedOn());
		} catch (final Exception e) {
			Assertions.fail(e);
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCantCreateDuplicates() {
		final Code code = (Code) createCode("0").setId(UUID.randomUUID());
		try {
			codeService.createAsset(code, project.getId());
			codeService.createAsset(code, project.getId());
			Assertions.fail("Should have thrown an exception");
		} catch (final Exception e) {
			Assertions.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetCodes() throws IOException {
		codeService.createAsset(createCode("0"), project.getId());
		codeService.createAsset(createCode("1"), project.getId());
		codeService.createAsset(createCode("2"), project.getId());

		final List<Code> sims = codeService.getPublicNotTemporaryAssets(0, 10);

		Assertions.assertEquals(3, sims.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetCodeById() throws IOException {
		final Code code = codeService.createAsset(createCode("0"), project.getId());
		final Code fetchedCode = codeService.getAsset(code.getId()).get();

		Assertions.assertEquals(code, fetchedCode);
		Assertions.assertEquals(code.getId(), fetchedCode.getId());
		Assertions.assertEquals(code.getCreatedOn(), fetchedCode.getCreatedOn());
		Assertions.assertEquals(code.getUpdatedOn(), fetchedCode.getUpdatedOn());
		Assertions.assertEquals(code.getDeletedOn(), fetchedCode.getDeletedOn());
		Assertions.assertEquals(code.getRepoUrl(), fetchedCode.getRepoUrl());
		Assertions.assertEquals(code.getFiles().size(), fetchedCode.getFiles().size());
		Assertions.assertEquals(code.getMetadata().keySet().size(), fetchedCode.getMetadata().keySet().size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateCode() throws Exception {
		final Code code = codeService.createAsset(createCode("A"), project.getId());
		code.setName("new name");

		final Code updatedCode = codeService.updateAsset(code, project.getId()).orElseThrow();

		Assertions.assertEquals(code, updatedCode);
		Assertions.assertNotNull(updatedCode.getUpdatedOn());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteCode() throws Exception {
		final Code code = codeService.createAsset(createCode("B"), project.getId());

		codeService.deleteAsset(code.getId(), project.getId());

		final Optional<Code> deleted = codeService.getAsset(code.getId());

		Assertions.assertTrue(deleted.isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCloneCode() throws Exception {
		Code code = createCode("A");

		code = codeService.createAsset(code, project.getId());

		final Code cloned = code.clone();

		Assertions.assertNotEquals(code.getId(), cloned.getId());
		Assertions.assertEquals(code.getName(), cloned.getName());
		Assertions.assertEquals(code.getRepoUrl(), cloned.getRepoUrl());
		Assertions.assertEquals(code.getFiles().size(), cloned.getFiles().size());
		Assertions.assertEquals(code.getMetadata().keySet().size(), cloned.getMetadata().keySet().size());
	}
}
