package software.uncharted.terarium.esingest.ingests.document;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.configuration.Config;
import software.uncharted.terarium.esingest.configuration.ConfigGetter;
import software.uncharted.terarium.esingest.ingests.IElasticPass;
import software.uncharted.terarium.esingest.iterators.IInputIterator;
import software.uncharted.terarium.esingest.iterators.JSONLineIterator;
import software.uncharted.terarium.esingest.models.input.document.DocumentSource;
import software.uncharted.terarium.esingest.models.output.document.Document;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;
import software.uncharted.terarium.esingest.service.s3.S3Service;

@Slf4j
public class DocumentInsertSourcePass
		implements IElasticPass<DocumentSource, Document> {

	final String DOCUMENT_PATH = "documents";

	public void setup(final ElasticIngestParams params) {
	}

	public void teardown(final ElasticIngestParams params) {
	}

	public String message() {
		return "Inserting documents";
	}

	public IInputIterator<DocumentSource> getIterator(final ElasticIngestParams params) throws IOException {
		Path documentPath = Paths.get(params.getInputDir()).resolve(DOCUMENT_PATH);

		return new JSONLineIterator<>(documentPath, DocumentSource.class, params.getBatchSize());
	}

	private String getPath(final UUID id, final String filename) {
		return String.join("/", "document", id.toString(), filename);
	}

	public List<Document> process(List<DocumentSource> input) {

		Config config = ConfigGetter.getConfig();
		S3Service s3Service = new S3Service(config.getAmazon());

		List<Document> res = new ArrayList<>();
		for (DocumentSource in : input) {

			UUID id = UUID.fromString(in.getId());

			Document doc = new Document();
			doc.setId(id);
			doc.setName(in.getSource().getTitle());
			doc.setDescription(in.getSource().getTitle());
			doc.setText(in.getSource().getBody());
			doc.setDoi(in.getSource().getFeature().getDoi());

			final String filename = "source.txt";
			doc.setFilenames(List.of(filename));

			final String bucket = config.getFileStorageS3BucketName();
			final String key = getPath(id, filename);

			s3Service.putObject(bucket, key, in.getSource().getBody().getBytes());

			res.add(doc);
		}
		return res;
	}

}
