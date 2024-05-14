package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.dataservice.Identifier;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;

@Slf4j
public class DatasetServiceTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private DatasetService datasetService;

	@BeforeEach
	public void setup() throws IOException {
		datasetService.setupIndexAndAliasAndEnsureEmpty();
	}

	@AfterEach
	public void teardown() throws IOException {
		datasetService.teardownIndexAndAlias();
	}

	static Grounding createGrounding(final String key) {
		final ObjectMapper mapper = new ObjectMapper();

		final Grounding grounding = new Grounding();
		grounding.setContext(
				mapper.createObjectNode().put("hello", "world-" + key).put("foo", "bar-" + key));
		grounding.setIdentifiers(new ArrayList<>());
		grounding.getIdentifiers().add(new Identifier("curie", "maria"));
		return grounding;
	}

	static Dataset createDataset() throws Exception {
		return createDataset("A");
	}

	static Dataset createDataset(final String key) throws Exception {

		final ObjectMapper mapper = new ObjectMapper();

		final DatasetColumn column1 = new DatasetColumn()
				.setName("Title")
				.setDataType(DatasetColumn.ColumnType.STRING)
				.setDescription("hello world")
				.setMetadata(
						mapper.createObjectNode().put("hello", "world-" + key).put("foo", "bar-" + key))
				.setGrounding(createGrounding(key));
		final DatasetColumn column2 = new DatasetColumn()
				.setName("Value")
				.setDataType(DatasetColumn.ColumnType.FLOAT)
				.setDescription("3.1415926")
				.setMetadata(
						mapper.createObjectNode().put("hello", "world-" + key).put("foo", "bar-" + key))
				.setGrounding(createGrounding(key));

		final Dataset dataset = new Dataset();
		dataset.setName("test-dataset-name-" + key);
		dataset.setDescription("test-dataset-description-" + key);
		dataset.setColumns(new ArrayList<>());
		dataset.getColumns().add(column1);
		dataset.getColumns().add(column2);
		dataset.setGrounding(createGrounding(key));
		dataset.setMetadata(
				mapper.createObjectNode().put("hello", "world-" + key).put("foo", "bar-" + key));
		dataset.setPublicAsset(true);

		return dataset;
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateDataset() throws Exception {

		final Dataset before = (Dataset) createDataset().setId(UUID.randomUUID());
		final Dataset after = datasetService.createAsset(before, ASSUMED_PERMISSION);

		Assertions.assertEquals(before.getId(), after.getId());
		Assertions.assertNotNull(after.getId());
		Assertions.assertNotNull(after.getCreatedOn());
		Assertions.assertEquals(after.getColumns().size(), 2);

		for (final DatasetColumn col : after.getColumns()) {
			Assertions.assertNotNull(col.getId());
			Assertions.assertNotNull(col.getCreatedOn());
			Assertions.assertNotNull(col.getGrounding());
			Assertions.assertNotNull(col.getGrounding().getId());
			Assertions.assertNotNull(col.getGrounding().getCreatedOn());
			Assertions.assertNotNull(col.getGrounding().getIdentifiers());
			Assertions.assertEquals(col.getGrounding().getIdentifiers().size(), 1);
			Assertions.assertNotNull(col.getGrounding().getIdentifiers().get(0).curie());
			Assertions.assertNotNull(col.getGrounding().getIdentifiers().get(0).name());
			Assertions.assertNotNull(col.getGrounding().getContext());
			Assertions.assertEquals(col.getGrounding().getContext().size(), 2);
		}

		Assertions.assertNotNull(after.getGrounding());
		Assertions.assertNotNull(after.getGrounding().getId());
		Assertions.assertNotNull(after.getGrounding().getCreatedOn());
		Assertions.assertNotNull(after.getGrounding().getIdentifiers());
		Assertions.assertNotNull(after.getGrounding().getIdentifiers().get(0).curie());
		Assertions.assertNotNull(after.getGrounding().getIdentifiers().get(0).name());
		Assertions.assertNotNull(after.getGrounding().getContext());
		Assertions.assertEquals(after.getGrounding().getContext().size(), 2);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCantCreateDuplicates() throws Exception {

		final Dataset dataset = (Dataset) createDataset().setId(UUID.randomUUID());

		datasetService.createAsset(dataset, ASSUMED_PERMISSION);

		try {
			datasetService.createAsset(dataset, ASSUMED_PERMISSION);
			Assertions.fail("Should have thrown an exception");
		} catch (final IllegalArgumentException e) {
			Assertions.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDatasets() throws Exception {

		datasetService.createAsset(createDataset("0"), ASSUMED_PERMISSION);
		datasetService.createAsset(createDataset("1"), ASSUMED_PERMISSION);
		datasetService.createAsset(createDataset("2"), ASSUMED_PERMISSION);

		final List<Dataset> datasets = datasetService.getPublicNotTemporaryAssets(0, 3);

		Assertions.assertEquals(3, datasets.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDataset() throws Exception {

		final Dataset dataset = datasetService.createAsset(createDataset(), ASSUMED_PERMISSION);

		final Dataset fetchedDataset = datasetService.getAsset(dataset.getId(), ASSUMED_PERMISSION).get();

		Assertions.assertEquals(dataset, fetchedDataset);
		Assertions.assertEquals(dataset.getId(), fetchedDataset.getId());
		Assertions.assertEquals(dataset.getCreatedOn(), fetchedDataset.getCreatedOn());
		Assertions.assertEquals(dataset.getUpdatedOn(), fetchedDataset.getUpdatedOn());
		Assertions.assertEquals(dataset.getDeletedOn(), fetchedDataset.getDeletedOn());
		Assertions.assertEquals(dataset.getGrounding(), fetchedDataset.getGrounding());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateDataset() throws Exception {

		final Dataset dataset = datasetService.createAsset(createDataset(), ASSUMED_PERMISSION);
		dataset.setName("new name");

		final Dataset updatedDataset = datasetService.updateAsset(dataset, ASSUMED_PERMISSION).orElseThrow();

		Assertions.assertEquals(dataset, updatedDataset);
		Assertions.assertNotNull(updatedDataset.getUpdatedOn());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteDataset() throws Exception {

		final Dataset dataset = datasetService.createAsset(createDataset(), ASSUMED_PERMISSION);

		datasetService.deleteAsset(dataset.getId(), ASSUMED_PERMISSION);

		final Optional<Dataset> deleted = datasetService.getAsset(dataset.getId(), ASSUMED_PERMISSION);

		Assertions.assertTrue(deleted.isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCloneDataset() throws Exception {

		Dataset dataset = createDataset();
		dataset = datasetService.createAsset(dataset, ASSUMED_PERMISSION);

		final Dataset cloned = datasetService.cloneAsset(dataset.getId(), ASSUMED_PERMISSION);

		Assertions.assertNotEquals(dataset.getId(), cloned.getId());
		Assertions.assertEquals(
				dataset.getGrounding().getIdentifiers(), cloned.getGrounding().getIdentifiers());
		Assertions.assertEquals(
				dataset.getGrounding().getContext(), cloned.getGrounding().getContext());
		Assertions.assertEquals(dataset.getColumns().size(), cloned.getColumns().size());
		for (int i = 0; i < dataset.getColumns().size(); i++) {
			Assertions.assertEquals(
					dataset.getColumns().get(i).getName(),
					cloned.getColumns().get(i).getName());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getDescription(),
					cloned.getColumns().get(i).getDescription());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getDataType(),
					cloned.getColumns().get(i).getDataType());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getAnnotations(),
					cloned.getColumns().get(i).getAnnotations());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getMetadata(),
					cloned.getColumns().get(i).getMetadata());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getGrounding().getIdentifiers(),
					cloned.getColumns().get(i).getGrounding().getIdentifiers());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getGrounding().getContext(),
					cloned.getColumns().get(i).getGrounding().getContext());
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanExportAndImportDataset() throws Exception {

		Dataset dataset = createDataset();
		dataset = datasetService.createAsset(dataset, ASSUMED_PERMISSION);

		final byte[] exported = datasetService.exportAsset(dataset.getId(), ASSUMED_PERMISSION);

		final Dataset imported = datasetService.importAsset(exported, ASSUMED_PERMISSION);

		Assertions.assertNotEquals(dataset.getId(), imported.getId());
		Assertions.assertEquals(dataset.getName(), imported.getName());
		Assertions.assertEquals(dataset.getDescription(), imported.getDescription());
		Assertions.assertEquals(
				dataset.getGrounding().getIdentifiers(), imported.getGrounding().getIdentifiers());
		Assertions.assertEquals(
				dataset.getGrounding().getContext(), imported.getGrounding().getContext());
		Assertions.assertEquals(
				dataset.getColumns().size(), imported.getColumns().size());
		for (int i = 0; i < dataset.getColumns().size(); i++) {
			Assertions.assertEquals(
					dataset.getColumns().get(i).getName(),
					imported.getColumns().get(i).getName());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getDescription(),
					imported.getColumns().get(i).getDescription());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getDataType(),
					imported.getColumns().get(i).getDataType());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getAnnotations(),
					imported.getColumns().get(i).getAnnotations());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getMetadata(),
					imported.getColumns().get(i).getMetadata());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getGrounding().getIdentifiers(),
					imported.getColumns().get(i).getGrounding().getIdentifiers());
			Assertions.assertEquals(
					dataset.getColumns().get(i).getGrounding().getContext(),
					imported.getColumns().get(i).getGrounding().getContext());
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSearchAssets() throws Exception {

		final int NUM = 32;

		List<Dataset> datasets = new ArrayList<>();
		for (int i = 0; i < NUM; i++) {
			datasets.add(createDataset(String.valueOf(i)));
		}
		datasets = datasetService.createAssets(datasets, ASSUMED_PERMISSION);

		final List<Dataset> results = datasetService.searchAssets(0, NUM, null);

		Assertions.assertEquals(NUM, results.size());

		for (int i = 0; i < results.size(); i++) {
			Assertions.assertEquals(datasets.get(i).getName(), results.get(i).getName());
			Assertions.assertEquals(
					datasets.get(i).getDescription(), results.get(i).getDescription());
			Assertions.assertEquals(
					datasets.get(i).getGrounding().getIdentifiers(),
					results.get(i).getGrounding().getIdentifiers());
			Assertions.assertEquals(
					datasets.get(i).getGrounding().getContext(),
					results.get(i).getGrounding().getContext());
			Assertions.assertEquals(
					datasets.get(i).getCreatedOn().toInstant().getEpochSecond(),
					results.get(i).getCreatedOn().toInstant().getEpochSecond());
			Assertions.assertEquals(
					datasets.get(i).getUpdatedOn().toInstant().getEpochSecond(),
					results.get(i).getUpdatedOn().toInstant().getEpochSecond());
			Assertions.assertEquals(
					datasets.get(i).getDeletedOn(), results.get(i).getDeletedOn());
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSyncToNewIndex() throws Exception {

		final int NUM = 32;

		final List<Dataset> datasets = new ArrayList<>();
		for (int i = 0; i < NUM; i++) {
			datasets.add(createDataset(String.valueOf(i)));
		}
		datasetService.createAssets(datasets, ASSUMED_PERMISSION);

		final String currentIndex = datasetService.getCurrentAssetIndex();

		Assertions.assertEquals(NUM, datasetService.searchAssets(0, NUM, null).size());

		datasetService.syncAllAssetsToNewIndex(true);

		final String newIndex = datasetService.getCurrentAssetIndex();

		Assertions.assertEquals(NUM, datasetService.searchAssets(0, NUM, null).size());

		Assertions.assertNotEquals(currentIndex, newIndex);
	}
}
