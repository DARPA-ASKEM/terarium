package software.uncharted.terarium.hmiserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.CacheName;

@CacheConfig(cacheNames = CacheName.EXAMPLE)
@Slf4j
@Service
public class CacheableTestService {

	public static String LOG_MESSAGE = "In cached method";
	public static String RETURN_VALUE = "Hello";

	@Cacheable
	public String cachedMethod() {
		log.info(LOG_MESSAGE);
		return RETURN_VALUE;
	}
}
