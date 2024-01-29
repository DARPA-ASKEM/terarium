package software.uncharted.terarium.taskrunner.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaskDeploymentService {

	private final ObjectMapper objectMapper;

	@Value("terariuam.taskrunner.auto-update:false")
	private boolean autoUpdateTasks;

	@Value("terariuam.taskrunner.auto-update-interval-seconds:300000")
	private int autoUpdateIntervalSeconds;

	@Value("terariuam.taskrunner.task-dir:/")
	private String taskRepoDir;

	@Value("terariuam.taskrunner.service-repo:DARPA-ASKEM/GoLLM")
	private String serviceRepo;

	private AtomicReference<String> lastCheckedTag = new AtomicReference<>("");

	private String getLatestTag() throws Exception {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(new URI("https://api.github.com/repos/" + serviceRepo + "/tags"))
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		List<JsonNode> tags = objectMapper.readValue(response.body(), new TypeReference<List<JsonNode>>() {
		});
		if (tags.size() > 0) {
			return tags.get(0).get("name").asText();
		} else {
			return null;
		}
	}

	private void deployRepository(String tag) throws IOException, InterruptedException {
		String url = "https://github.com/" + serviceRepo + "/archive/refs/tags/" + tag + ".tar.gz";

		// Download the tarball
		InputStream in = new URL(url).openStream();
		Files.copy(in, Paths.get(serviceRepo + "-" + tag + ".tar.gz"),
				StandardCopyOption.REPLACE_EXISTING);

		// Extract the tarball
		try (InputStream fi = Files.newInputStream(Paths.get(serviceRepo + "-" + tag + ".tar.gz"));
				BufferedInputStream bi = new BufferedInputStream(fi);
				GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
				TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

			TarArchiveEntry entry;
			while ((entry = (TarArchiveEntry) ti.getNextEntry()) != null) {
				Path outputPath = Paths.get(taskRepoDir, entry.getName());
				if (entry.isDirectory()) {
					Files.createDirectories(outputPath);
				} else {
					Files.createDirectories(outputPath.getParent());
					try (OutputStream outputFileStream = Files.newOutputStream(outputPath)) {
						IOUtils.copy(ti, outputFileStream);
					}
				}
			}
		}

		// Install deps
		ProcessBuilder install = new ProcessBuilder("pip", "install", "--no-cache-dir", "-r", "requirements.txt");
		install.directory(new File(taskRepoDir));
		Process process1 = install.start();
		process1.waitFor();

		// Install repo
		ProcessBuilder run = new ProcessBuilder("pip", "install", "-e", ".");
		run.directory(new File(taskRepoDir));
		Process process2 = run.start();
		process2.waitFor();
	}

	@Scheduled(fixedDelayString = "${terariuam.taskrunner.auto-update-interval-milliseconds}")
	public void redeploy() throws Exception {
		if (!autoUpdateTasks) {
			// don't update
			return;
		}

		String tag = getLatestTag();

		if (lastCheckedTag.get().equals(tag)) {
			// up to date
			return;
		}

		log.info("Deploying new version of {}:{}", serviceRepo, tag);

		deployRepository(tag);

		// update the tag
		lastCheckedTag.set(tag);
	}

}
