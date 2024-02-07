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
import software.uncharted.terarium.esingest.models.input.document.DocumentEmbedding;
import software.uncharted.terarium.esingest.models.output.Embedding;
import software.uncharted.terarium.esingest.models.output.document.Document;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

@Slf4j
public class DocumentAddEmbeddingsPass
		implements IElasticPass<DocumentEmbedding, Document> {

	final String EMBEDDING_PATH = "embeddings";

	public void setup(final ElasticIngestParams params) {
	}

	public void teardown(final ElasticIngestParams params) {
	}

	public String message() {
		return "Inserting embeddings";
	}

	public IInputIterator<DocumentEmbedding> getIterator(final ElasticIngestParams params) throws IOException {
		Path embeddingsPath = Paths.get(params.getInputDir()).resolve(EMBEDDING_PATH);

		return new JSONLineIterator<>(embeddingsPath, DocumentEmbedding.class,

				// NOTE: we want to upload _all_ embedding chunks in a single payload, so we
				// need to ensure that when a worker receives the embeddings, it has all the
				// embeddings for a single document and it is not split between workers.

				(List<DocumentEmbedding> batch, DocumentEmbedding latestToAdd) -> {
					// if we are under the batch size, don't chunk
					if (batch.size() < params.getBatchSize()) {
						return false;
					}

					// if we are over, only split if the newest item is for a different doc

					DocumentEmbedding last = batch.get(batch.size() - 1);

					// do not chunk unless we have different doc ids
					return !last.getId().equals(latestToAdd.getId());
				});
	}

	private Embedding process(DocumentEmbedding input) {
		Embedding embedding = new Embedding();
		embedding.setEmbeddingId(input.getEmbeddingChunkId());
		embedding.setSpans(input.getSpans());
		embedding.setVector(input.getEmbedding());
		return embedding;
	}

	public List<Document> process(List<DocumentEmbedding> input) {
		List<Document> output = new ArrayList<>();
		List<Embedding> embeddings = new ArrayList<>();
		String currentId = null;

		for (DocumentEmbedding in : input) {
			Embedding embedding = process(in);
			if (embedding == null) {
				continue;
			}

			if (currentId == null) {
				// create a new partial
				currentId = in.getId();
			} else if (!currentId.equals(in.getId())) {
				// embedding references a new doc, add existing partial to output, create next
				// one
				Document doc = new Document();
				doc.setEmbeddings(embeddings);
				output.add(doc);

				currentId = in.getId();
				embeddings = new ArrayList<>();
			}

			embeddings.add(embedding);
		}
		return output;
	}

}
