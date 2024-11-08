package software.uncharted.terarium.hmiserver.service.s3;

import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.utils.JSONLInputStreamReader;

@Slf4j
@Service
@Configuration
@RequiredArgsConstructor
public class DownloadStaticESIndexService {

	private final S3ClientService s3ClientService;
	private final ElasticsearchService elasticsearchService;

	@Value("${terarium.elasticsearch.index.epi-dkg-root}")
	String epiRoot;

	@Value("${terarium.elasticsearch.index.climate-dkg-root}")
	String climateRoot;

	@Value("${terarium.static-index-path}")
	String staticIndexPath;

	private static String AWS_ID = "static-index";
	private final List<IndexAndMapping> INDICIES = List.of(
		new IndexAndMapping("epi_dkg_20241030") // ,
		//	new IndexAndMapping(climateRoot)
	);

	@Data
	private static class IndexAndMapping {

		public IndexAndMapping(String root) {
			this.fileName = root + ".jsonl";
			this.mappingFileName = root + "_mapping.json";
			this.indexName = root;
		}

		String fileName;
		String indexName;
		String mappingFileName;
	}

	@PostConstruct
	@Async
	void init() {
		final S3Service s3Service = s3ClientService.getS3Service(AWS_ID);

		INDICIES.forEach(index -> {
			if (!elasticsearchService.hasIndex(index.getFileName())) {
				try {
					ResponseInputStream<GetObjectResponse> esMappingStream = s3Service.getObject(
						staticIndexPath,
						index.getMappingFileName()
					);
					String esMappingContent = new BufferedReader(new InputStreamReader(esMappingStream))
						.lines()
						.collect(Collectors.joining("\n"));
					elasticsearchService.createIndex(index.getIndexName(), esMappingContent);
				} catch (Exception e) {
					log.error("Failed to read mapping file: " + index.getMappingFileName(), e);
				}

				log.warn("About to download");
				try {
					ResponseInputStream<GetObjectResponse> esIndexStream = s3Service.getObject(
						staticIndexPath,
						index.getFileName()
					);

					final JSONLInputStreamReader jsonlInputStreamReader = new JSONLInputStreamReader(10 * 1024 * 1024);

					log.warn("We have the stream");

					jsonlInputStreamReader.read(esIndexStream, jsonNodes -> {
						// Insert the documents into the index
						final List<String> ids = jsonNodes.stream().map(node -> node.at("id").asText()).toList();
						try {
							final BulkResponse response = elasticsearchService.insert(staticIndexPath, jsonNodes, ids);
							if (response.errors()) {
								log.error(
									"Failed to insert documents into index {}: {}",
									staticIndexPath,
									response
										.items()
										.stream()
										.filter(item -> item.error() != null)
										.map(item -> item.error().toString())
										.collect(Collectors.joining(", "))
								);
							}
						} catch (IOException e) {
							log.error("Failed to insert documents into index {}", staticIndexPath, e);
						}
					});

					String esIndexContent = new BufferedReader(new InputStreamReader(esIndexStream))
						.lines()
						.collect(Collectors.joining("\n"));
					log.warn("About to insert");
					elasticsearchService.bulkInsert(index.getIndexName(), esIndexContent);
				} catch (Exception e) {
					log.error("Failed to read index file: " + index.getFileName(), e);
				}
			}
		});
	}
}
