package software.uncharted.terarium.hmiserver.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.CacheName;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheService {

	private final CacheManager cacheManager;
	private final Config config;

	@PostConstruct
	void init() {
		if (config.getCaching().getClearOnStartup()) {
			clear();
		}
	}

	/**
	 * Clears a cache by name
	 *
	 * @param name the name of the cache
	 */
	public void clear(final String name) {
		final Cache cache = cacheManager.getCache(name);
		if (cache != null) {
			cache.clear();
		}
	}

	/**
	 * Clears all caches
	 */
	public void clear() {
		CacheName.getAll().forEach(this::clear);
	}
}
