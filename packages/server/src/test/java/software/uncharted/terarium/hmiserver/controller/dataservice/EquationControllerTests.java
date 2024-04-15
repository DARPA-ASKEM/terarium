package software.uncharted.terarium.hmiserver.controller.dataservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.dataservice.equation.Equation;
import software.uncharted.terarium.hmiserver.service.data.EquationService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

public class EquationControllerTests extends TerariumApplicationTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EquationService equationService;

    @Autowired
    private ElasticsearchService elasticService;

    @Autowired
    private ElasticsearchConfiguration elasticConfig;

    @BeforeEach
    public void setup() throws IOException {
        elasticService.createOrEnsureIndexIsEmpty(elasticConfig.getEquationIndex());
    }

    @AfterEach
    public void teardown() throws IOException {
        elasticService.deleteIndex(elasticConfig.getEquationIndex());
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanCreateEquation() throws Exception {

        final Equation equation = new Equation().setName("test-equation-name");

        mockMvc.perform(MockMvcRequestBuilders.post("/equations")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(equation)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanGetEquation() throws Exception {

        final Equation equation = equationService.createAsset(new Equation().setName("test-equation-name"));

        mockMvc.perform(MockMvcRequestBuilders.get("/equations/" + equation.getId())
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanGetEquations() throws Exception {

        equationService.createAsset(new Equation().setName("test-equation-name"));
        equationService.createAsset(new Equation().setName("test-equation-name"));
        equationService.createAsset(new Equation().setName("test-equation-name"));

        mockMvc.perform(MockMvcRequestBuilders.get("/equations").with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanDeleteEquation() throws Exception {

        final Equation equation = equationService.createAsset(new Equation().setName("test-equation-name"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/equations/" + equation.getId())
                        .with(csrf()))
                .andExpect(status().isOk());

        Assertions.assertTrue(equationService.getAsset(equation.getId()).isEmpty());
    }
}
