package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceQueryParam;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceSearchResult;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.service.data.ProjectSearchService;
import software.uncharted.terarium.hmiserver.service.data.ProjectService;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

public class ProvenanceControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProvenanceService provenanceService;

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

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateProvenance() throws Exception {
		final Provenance provenance = new Provenance()
			.setLeft(UUID.randomUUID())
			.setLeftType(ProvenanceType.MODEL)
			.setRight(UUID.randomUUID())
			.setRightType(ProvenanceType.EQUATION)
			.setRelationType(ProvenanceRelationType.EXTRACTED_FROM);

		mockMvc
			.perform(
				MockMvcRequestBuilders.post("/provenance")
					.with(csrf())
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(provenance))
			)
			.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSearchProvenanceConnectedNodes() throws Exception {
		final UUID modelId = UUID.randomUUID();
		final UUID equationId = UUID.randomUUID();
		final UUID codeId = UUID.randomUUID();
		final UUID docId = UUID.randomUUID();

		provenanceService.createProvenance(
			new Provenance()
				.setLeft(modelId)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(equationId)
				.setRightType(ProvenanceType.EQUATION)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
		);
		provenanceService.createProvenance(
			new Provenance()
				.setLeft(modelId)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(codeId)
				.setRightType(ProvenanceType.CODE)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
		);
		provenanceService.createProvenance(
			new Provenance()
				.setLeft(modelId)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(docId)
				.setRightType(ProvenanceType.DOCUMENT)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
		);

		final ProvenanceQueryParam payload = new ProvenanceQueryParam()
			.setRootId(modelId)
			.setRootType(ProvenanceType.MODEL)
			.setEdges(true)
			.setNodes(true);

		final MvcResult res = mockMvc
			.perform(
				MockMvcRequestBuilders.post("/provenance/search/connected-nodes")
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(payload))
					.with(csrf())
			)
			.andExpect(status().isOk())
			.andReturn();

		final ProvenanceSearchResult results = objectMapper.readValue(
			res.getResponse().getContentAsString(),
			ProvenanceSearchResult.class
		);

		Assertions.assertEquals(4, results.getNodes().size());
		Assertions.assertEquals(3, results.getEdges().size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSearchProvenanceModelsFromCode() throws Exception {
		final UUID modelIdA = UUID.randomUUID();
		final UUID codeIdA1 = UUID.randomUUID();
		final UUID codeIdA2 = UUID.randomUUID();
		final UUID codeIdA3 = UUID.randomUUID();

		final UUID modelIdB = UUID.randomUUID();
		final UUID codeIdB1 = UUID.randomUUID();
		final UUID codeIdB2 = UUID.randomUUID();

		provenanceService.createProvenance(
			new Provenance()
				.setLeft(modelIdA)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(codeIdA1)
				.setRightType(ProvenanceType.CODE)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
		);
		provenanceService.createProvenance(
			new Provenance()
				.setLeft(modelIdA)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(codeIdA2)
				.setRightType(ProvenanceType.CODE)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
		);
		provenanceService.createProvenance(
			new Provenance()
				.setLeft(modelIdA)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(codeIdA3)
				.setRightType(ProvenanceType.CODE)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
		);

		final ProvenanceQueryParam payloadA = new ProvenanceQueryParam()
			.setRootId(modelIdA)
			.setRootType(ProvenanceType.MODEL)
			.setEdges(true)
			.setNodes(true);

		final MvcResult resA = mockMvc
			.perform(
				MockMvcRequestBuilders.post("/provenance/search/connected-nodes")
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(payloadA))
					.with(csrf())
			)
			.andExpect(status().isOk())
			.andReturn();

		final ProvenanceSearchResult resultsA = objectMapper.readValue(
			resA.getResponse().getContentAsString(),
			ProvenanceSearchResult.class
		);

		Assertions.assertEquals(4, resultsA.getNodes().size());
		Assertions.assertEquals(3, resultsA.getEdges().size());

		provenanceService.createProvenance(
			new Provenance()
				.setLeft(modelIdB)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(codeIdB1)
				.setRightType(ProvenanceType.CODE)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
		);
		provenanceService.createProvenance(
			new Provenance()
				.setLeft(modelIdB)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(codeIdB2)
				.setRightType(ProvenanceType.CODE)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM)
		);

		final ProvenanceQueryParam payloadB = new ProvenanceQueryParam()
			.setRootId(modelIdB)
			.setRootType(ProvenanceType.MODEL)
			.setEdges(true)
			.setNodes(true);

		final MvcResult resB = mockMvc
			.perform(
				MockMvcRequestBuilders.post("/provenance/search/connected-nodes")
					.contentType("application/json")
					.content(objectMapper.writeValueAsString(payloadB))
					.with(csrf())
			)
			.andExpect(status().isOk())
			.andReturn();

		final ProvenanceSearchResult resultsB = objectMapper.readValue(
			resB.getResponse().getContentAsString(),
			ProvenanceSearchResult.class
		);

		Assertions.assertEquals(3, resultsB.getNodes().size());
		Assertions.assertEquals(2, resultsB.getEdges().size());
	}
}
