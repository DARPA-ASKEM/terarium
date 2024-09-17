package software.uncharted.terarium.hmiserver.configuration;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.uncharted.terarium.hmiserver.models.CacheName;

@Configuration
@EnableCaching
@ImportAutoConfiguration(classes = { CacheAutoConfiguration.class, RedisAutoConfiguration.class })
@Slf4j
public class CacheConfiguration implements CachingConfigurer {

	@Override
	@Bean
	public CacheManager cacheManager() {
		final SimpleCacheManager cacheManager = new SimpleCacheManager();
		final Cache usersCache = new ConcurrentMapCache("users");
		final Cache rolesCache = new ConcurrentMapCache("roles");
		final Cache authoriesCache = new ConcurrentMapCache("authorities");
		final Cache projectContributorsCache = new ConcurrentMapCache("projectcontributors");
		final Cache projectreadersCache = new ConcurrentMapCache("projectreaders");
		final Cache userRepresentationCache = new ConcurrentMapCache(CacheName.USER_REPRESENTATION);
		cacheManager.setCaches(
			Arrays.asList(
				usersCache,
				rolesCache,
				authoriesCache,
				projectContributorsCache,
				projectreadersCache,
				userRepresentationCache
			)
		);
		return cacheManager;
	}

	/**
	 * Overrides the default key generating for spring caching. This ensures that the fully qualified classname (with
	 * the package) and the method parameters are taken into account
	 *
	 * @return the {@link KeyGenerator} for caching
	 */
	@Override
	@Bean
	public KeyGenerator keyGenerator() {
		return (target, method, params) -> {
			// Generate a unique key based on the calling method and parameters supplied
			String key = target.getClass().getName();
			key += "_" + method.getName();
			for (final Object o : params) {
				key += "_" + (o != null ? String.valueOf(o.toString().hashCode()) : "null");
			}

			// Return the key
			return key;
		};
	}
}
