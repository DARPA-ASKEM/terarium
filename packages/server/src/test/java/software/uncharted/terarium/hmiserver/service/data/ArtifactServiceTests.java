package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.Arrays;
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
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetExport;

public class ArtifactServiceTests extends TerariumApplicationTests {

	@Autowired
	ArtifactService artifactService;

	static ObjectMapper objectMapper = new ObjectMapper();

	static Artifact createArtifact(final String key) {
		final Artifact artifact = new Artifact();
		artifact.setName("test-artifact-name-" + key);
		artifact.setDescription("test-artifact-description-" + key);
		artifact.setFileNames(Arrays.asList("never", "gonna", "give", "you", "up"));

		return artifact;
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateArtifact() {
		final Artifact before = (Artifact) createArtifact("0").setId(UUID.randomUUID());
		try {
			final Artifact after = artifactService.createAsset(before);

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
		final Artifact artifact = (Artifact) createArtifact("0").setId(UUID.randomUUID());
		try {
			artifactService.createAsset(artifact);
			artifactService.createAsset(artifact);
			Assertions.fail("Should have thrown an exception");

		} catch (final Exception e) {
			Assertions.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetArtifacts() throws IOException {
		artifactService.createAsset(createArtifact("0"));
		artifactService.createAsset(createArtifact("1"));
		artifactService.createAsset(createArtifact("2"));

		final List<Artifact> sims = artifactService.getAssets(0, 10);

		Assertions.assertEquals(3, sims.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetArtifactById() throws IOException {
		final Artifact artifact = artifactService.createAsset(createArtifact("0"));
		final Artifact fetchedArtifact = artifactService.getAsset(artifact.getId()).get();

		Assertions.assertEquals(artifact, fetchedArtifact);
		Assertions.assertEquals(artifact.getId(), fetchedArtifact.getId());
		Assertions.assertEquals(artifact.getCreatedOn(), fetchedArtifact.getCreatedOn());
		Assertions.assertEquals(artifact.getUpdatedOn(), fetchedArtifact.getUpdatedOn());
		Assertions.assertEquals(artifact.getDeletedOn(), fetchedArtifact.getDeletedOn());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateArtifact() throws Exception {

		final Artifact artifact = artifactService.createAsset(createArtifact("A"));
		artifact.setName("new name");

		final Artifact updatedArtifact = artifactService.updateAsset(artifact).orElseThrow();

		Assertions.assertEquals(artifact, updatedArtifact);
		Assertions.assertNotNull(updatedArtifact.getUpdatedOn());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteArtifact() throws Exception {

		final Artifact artifact = artifactService.createAsset(createArtifact("B"));

		artifactService.deleteAsset(artifact.getId());

		final Optional<Artifact> deleted = artifactService.getAsset(artifact.getId());

		Assertions.assertTrue(deleted.isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCloneArtifact() throws Exception {

		Artifact artifact = createArtifact("A");

		artifact = artifactService.createAsset(artifact);

		final Artifact cloned = artifactService.cloneAsset(artifact.getId());

		Assertions.assertNotEquals(artifact.getId(), cloned.getId());
		Assertions.assertEquals(artifact.getName(), cloned.getName());
		Assertions.assertEquals(
				artifact.getFileNames().size(), cloned.getFileNames().size());
		Assertions.assertEquals(
				artifact.getFileNames().get(0), cloned.getFileNames().get(0));
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanExportAndImportArtifact() throws Exception {

		Artifact artifact = createArtifact("A");

		artifact = artifactService.createAsset(artifact);

		final AssetExport<Artifact> exported = artifactService.exportAsset(artifact.getId());

		final Artifact imported = artifactService.importAsset(exported);

		Assertions.assertNotEquals(artifact.getId(), imported.getId());
		Assertions.assertEquals(artifact.getName(), imported.getName());
		Assertions.assertEquals(artifact.getDescription(), imported.getDescription());
		Assertions.assertEquals(
				artifact.getFileNames().size(), imported.getFileNames().size());
		Assertions.assertEquals(
				artifact.getFileNames().get(0), imported.getFileNames().get(0));
	}
}
