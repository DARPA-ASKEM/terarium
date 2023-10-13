package software.uncharted.terarium.hmiserver.controller;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.controller.services.DownloadService;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

@RequestMapping("/download")
@RestController
@Slf4j
public class DownloadController {

	private final DownloadService downloadService;

	@Autowired
    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

	@GetMapping
	public ResponseEntity<Resource> get(@RequestParam("doi") final String doi) throws IOException, URISyntaxException {
		final byte[] pdfBytes = downloadService.getPDF("https://unpaywall.org/" + doi);
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
	public ResponseEntity<String> getURL(@QueryParam("url") final String url) throws IOException, URISyntaxException {
		final String pdfLink = downloadService.getPDFURL("https://unpaywall.org/" + url);
		if (pdfLink != null) {
			return ResponseEntity.ok(pdfLink);
		}
		return ResponseEntity.noContent().build();
	}

}
