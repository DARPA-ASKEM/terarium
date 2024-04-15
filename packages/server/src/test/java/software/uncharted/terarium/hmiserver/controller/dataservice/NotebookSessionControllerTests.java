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
import software.uncharted.terarium.hmiserver.models.dataservice.notebooksession.NotebookSession;
import software.uncharted.terarium.hmiserver.service.data.NotebookSessionService;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

public class NotebookSessionControllerTests extends TerariumApplicationTests {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotebookSessionService notebookSessionService;

    @Autowired
    private ElasticsearchService elasticService;

    @Autowired
    private ElasticsearchConfiguration elasticConfig;

    @BeforeEach
    public void setup() throws IOException {
        elasticService.createOrEnsureIndexIsEmpty(elasticConfig.getNotebookSessionIndex());
    }

    @AfterEach
    public void teardown() throws IOException {
        elasticService.deleteIndex(elasticConfig.getNotebookSessionIndex());
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanCreateNotebookSession() throws Exception {

        final NotebookSession notebookSession = new NotebookSession().setName("test-notebook-name");

        mockMvc.perform(MockMvcRequestBuilders.post("/sessions")
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(notebookSession)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanGetNotebookSession() throws Exception {

        final NotebookSession notebookSession =
                notebookSessionService.createAsset(new NotebookSession().setName("test-notebook-name"));

        mockMvc.perform(MockMvcRequestBuilders.get("/sessions/" + notebookSession.getId())
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanGetNotebookSessions() throws Exception {

        notebookSessionService.createAsset(new NotebookSession().setName("test-notebook-name"));
        notebookSessionService.createAsset(new NotebookSession().setName("test-notebook-name"));
        notebookSessionService.createAsset(new NotebookSession().setName("test-notebook-name"));

        mockMvc.perform(MockMvcRequestBuilders.get("/sessions").with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    @WithUserDetails(MockUser.URSULA)
    public void testItCanDeleteNotebookSession() throws Exception {

        final NotebookSession notebookSession =
                notebookSessionService.createAsset(new NotebookSession().setName("test-notebook-name"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/sessions/" + notebookSession.getId())
                        .with(csrf()))
                .andExpect(status().isOk());

        Assertions.assertTrue(
                notebookSessionService.getAsset(notebookSession.getId()).isEmpty());
    }
}
