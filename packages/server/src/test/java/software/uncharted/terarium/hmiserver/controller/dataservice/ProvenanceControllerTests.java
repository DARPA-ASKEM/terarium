package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceQueryParam;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceSearchResult;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

public class ProvenanceControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProvenanceService provenanceService;

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateProvenance() throws Exception {

		final Provenance provenance = new Provenance()
				.setLeft(UUID.randomUUID())
				.setLeftType(ProvenanceType.MODEL)
				.setRight(UUID.randomUUID())
				.setRightType(ProvenanceType.EQUATION)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM);

		mockMvc.perform(MockMvcRequestBuilders.post("/provenance")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(provenance)))
				.andExpect(status().isCreated());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetProvenance() throws Exception {

		final Provenance provenance = provenanceService.createProvenance(new Provenance()
				.setLeft(UUID.randomUUID())
				.setLeftType(ProvenanceType.MODEL)
				.setRight(UUID.randomUUID())
				.setRightType(ProvenanceType.EQUATION)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM));

		mockMvc.perform(MockMvcRequestBuilders.get("/provenance/" + provenance.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanSearchProvenanceConnectedNodes() throws Exception {

		UUID modelId = UUID.randomUUID();
		UUID equationId = UUID.randomUUID();
		UUID codeId = UUID.randomUUID();
		UUID docId = UUID.randomUUID();

		provenanceService.createProvenance(new Provenance()
				.setLeft(modelId)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(equationId)
				.setRightType(ProvenanceType.EQUATION)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM));
		provenanceService.createProvenance(new Provenance()
				.setLeft(modelId)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(codeId)
				.setRightType(ProvenanceType.CODE)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM));
		provenanceService.createProvenance(new Provenance()
				.setLeft(modelId)
				.setLeftType(ProvenanceType.MODEL)
				.setRight(docId)
				.setRightType(ProvenanceType.DOCUMENT)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM));

		ProvenanceQueryParam payload = new ProvenanceQueryParam()
				.setRootId(modelId)
				.setRootType(ProvenanceType.MODEL)
				.setEdges(true)
				.setNodes(true);

		MvcResult res = mockMvc.perform(MockMvcRequestBuilders.post("/provenance/search/connected-nodes")
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(payload))
				.with(csrf()))
				.andExpect(status().isOk())
				.andReturn();

		ProvenanceSearchResult results = objectMapper.readValue(res.getResponse().getContentAsString(),
				ProvenanceSearchResult.class);

		System.out.println(results);

		Assertions.assertEquals(4, results.getNodes().size());
		Assertions.assertEquals(3, results.getEdges().size());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteProvenance() throws Exception {

		final Provenance provenance = provenanceService.createProvenance(new Provenance()
				.setLeft(UUID.randomUUID())
				.setLeftType(ProvenanceType.MODEL)
				.setRight(UUID.randomUUID())
				.setRightType(ProvenanceType.EQUATION)
				.setRelationType(ProvenanceRelationType.EXTRACTED_FROM));

		mockMvc.perform(MockMvcRequestBuilders.delete("/provenance/" + provenance.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertTrue(provenanceService.getProvenance(provenance.getId()).isEmpty());
	}

}
