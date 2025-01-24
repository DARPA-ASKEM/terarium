package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentExtraction;
import software.uncharted.terarium.hmiserver.models.dataservice.document.ExtractionAssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.mira.DKG;

@Slf4j
public class DocumentServiceTests extends TerariumApplicationTests {

	@Autowired
	private DocumentAssetService documentAssetService;

	@Autowired
	private ProjectSearchService projectSearchService;

	@Autowired
	private ProjectService projectService;

	Project project;

	@BeforeEach
	public void setup() throws IOException {
		projectSearchService.setupIndexAndAliasAndEnsureEmpty();
		documentAssetService.setupIndexAndAliasAndEnsureEmpty();

		project = projectService.createProject(
			(Project) new Project().setPublicAsset(true).setName("test-project-name").setDescription("my description")
		);
	}

	@AfterEach
	public void teardown() throws IOException {
		documentAssetService.teardownIndexAndAlias();
		projectSearchService.teardownIndexAndAlias();
	}

	static Grounding createGrounding(final String key) {
		final Grounding grounding = new Grounding();
		final Map<String, String> context = new HashMap<>();
		context.put("hello", "world-" + key);
		context.put("foo", "bar-" + key);
		grounding.setContext(context);
		grounding.setIdentifiers(new ArrayList<>());
		grounding.getIdentifiers().add(new DKG("curie", "maria", "", null, null));
		return grounding;
	}

	static DocumentExtraction createDocExtraction() {
		return new DocumentExtraction().setFileName("Hello World.pdf").setAssetType(ExtractionAssetType.FIGURE);
	}

	static DocumentAsset createDocument() throws Exception {
		return createDocument("A");
	}

	static DocumentAsset createDocument(final String key) throws Exception {
		final DocumentAsset documentAsset = new DocumentAsset();
		documentAsset.setName("test-document-name-" + key);
		documentAsset.setDescription("test-document-description-" + key);
		documentAsset.setFileNames(new ArrayList<>());
		documentAsset.getFileNames().add("science.pdf");
		documentAsset.getFileNames().add("science2.pdf");
		documentAsset.setGrounding(createGrounding(key));
		documentAsset.setMetadata(new HashMap<>());
		documentAsset.getMetadata().put("hello", JsonNodeFactory.instance.textNode("world-" + key));
		documentAsset.setPublicAsset(true);
		documentAsset.setAssets(new ArrayList<>());
		documentAsset.getAssets().add(createDocExtraction());
		return documentAsset;
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateDocument() throws Exception {
		final DocumentAsset before = (DocumentAsset) createDocument().setId(UUID.randomUUID());
		final DocumentAsset after = documentAssetService.createAsset(before, project.getId(), ASSUME_WRITE_PERMISSION);

		Assertions.assertEquals(before.getId(), after.getId());
		Assertions.assertNotNull(after.getId());
		Assertions.assertNotNull(after.getCreatedOn());
		Assertions.assertEquals(3, after.getFileNames().size());

		Assertions.assertNotNull(after.getGrounding());
		Assertions.assertNotNull(after.getGrounding().getId());
		Assertions.assertNotNull(after.getGrounding().getCreatedOn());
		Assertions.assertNotNull(after.getGrounding().getIdentifiers());
		Assertions.assertNotNull(after.getGrounding().getIdentifiers().get(0).getCurie());
		Assertions.assertNotNull(after.getGrounding().getIdentifiers().get(0).getName());
		Assertions.assertNotNull(after.getGrounding().getContext());
		Assertions.assertEquals(after.getGrounding().getContext().size(), 2);
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCantCreateDuplicates() throws Exception {
		final DocumentAsset documentAsset = (DocumentAsset) createDocument().setId(UUID.randomUUID());

		documentAssetService.createAsset(documentAsset, project.getId(), ASSUME_WRITE_PERMISSION);

		try {
			documentAssetService.createAsset(documentAsset, project.getId(), ASSUME_WRITE_PERMISSION);
			Assertions.fail("Should have thrown an exception");
		} catch (final IllegalArgumentException e) {
			Assertions.assertTrue(e.getMessage().contains("already exists"));
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDocumentAssets() throws Exception {
		documentAssetService.createAsset(createDocument("0"), project.getId(), ASSUME_WRITE_PERMISSION);
		documentAssetService.createAsset(createDocument("1"), project.getId(), ASSUME_WRITE_PERMISSION);
		documentAssetService.createAsset(createDocument("2"), project.getId(), ASSUME_WRITE_PERMISSION);

		final List<DocumentAsset> documentAssets = documentAssetService.getPublicNotTemporaryAssets(0, 3);

		Assertions.assertEquals(3, documentAssets.size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetDocumentAsset() throws Exception {
		final DocumentAsset documentAsset = documentAssetService.createAsset(
			createDocument(),
			project.getId(),
			ASSUME_WRITE_PERMISSION
		);

		final DocumentAsset fetchedDocumentAsset = documentAssetService
			.getAsset(documentAsset.getId(), ASSUME_WRITE_PERMISSION)
			.get();

		Assertions.assertEquals(documentAsset, fetchedDocumentAsset);
		Assertions.assertEquals(documentAsset.getId(), fetchedDocumentAsset.getId());
		Assertions.assertEquals(documentAsset.getCreatedOn(), fetchedDocumentAsset.getCreatedOn());
		Assertions.assertEquals(documentAsset.getUpdatedOn(), fetchedDocumentAsset.getUpdatedOn());
		Assertions.assertEquals(documentAsset.getDeletedOn(), fetchedDocumentAsset.getDeletedOn());
		Assertions.assertEquals(documentAsset.getGrounding(), fetchedDocumentAsset.getGrounding());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanUpdateDocumentAsset() throws Exception {
		final DocumentAsset documentAsset = documentAssetService.createAsset(
			createDocument(),
			project.getId(),
			ASSUME_WRITE_PERMISSION
		);
		documentAsset.setName("new name");

		final DocumentAsset updatedDocumentAsset = documentAssetService
			.updateAsset(documentAsset, project.getId(), ASSUME_WRITE_PERMISSION)
			.orElseThrow();

		Assertions.assertEquals(documentAsset, updatedDocumentAsset);
		Assertions.assertNotNull(updatedDocumentAsset.getUpdatedOn());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteDocumentAsset() throws Exception {
		final DocumentAsset documentAsset = documentAssetService.createAsset(
			createDocument(),
			project.getId(),
			ASSUME_WRITE_PERMISSION
		);

		documentAssetService.deleteAsset(documentAsset.getId(), project.getId(), ASSUME_WRITE_PERMISSION);

		final Optional<DocumentAsset> deleted = documentAssetService.getAsset(
			documentAsset.getId(),
			ASSUME_WRITE_PERMISSION
		);

		Assertions.assertTrue(deleted.isEmpty());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCloneDocumentAsset() throws Exception {
		DocumentAsset documentAsset = createDocument();
		documentAsset = documentAssetService.createAsset(documentAsset, project.getId(), ASSUME_WRITE_PERMISSION);

		final DocumentAsset cloned = documentAsset.clone();

		Assertions.assertNotEquals(documentAsset.getId(), cloned.getId());
		Assertions.assertEquals(documentAsset.getGrounding().getIdentifiers(), cloned.getGrounding().getIdentifiers());
		Assertions.assertEquals(documentAsset.getGrounding().getContext(), cloned.getGrounding().getContext());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSearchAssets() throws Exception {
		final int NUM = 32;

		List<DocumentAsset> documentAssets = new ArrayList<>();
		for (int i = 0; i < NUM; i++) {
			documentAssets.add(createDocument(String.valueOf(i)));
		}
		documentAssets = documentAssetService.createAssets(documentAssets, project.getId(), ASSUME_WRITE_PERMISSION);

		final List<DocumentAsset> results = documentAssetService.searchAssets(0, NUM, null);

		Assertions.assertEquals(NUM, results.size());

		for (int i = 0; i < results.size(); i++) {
			Assertions.assertEquals(documentAssets.get(i).getName(), results.get(i).getName());
			Assertions.assertEquals(documentAssets.get(i).getDescription(), results.get(i).getDescription());
			Assertions.assertEquals(
				documentAssets.get(i).getGrounding().getIdentifiers(),
				results.get(i).getGrounding().getIdentifiers()
			);
			Assertions.assertEquals(
				documentAssets.get(i).getGrounding().getContext(),
				results.get(i).getGrounding().getContext()
			);
			Assertions.assertEquals(
				documentAssets.get(i).getCreatedOn().toInstant().getEpochSecond(),
				results.get(i).getCreatedOn().toInstant().getEpochSecond()
			);
			Assertions.assertEquals(
				documentAssets.get(i).getUpdatedOn().toInstant().getEpochSecond(),
				results.get(i).getUpdatedOn().toInstant().getEpochSecond()
			);
			Assertions.assertEquals(documentAssets.get(i).getDeletedOn(), results.get(i).getDeletedOn());
		}
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSyncToNewIndex() throws Exception {
		final int NUM = 32;

		final List<DocumentAsset> documentAssets = new ArrayList<>();
		for (int i = 0; i < NUM; i++) {
			documentAssets.add(createDocument(String.valueOf(i)));
		}
		documentAssetService.createAssets(documentAssets, project.getId(), ASSUME_WRITE_PERMISSION);

		final String currentIndex = documentAssetService.getCurrentAssetIndex();

		Assertions.assertEquals(NUM, documentAssetService.searchAssets(0, NUM, null).size());

		documentAssetService.syncAllAssetsToNewIndex(true);

		final String newIndex = documentAssetService.getCurrentAssetIndex();

		Assertions.assertEquals(NUM, documentAssetService.searchAssets(0, NUM, null).size());

		Assertions.assertNotEquals(currentIndex, newIndex);
	}
}
