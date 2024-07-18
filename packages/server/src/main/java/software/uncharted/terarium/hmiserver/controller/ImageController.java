package software.uncharted.terarium.hmiserver.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.ImageService;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

	private final ImageService imageService;

	@GetMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<String> getImageUrl(@PathVariable final UUID id) {
		final String url = imageService.getImageUrl(id);

		if (url == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(url);
	}

	@PutMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Void> storeImage(
		@PathVariable final UUID id,
		@RequestBody final String base64Data
	) {
		imageService.storeImage(id, base64Data);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}")
	@Secured(Roles.USER)
	public ResponseEntity<Void> deleteImage(@PathVariable final UUID id) {
		imageService.deleteImage(id);
		return ResponseEntity.ok().build();
	}
}
