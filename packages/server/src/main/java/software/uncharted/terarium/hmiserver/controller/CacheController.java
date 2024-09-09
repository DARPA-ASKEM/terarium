package software.uncharted.terarium.hmiserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.CacheService;

@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
public class CacheController {

	private final CacheService cacheService;

	/**
	 * Clear caches
	 *
	 * @param cacheName if present, the name of the cache to clear. if absent, clear all
	 */
	@DeleteMapping
	@Secured(Roles.TEST)
	public ResponseEntity<String> clear(@RequestParam(required = false) final String cacheName) {
		if (cacheName != null) {
			cacheService.clear(cacheName);
		} else {
			cacheService.clear();
		}
		return ResponseEntity.ok("ok");
	}
}
