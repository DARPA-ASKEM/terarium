package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetExport;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.code.CodeFile;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Dynamics;
import software.uncharted.terarium.hmiserver.models.dataservice.code.ProgrammingLanguage;

public class CodeServiceTests extends TerariumApplicationTests {

	@Autowired
	CodeService codeService;

	static ObjectMapper objectMapper = new ObjectMapper();

	static CodeFile createCodeFile(final String name) {
		return new CodeFile()
				.setFileName(name)
				.setLanguage(ProgrammingLanguage.PYTHON)
				.setDynamics(new Dynamics().setName("hello").setDescription("world"));
	}

	static Code createCode(final String key) {
		final Code code = new Code();
		code.setName("test-code-name-" + key);
		code.setDescription("test-code-description-" + key);
		code.setRepoUrl("https://github.com/DARPA-ASKEM/terarium");
		code.setFiles(new HashMap<>());
		code.getFiles().put("file1.py", createCodeFile("file1"));
		code.getFiles().put("file2.py", createCodeFile("file2"));
		code.setMetadata(new HashMap<>());
		code.getMetadata().put("dog", "puppy");
		code.getMetadata().put("cat", "kitten");
		code.getMetadata().put("otter", "kit");
		code.getMetadata().put("horse", "foal");
		return code;
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateCode() {
		final Code before = (Code) createCode("0").setId(UUID.randomUUID());
		try {
			final Code after = codeService.createAsset(before);

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
			codeService.createAsset(code);
			codeService.createAsset(code);
			Assertions.fail("Should have thrown an exception");

		} catch (final Exception e) {
			Assertions.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetCodes() throws IOException {
		codeService.createAsset(createCode("0"));
		codeService.createAsset(createCode("1"));
		codeService.createAsset(createCode("2"));

		final List<Code> sims = codeService.getAssets(0, 10);

		Assertions.assertEquals(3, sims.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetCodeById() throws IOException {
		final Code code = codeService.createAsset(createCode("0"));
		final Code fetchedCode = codeService.getAsset(code.getId()).get();

		Assertions.assertEquals(code, fetchedCode);
		Assertions.assertEquals(code.getId(), fetchedCode.getId());
		Assertions.assertEquals(code.getCreatedOn(), fetchedCode.getCreatedOn());
		Assertions.assertEquals(code.getUpdatedOn(), fetchedCode.getUpdatedOn());
		Assertions.assertEquals(code.getDeletedOn(), fetchedCode.getDeletedOn());
		Assertions.assertEquals(code.getRepoUrl(), fetchedCode.getRepoUrl());
		Assertions.assertEquals(code.getFiles().size(), fetchedCode.getFiles().size());
		Assertions.assertEquals(
				code.getMetadata().keySet().size(),
				fetchedCode.getMetadata().keySet().size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateCode() throws Exception {

		final Code code = codeService.createAsset(createCode("A"));
		code.setName("new name");

		final Code updatedCode = codeService.updateAsset(code).orElseThrow();

		Assertions.assertEquals(code, updatedCode);
		Assertions.assertNotNull(updatedCode.getUpdatedOn());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteCode() throws Exception {

		final Code code = codeService.createAsset(createCode("B"));

		codeService.deleteAsset(code.getId());

		final Optional<Code> deleted = codeService.getAsset(code.getId());

		Assertions.assertTrue(deleted.isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCloneCode() throws Exception {

		Code code = createCode("A");

		code = codeService.createAsset(code);

		final Code cloned = codeService.cloneAsset(code.getId());

		Assertions.assertNotEquals(code.getId(), cloned.getId());
		Assertions.assertEquals(code.getName(), cloned.getName());
		Assertions.assertEquals(code.getRepoUrl(), cloned.getRepoUrl());
		Assertions.assertEquals(code.getFiles().size(), cloned.getFiles().size());
		Assertions.assertEquals(
				code.getMetadata().keySet().size(),
				cloned.getMetadata().keySet().size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanExportAndImportCode() throws Exception {

		Code code = createCode("A");

		code = codeService.createAsset(code);

		final AssetExport<Code> exported = codeService.exportAsset(code.getId());

		final Code imported = codeService.importAsset(exported);

		Assertions.assertNotEquals(code.getId(), imported.getId());
		Assertions.assertEquals(code.getName(), imported.getName());
		Assertions.assertEquals(code.getDescription(), imported.getDescription());
		Assertions.assertEquals(code.getRepoUrl(), imported.getRepoUrl());
		Assertions.assertEquals(code.getFiles().size(), imported.getFiles().size());
		Assertions.assertEquals(
				code.getMetadata().keySet().size(),
				imported.getMetadata().keySet().size());
	}
}
