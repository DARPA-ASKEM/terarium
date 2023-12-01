package software.uncharted.terarium.hmiserver.controller.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import lombok.RequiredArgsConstructor;
import software.uncharted.terarium.hmiserver.configuration.ElasticsearchConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.PresignedURL;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;
import software.uncharted.terarium.hmiserver.service.elasticsearch.ElasticsearchService;

@Service
@RequiredArgsConstructor
public class DocumentAssetService {

	private final ElasticsearchService elasticService;
	private final ElasticsearchConfiguration elasticConfig;

	public List<DocumentAsset> getDocumentAssets(Integer page, Integer pageSize) throws IOException {
		final SearchRequest req = new SearchRequest.Builder()
			.index(elasticConfig.getDocumentIndex())
			.from(page)
			.size(pageSize)
			.build();
		return elasticService.search(req, DocumentAsset.class);
	}

	public DocumentAsset getDocumentAsset(String id) throws IOException {
		return elasticService.get(elasticConfig.getDocumentIndex(), id, DocumentAsset.class);
	}

	public void deleteDocumentAsset(String id) throws IOException {
		elasticService.delete(elasticConfig.getDocumentIndex(), id);
	}

	public DocumentAsset createDocumentAsset(DocumentAsset document) throws IOException {
		elasticService.index(elasticConfig.getDocumentIndex(), document.getId(), document);
		return document;
	}

	public DocumentAsset updateDocumentAsset(DocumentAsset document) throws IOException {
		elasticService.index(elasticConfig.getDocumentIndex(), document.getId(), document);
		return document;
	}

	public PresignedURL getUploadUrl(String documentId, String filename)
	{
		// TODO(kbirk): impl
		return null;
	}

	public PresignedURL getDownloadUrl(String documentId, String filename)
	{
		// TODO(kbirk): impl
		return null;
	}

	public String getDocumentDoi(Document doc) {
		String docIdentifier = "";
		if (doc != null && doc.getIdentifier() != null && doc.getIdentifier().size() > 0) {
			for (Map<String, String> identifier : doc.getIdentifier()) {
				if (identifier.get("type").equals("doi")) {
					docIdentifier = identifier.get("id");
					break;
				}
			}
		}
		return docIdentifier;
	}


}
