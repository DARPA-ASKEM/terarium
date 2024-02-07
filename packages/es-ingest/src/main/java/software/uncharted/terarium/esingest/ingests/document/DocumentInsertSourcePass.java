package software.uncharted.terarium.esingest.ingests.document;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.ingests.IElasticPass;
import software.uncharted.terarium.esingest.iterators.IInputIterator;
import software.uncharted.terarium.esingest.iterators.JSONLineIterator;
import software.uncharted.terarium.esingest.models.input.document.DocumentSource;
import software.uncharted.terarium.esingest.models.output.Document;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

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

	public List<Document> process(List<DocumentSource> input) {
		List<Document> res = new ArrayList<>();
		for (DocumentSource in : input) {
			Document doc = new Document();
			doc.setId(in.getId());
			doc.setTitle(in.getSource().getTitle());
			doc.setFullText(in.getSource().getBody());
			res.add(doc);
		}
		return res;
	}

}
