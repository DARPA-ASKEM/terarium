package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;import java.util.List;
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
import software.uncharted.terarium.hmiserver.models.dataservice.Identifier;import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;import software.uncharted.terarium.hmiserver.models.dataservice.dataset.DatasetColumn;

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

    static Dataset createDataset() throws Exception {
        return createDataset("A");
    }

    static Dataset createDataset(final String key) throws Exception {

        final Grounding grounding = new Grounding();
				grounding.setContext(new HashMap<>());
				grounding.getContext().put("hello", "world-"+key);
				grounding.getContext().put("foo", "bar-"+key);
				grounding.setIdentifiers(new ArrayList<>());
				grounding.getIdentifiers().add(new Identifier("curie", "maria"));

				final DatasetColumn column1 = new DatasetColumn().setName("Title").setDataType(DatasetColumn.ColumnType.STRING).setDescription("hello world").setGrounding(grounding);
				final DatasetColumn column2 = new DatasetColumn().setName("Value").setDataType(DatasetColumn.ColumnType.FLOAT).setDescription("3.1415926").setGrounding(grounding);


        final Dataset dataset = new Dataset();
        dataset.setName("test-dataset-name-" + key);
        dataset.setDescription("test-dataset-description-" + key);
        dataset.setColumns(new ArrayList<>());
				dataset.getColumns().add(column1);
				dataset.getColumns().add(column2);
				dataset.setGrounding(grounding);
        dataset.setPublicAsset(true);

        return dataset;
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanCreateDataset() throws Exception {

        final Dataset before = (Dataset) createDataset().setId(UUID.randomUUID());
        final Dataset after = datasetService.createAsset(before);

        Assertions.assertEquals(before.getId(), after.getId());
        Assertions.assertNotNull(after.getId());
        Assertions.assertNotNull(after.getCreatedOn());
        Assertions.assertEquals(after.getColumns().size(), 2);

    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCantCreateDuplicates() throws Exception {

        final Dataset dataset = (Dataset) createDataset().setId(UUID.randomUUID());

        datasetService.createAsset(dataset);

        try {
            datasetService.createAsset(dataset);
            Assertions.fail("Should have thrown an exception");
        } catch (final IllegalArgumentException e) {
            Assertions.assertTrue(e.getMessage().contains("already exists"));
        }
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanGetDatasets() throws Exception {

        datasetService.createAsset(createDataset("0"));
        datasetService.createAsset(createDataset("1"));
        datasetService.createAsset(createDataset("2"));

        final List<Dataset> datasets = datasetService.getAssets(0, 3);

        Assertions.assertEquals(3, datasets.size());
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanGetDataset() throws Exception {

        final Dataset dataset = datasetService.createAsset(createDataset());

        final Dataset fetchedDataset =
                datasetService.getAsset(dataset.getId()).get();

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

        final Dataset dataset = datasetService.createAsset(createDataset());
        dataset.setName("new name");

        final Dataset updatedDataset = datasetService.updateAsset(dataset).orElseThrow();

        Assertions.assertEquals(dataset, updatedDataset);
        Assertions.assertNotNull(updatedDataset.getUpdatedOn());
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanDeleteDataset() throws Exception {

        final Dataset dataset = datasetService.createAsset(createDataset());

        datasetService.deleteAsset(dataset.getId());

        final Optional<Dataset> deleted = datasetService.getAsset(dataset.getId());

        Assertions.assertTrue(deleted.isEmpty());
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanCloneDataset() throws Exception {

        Dataset dataset = createDataset();
        dataset = datasetService.createAsset(dataset);

        final Dataset cloned = datasetService.cloneAsset(dataset.getId());

        Assertions.assertNotEquals(dataset.getId(), cloned.getId());
        //TODO more checks here
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanExportAndImportDataset() throws Exception {

        Dataset dataset = createDataset();
        dataset = datasetService.createAsset(dataset);

        final byte[] exported = datasetService.exportAsset(dataset.getId());

        final Dataset imported = datasetService.importAsset(exported);

        Assertions.assertNotEquals(dataset.getId(), imported.getId());
        Assertions.assertEquals(dataset.getName(), imported.getName());
        Assertions.assertEquals(dataset.getDescription(), imported.getDescription());
        Assertions.assertEquals(dataset.getGrounding(), imported.getGrounding());
        //TODO more checks here
    }


    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanSearchAssets() throws Exception {

        final int NUM = 32;

        List<Dataset> datasets = new ArrayList<>();
        for (int i = 0; i < NUM; i++) {
            datasets.add(createDataset(String.valueOf(i)));
        }
        datasets = datasetService.createAssets(datasets);

        final List<Dataset> results = datasetService.searchAssets(0, NUM, null);

        Assertions.assertEquals(NUM, results.size());

        for (int i = 0; i < results.size(); i++) {
            Assertions.assertEquals(datasets.get(i).getName(), results.get(i).getName());
            Assertions.assertEquals(
                    datasets.get(i).getDescription(), results.get(i).getDescription());
            Assertions.assertEquals(
                    datasets.get(i).getGrounding(), results.get(i).getGrounding());
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
        datasetService.createAssets(datasets);

        final String currentIndex = datasetService.getCurrentAssetIndex();

        Assertions.assertEquals(NUM, datasetService.searchAssets(0, NUM, null).size());

        datasetService.syncAllAssetsToNewIndex(true);

        final String newIndex = datasetService.getCurrentAssetIndex();

        Assertions.assertEquals(NUM, datasetService.searchAssets(0, NUM, null).size());

        Assertions.assertNotEquals(currentIndex, newIndex);
    }
}
