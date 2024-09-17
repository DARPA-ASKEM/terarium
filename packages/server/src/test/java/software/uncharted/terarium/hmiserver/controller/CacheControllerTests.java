package software.uncharted.terarium.hmiserver.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.models.CacheName;
import software.uncharted.terarium.hmiserver.service.CacheableTestService;
import software.uncharted.terarium.hmiserver.utils.MatchUtil;

@ExtendWith(OutputCaptureExtension.class)
public class CacheControllerTests extends TerariumApplicationTests {

	@Autowired
	private CacheableTestService testService;

	@Autowired
	private CacheManager cacheManager;

	@AfterEach
	public void afterEach() {
		cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
	}

	// @Test
	// @WithUserDetails(MockUser.ADAM)
	public void testItCanClearACache(final CapturedOutput output) throws Exception {
		testService.cachedMethod();

		mockMvc.perform(MockMvcRequestBuilders.delete("/cache").with(csrf())).andExpect(status().isOk());

		testService.cachedMethod();

		Assertions.assertEquals(2L, MatchUtil.matchCount(CacheableTestService.LOG_MESSAGE, output.getOut()));
	}

	// @Test
	// @WithUserDetails(MockUser.ADAM)
	public void testItCanClearAllCaches(final CapturedOutput output) throws Exception {
		testService.cachedMethod();
		mockMvc
			.perform(MockMvcRequestBuilders.delete("/cache").param("name", CacheName.EXAMPLE).with(csrf()))
			.andExpect(status().isOk());
		testService.cachedMethod();

		Assertions.assertEquals(2L, MatchUtil.matchCount(CacheableTestService.LOG_MESSAGE, output.getOut()));
	}
}
