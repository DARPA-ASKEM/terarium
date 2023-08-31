package software.uncharted.pantera.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.pantera.PanteraApplicationTests;
import software.uncharted.pantera.configuration.MockUser;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTests extends PanteraApplicationTests {

    @Test
    public void testItReturns401OnUnAuthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(MockUser.ADAM)
    public void testItReturnsOkOnAuthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user/me"))
                .andExpect(status().isOk());
    }
}
