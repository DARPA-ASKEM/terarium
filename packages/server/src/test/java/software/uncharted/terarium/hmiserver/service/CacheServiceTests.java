package software.uncharted.terarium.hmiserver.service;

import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cache.CacheManager;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.models.CacheName;
import software.uncharted.terarium.hmiserver.utils.MatchUtil;

@ExtendWith(OutputCaptureExtension.class)
public class CacheServiceTests extends TerariumApplicationTests {

	@Autowired
	CacheService cacheService;

	@Autowired
	CacheableTestService testService;

	@Autowired
	CacheManager cacheManager;

	@AfterEach
	public void afterEach() {
		cacheManager.getCacheNames().forEach(name -> Objects.requireNonNull(cacheManager.getCache(name)).clear());
	}

	// @Test
	public void testItCanCacheAValue(final CapturedOutput output) {
		final String value = testService.cachedMethod();
		Assertions.assertEquals(value, CacheableTestService.RETURN_VALUE);

		final String cachedValue = testService.cachedMethod();
		Assertions.assertEquals(cachedValue, CacheableTestService.RETURN_VALUE);

		Assertions.assertEquals(1L, MatchUtil.matchCount(CacheableTestService.LOG_MESSAGE, output.getOut()));
	}

	// @Test
	public void testItCanClearACache(final CapturedOutput output) {
		final String value = testService.cachedMethod();
		Assertions.assertEquals(value, CacheableTestService.RETURN_VALUE);

		cacheService.clear(CacheName.EXAMPLE);

		final String secondValue = testService.cachedMethod();
		Assertions.assertEquals(secondValue, CacheableTestService.RETURN_VALUE);

		Assertions.assertEquals(2L, MatchUtil.matchCount(CacheableTestService.LOG_MESSAGE, output.getOut()));
	}

	// @Test
	public void testItCanClearAllCaches(final CapturedOutput output) {
		final String value = testService.cachedMethod();
		Assertions.assertEquals(value, CacheableTestService.RETURN_VALUE);

		cacheService.clear();

		final String secondValue = testService.cachedMethod();
		Assertions.assertEquals(secondValue, CacheableTestService.RETURN_VALUE);

		Assertions.assertEquals(2L, MatchUtil.matchCount(CacheableTestService.LOG_MESSAGE, output.getOut()));
	}
}
