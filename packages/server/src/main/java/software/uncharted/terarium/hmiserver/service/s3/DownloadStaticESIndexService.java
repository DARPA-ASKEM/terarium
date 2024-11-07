package software.uncharted.terarium.hmiserver.service.s3;

import jakarta.annotation.PostConstruct;
import java.util.List;
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
		new IndexAndMapping(epiRoot),
		new IndexAndMapping(climateRoot)
	);

	@Data
	private static class IndexAndMapping {

		public IndexAndMapping(String root) {
			this.indexName = root + ".json";
			this.mapping = root + "_mapping.json";
		}

		String indexName;
		String mapping;
	}

	@PostConstruct
	@Async
	void init() {
		INDICIES.forEach(index -> {
			if (!elasticsearchService.hasIndex(index.getIndexName())) {
				// Get the input stream from s3 for the source data
				final S3Service s3Service = s3ClientService.getS3Service(AWS_ID);
				ResponseInputStream<GetObjectResponse> responseInputStream = s3Service.getObject(
					staticIndexPath,
					index.getIndexName()
				);
			}
		});
	}
}
