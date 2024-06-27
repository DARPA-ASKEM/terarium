package software.uncharted.terarium.ingest.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.ingest.configuration.Config;
import software.uncharted.terarium.ingest.ingests.IIngest;
import software.uncharted.terarium.ingest.input.IAssetInput;
import software.uncharted.terarium.ingest.input.IEmbeddingInput;
import software.uncharted.terarium.ingest.iterators.JSONLineIterator;
import software.uncharted.terarium.ingest.models.TerariumAssetEmbeddings;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentIngestService {

	final String DOCUMENT_PATH = "documents";
	final String EMBEDDING_PATH = "embeddings";

	private final ObjectMapper objectMapper;
	private final HttpClient client = HttpClient.newHttpClient();
	private final Config config;

	private void createDocument(final JsonNode doc) throws JsonProcessingException, IOException, InterruptedException {

		final String url = String.format("{}/documents", config.getHmiServer().getUrl());

		final String json = objectMapper.writeValueAsString(doc);

		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(json))
				.build();

		// Send the request and receive the response
		final HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new RuntimeException(
					"Failed to create document with status code: " + response.statusCode() + ":" + response.body());
		}
	}

	private void uploadEmbeddings(final UUID id, final TerariumAssetEmbeddings embeddings)
			throws JsonProcessingException, IOException, InterruptedException {

		final String url = String.format("{}/documents/{}/embeddings", config.getHmiServer().getUrl(), id.toString());

		final String json = objectMapper.writeValueAsString(embeddings);

		final HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString(json))
				.build();

		// Send the request and receive the response
		final HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new RuntimeException(
					"Failed to upload embeddings with status code: " + response.statusCode() + ":" + response.body());
		}
	}

	public void createDocuments(final IngestParams params, final IIngest ingest)
			throws IOException, InterruptedException, ExecutionException {

		final Path documentPath = Paths.get(params.getInputDir()).resolve(DOCUMENT_PATH);

		final JSONLineIterator iterator = new JSONLineIterator(documentPath);

		long total = 0;
		long failed = 0;

		while (true) {
			final JsonNode json = iterator.getNext();
			if (json == null) {
				break;
			}

			try {
				final IAssetInput input = objectMapper.treeToValue(json, ingest.getAssetInputClass());

				createDocument(input.getAsset());

				total++;
			} catch (final Exception e) {
				log.error("Failed to create document:", e);
				e.printStackTrace();
				failed++;
			}
		}

		log.info("Created {} documents, failed to create {} documents, total documents: {}", total - failed, failed,
				total);
	}

	public void upsertEmbeddings(final IngestParams params, final IIngest ingest)
			throws IOException, InterruptedException, ExecutionException {

		final Path embeddingPath = Paths.get(params.getInputDir()).resolve(EMBEDDING_PATH);

		final JSONLineIterator iterator = new JSONLineIterator(embeddingPath);

		long total = 0;
		long failed = 0;
		while (true) {
			final JsonNode json = iterator.getNext();
			if (json == null) {
				break;
			}

			try {
				final IEmbeddingInput input = objectMapper.treeToValue(json, ingest.getEmbeddingInputClass());

				uploadEmbeddings(input.getId(), input.getEmbedding());

				total++;
			} catch (final Exception e) {
				log.error("Failed to upload embeddings:", e);
				e.printStackTrace();
				failed++;
			}

		}
		log.info("Updated {} documents with embeddings, failed to update {} documents, total documents: {}",
				total - failed, failed,
				total);
	}

	public void ingest(final IngestParams params, final IIngest ingest)
			throws IOException, InterruptedException, ExecutionException {

		createDocuments(params, ingest);
		upsertEmbeddings(params, ingest);

	}

}
