package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceRelationType;
import software.uncharted.terarium.hmiserver.models.dataservice.provenance.ProvenanceType;
import software.uncharted.terarium.hmiserver.service.data.ProvenanceService;

public class ProvenanceControllerTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ProvenanceService provenanceService;

	final Provenance provenance = new Provenance()
			.setId("test-provenance")
			.setLeft("test-left-id")
			.setLeftType(ProvenanceType.MODEL)
			.setRight("test-right-id")
			.setRightType(ProvenanceType.EQUATION)
			.setRelationType(ProvenanceRelationType.EXTRACTED_FROM);

	@After
	public void tearDown() {
		provenanceService.deleteProvenance(provenance.getId());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateProvenance() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.post("/provenance")
				.with(csrf())
				.contentType("application/json")
				.content(objectMapper.writeValueAsString(provenance)))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanGetProvenance() throws Exception {

		provenanceService.createProvenance(provenance);

		mockMvc.perform(MockMvcRequestBuilders.get("/provenance/" + provenance.getId())
				.with(csrf()))
				.andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanDeleteProvenance() throws Exception {

		provenanceService.createProvenance(provenance);

		mockMvc.perform(MockMvcRequestBuilders.delete("/provenance/" + provenance.getId())
				.with(csrf()))
				.andExpect(status().isOk());

		Assertions.assertTrue(provenanceService.getProvenance(provenance.getId()).isEmpty());
	}

}
