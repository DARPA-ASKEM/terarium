package software.uncharted.terarium.hmiserver.service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.service.data.DocumentAssetService;
import software.uncharted.terarium.hmiserver.service.data.ProjectSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;

@Slf4j
public class ExtractionServiceTests extends TerariumApplicationTests {

	@Autowired
	private DocumentAssetService documentAssetService;

	@Autowired
	private ExtractionService extractionService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private ProjectSearchService projectSearchService;

	Project project;

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

	// @Test
	@WithUserDetails(MockUser.URSULA)
	public void pdfExtraction() throws Exception {
		final ClassPathResource resource = new ClassPathResource("knowledge/Bertozzi2020.pdf");
		final byte[] content = Files.readAllBytes(resource.getFile().toPath());

		final HttpEntity pdfFileEntity = new ByteArrayEntity(content, ContentType.create("application/pdf"));

		DocumentAsset documentAsset = (DocumentAsset) new DocumentAsset()
			.setFileNames(List.of("SIR.pdf"))
			.setName("test-pdf-name")
			.setDescription("my description");

		documentAsset = documentAssetService.createAsset(documentAsset, project.getId());

		documentAssetService.uploadFile(documentAsset.getId(), "SIR.pdf", pdfFileEntity);

		documentAsset = extractionService.extractPDFAndApplyToDocumentNew(documentAsset.getId(), null).get();

		log.info("" + documentAsset.getExtraction());
	}
}
