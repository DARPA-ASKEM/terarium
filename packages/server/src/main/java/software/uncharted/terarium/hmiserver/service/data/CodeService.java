package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
@RequiredArgsConstructor
public class CodeService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	private final Config config;
	private final S3ClientService s3ClientService;

	public List<Code> getCode(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
				.index(elasticConfig.getCodeIndex())
				.from(page)
				.size(pageSize)
				.build();
		return elasticService.search(req, Code.class);
	}

	public Code getCode(String id) throws IOException {
		return elasticService.get(elasticConfig.getCodeIndex(), id, Code.class);
	}

	public void deleteCode(String id) throws IOException {
		elasticService.delete(elasticConfig.getCodeIndex(), id);
	}

	public Code createCode(Code code) throws IOException {
		elasticService.index(elasticConfig.getCodeIndex(), code.getId(), code);
		return code;
	}

	public Code updateCode(Code code) throws IOException {
		elasticService.index(elasticConfig.getCodeIndex(), code.getId(), code);
		return code;
	}

	private String getPath(String codeId, String filename) {
		return String.join("/", config.getCodePath(), codeId, filename);
	}

	public PresignedURL getUploadUrl(String codeId, String filename) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedPutUrl(
				config.getFileStorageS3BucketName(),
				getPath(codeId, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("PUT");
		return presigned;
	}

	public PresignedURL getDownloadUrl(String codeId, String filename) {
		long HOUR_EXPIRATION = 60;

		PresignedURL presigned = new PresignedURL();
		presigned.setUrl(s3ClientService.getS3Service().getS3PreSignedGetUrl(
				config.getFileStorageS3BucketName(),
				getPath(codeId, filename),
				HOUR_EXPIRATION));
		presigned.setMethod("GET");
		return presigned;
	}

}
