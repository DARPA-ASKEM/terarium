package software.uncharted.terarium.hmiserver.controller;

import jakarta.ws.rs.QueryParam;
import java.io.IOException;
import java.net.URISyntaxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import software.uncharted.terarium.hmiserver.controller.services.DownloadService;
import software.uncharted.terarium.hmiserver.security.Roles;

@RequestMapping("/download")
@RestController
@Slf4j
public class DownloadController {

	@GetMapping
	@Secured(Roles.USER)
	public ResponseEntity<Resource> get(@RequestParam("doi") final String doi) throws IOException, URISyntaxException {
		final byte[] pdfBytes = DownloadService.getPDF("https://unpaywall.org/" + doi);
		if (pdfBytes != null) {
			return ResponseEntity.ok()
				.headers(new HttpHeaders())
				.contentLength(pdfBytes.length)
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.body(new ByteArrayResource(pdfBytes));
		} else {
			return ResponseEntity.internalServerError().build();
		}
	}

	@GetMapping("/url")
	@Secured(Roles.USER)
	public ResponseEntity<String> getURL(@QueryParam("url") final String url) throws IOException, URISyntaxException {
		final String pdfLink = DownloadService.getPDFURL("https://unpaywall.org/" + url);
		if (pdfLink != null) {
			return ResponseEntity.ok(pdfLink);
		}
		return ResponseEntity.noContent().build();
	}
}
