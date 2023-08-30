package software.uncharted.pantera.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cache.CacheManager;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.pantera.PanteraApplicationTests;
import software.uncharted.pantera.configuration.MockUser;
import software.uncharted.pantera.model.CacheName;
import software.uncharted.pantera.service.CacheableTestService;
import software.uncharted.pantera.util.MatchUtil;

import java.util.Objects;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(OutputCaptureExtension.class)
public class CacheControllerTests extends PanteraApplicationTests {
  @Autowired
  private CacheableTestService testService;

  @Autowired
  private CacheManager cacheManager;

  @AfterEach
  public void afterEach() {
    cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
  }

  @Test
  @WithUserDetails(MockUser.ADAM)
  public void testItCanClearACache(final CapturedOutput output) throws Exception {
    testService.cachedMethod();

    mockMvc.perform(MockMvcRequestBuilders
        .delete("/cache")
        .with(csrf()))
      .andExpect(status().isOk());

    testService.cachedMethod();

    Assertions.assertEquals(2L, MatchUtil.matchCount(CacheableTestService.LOG_MESSAGE, output.getOut()));
  }

  @Test
  @WithUserDetails(MockUser.ADAM)
  public void testItCanClearAllCaches(final CapturedOutput output) throws Exception {
    testService.cachedMethod();
    mockMvc.perform(MockMvcRequestBuilders
        .delete("/cache")
        .param("name", CacheName.EXAMPLE)
        .with(csrf()))
      .andExpect(status().isOk());
    testService.cachedMethod();

    Assertions.assertEquals(2L, MatchUtil.matchCount(CacheableTestService.LOG_MESSAGE, output.getOut()));
  }
}
