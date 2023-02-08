package software.uncharted.terarium.documentserver.caching;

import io.quarkus.cache.CacheManager;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.ScheduledExecution;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class CacheClearService {

	public enum CacheCollection {
		XDD
	}

	@Getter
	@RequiredArgsConstructor
	public enum CacheName {
		XDD_SETS(Constants.XDD_SETS_NAME, CacheCollection.XDD),
		XDD_DICTIONARIES(Constants.XDD_DICTIONARIES_NAME, CacheCollection.XDD);


		private final String name;

		private final CacheCollection collection;

		public static List<CacheName> findAllByCollection(CacheCollection collection) {
			return Stream.of(values())
				.filter(cacheName -> cacheName.collection.equals(collection))
				.collect(Collectors.toList());
		}

		public static class Constants {
			public static final String XDD_SETS_NAME = "xdd-sets";
			public static final String XDD_DICTIONARIES_NAME = "xdd-dictionaries";
		}
	}

	@Inject
	CacheManager cacheManager;

	@Scheduled(cron = "0 0 * * * ?")
		// every hour on the hour
	void clearXDDCaches(ScheduledExecution execution) {
		System.out.println("clear");
		CacheName.findAllByCollection(CacheCollection.XDD).forEach(cacheName -> {
			cacheManager.getCache(cacheName.getName()).ifPresent(cache ->
				cache.invalidateAll().await().indefinitely()
			);
		});
	}
}
