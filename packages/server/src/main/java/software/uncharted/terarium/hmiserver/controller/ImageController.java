package software.uncharted.terarium.hmiserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import software.uncharted.terarium.hmiserver.security.Roles;
import software.uncharted.terarium.hmiserver.service.ImageService;

@RestController
@RequestMapping("/images")
public class ImageController {
    @Autowired
    private ImageService imageService;

    @GetMapping("/{id}")
    @Secured(Roles.USER)
    public ResponseEntity<String> getImageUrl(@PathVariable String id) {
        String url = imageService.getImageUrl(id);

        return ResponseEntity.ok(url);
    }

    @PutMapping("/{id}")
    @Secured(Roles.USER)
    public ResponseEntity<Void> storeImage(@PathVariable String id,
                                         @RequestParam(value = "data", required = true) final String base64Data) {
        imageService.storeImage(id, base64Data);

        return ResponseEntity.ok().build();
    }
}
