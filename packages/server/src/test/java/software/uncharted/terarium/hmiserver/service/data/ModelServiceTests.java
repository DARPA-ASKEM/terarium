package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;

public class ModelServiceTests extends TerariumApplicationTests {

	@Autowired
	ModelService modelService;

	static Model createModel(final String key) {
		final Model model = new Model();
		model.setName("test-model-name-" + key);
		model.setDescription("test-model-description-" + key);
		return model;
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateModel() {
		final Model before = (Model) createModel("0").setId(UUID.randomUUID());
		try {
			final Model after = modelService.createAsset(before);

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
		final Model model = (Model) createModel("0").setId(UUID.randomUUID());
		try {
			modelService.createAsset(model);
			modelService.createAsset(model);
			Assertions.fail("Should have thrown an exception");

		} catch (final Exception e) {
			Assertions.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetModels() throws IOException {
		modelService.createAsset(createModel("0"));
		modelService.createAsset(createModel("1"));
		modelService.createAsset(createModel("2"));

		final List<Model> sims = modelService.getAssets(0, 10);

		Assertions.assertEquals(3, sims.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetModelById() throws IOException {
		final Model model = modelService.createAsset(createModel("0"));
		final Model fetchedModel = modelService.getAsset(model.getId()).get();

		Assertions.assertEquals(model, fetchedModel);
		Assertions.assertEquals(model.getId(), fetchedModel.getId());
		Assertions.assertEquals(model.getCreatedOn(), fetchedModel.getCreatedOn());
		Assertions.assertEquals(model.getUpdatedOn(), fetchedModel.getUpdatedOn());
		Assertions.assertEquals(model.getDeletedOn(), fetchedModel.getDeletedOn());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateModel() throws Exception {

		final Model model = modelService.createAsset(createModel("A"));
		model.setName("new name");

		final Model updatedModel = modelService.updateAsset(model).orElseThrow();

		Assertions.assertEquals(model, updatedModel);
		Assertions.assertNotNull(updatedModel.getUpdatedOn());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteModel() throws Exception {

		final Model model = modelService.createAsset(createModel("B"));

		modelService.deleteAsset(model.getId());

		final Optional<Model> deleted = modelService.getAsset(model.getId());

		Assertions.assertTrue(deleted.isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCloneModel() throws Exception {

		Model model = createModel("A");

		model = modelService.createAsset(model);

		final Model cloned = modelService.cloneAsset(model.getId());

		Assertions.assertNotEquals(model.getId(), cloned.getId());
		Assertions.assertEquals(model.getName(), cloned.getName());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanExportAndImportModel() throws Exception {

		Model model = createModel("A");

		model = modelService.createAsset(model);

		final byte[] exported = modelService.exportAsset(model.getId());

		final Model imported = modelService.importAsset(exported);

		Assertions.assertNotEquals(model.getId(), imported.getId());
		Assertions.assertEquals(model.getName(), imported.getName());
		Assertions.assertEquals(model.getDescription(), imported.getDescription());
	}
}
