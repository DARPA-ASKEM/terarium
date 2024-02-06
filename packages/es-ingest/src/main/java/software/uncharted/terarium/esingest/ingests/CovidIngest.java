package software.uncharted.terarium.esingest.ingests;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.iterators.IInputIterator;
import software.uncharted.terarium.esingest.iterators.JSONLineReaderIterator;
import software.uncharted.terarium.esingest.models.input.covid.CovidDocument;
import software.uncharted.terarium.esingest.models.input.covid.CovidEmbedding;
import software.uncharted.terarium.esingest.models.output.Document;
import software.uncharted.terarium.esingest.models.output.Embedding;
import software.uncharted.terarium.esingest.models.output.EmbeddingChunk;

@Slf4j
public class CovidIngest
		implements IElasticIngest<CovidDocument, Document, CovidEmbedding, EmbeddingChunk> {

	final String DOCUMENT_PATH = "documents";
	final String EMBEDDING_PATH = "embeddings";

	public IInputIterator<CovidDocument> getDocumentInputIterator(Path inputPath, long batchSize) throws IOException {
		Path documentPath = inputPath.resolve(DOCUMENT_PATH);

		return new JSONLineReaderIterator<>(documentPath, CovidDocument.class, batchSize);
	}

	public IInputIterator<CovidEmbedding> getEmbeddingInputIterator(Path inputPath, long batchSize) throws IOException {
		Path embeddingsPath = inputPath.resolve(EMBEDDING_PATH);

		return new JSONLineReaderIterator<>(embeddingsPath, CovidEmbedding.class,

				// NOTE: we want to upload _all_ embedding chunks in a single payload, so we
				// need to ensure that when a worker receives the embeddings, it has all the
				// embeddings for a single document and it is not split between workers.

				(List<CovidEmbedding> batch, CovidEmbedding latestToAdd) -> {
					// if we are under the batch size, don't chunk
					if (batch.size() < batchSize) {
						return false;
					}

					// if we are over, only split if the newest item is for a different doc

					CovidEmbedding last = batch.get(batch.size() - 1);

					// do not chunk unless we have different doc ids
					return !last.getId().equals(latestToAdd.getId());
				});
	}

	public Document processDocument(CovidDocument input) {

		Document doc = new Document();
		doc.setId(input.getId());
		doc.setTitle(input.getSource().getTitle());
		doc.setFullText(input.getSource().getBody());

		return doc;
	}

	public EmbeddingChunk processEmbedding(CovidEmbedding input) {
		Embedding embedding = new Embedding();
		embedding.setEmbeddingId(input.getEmbeddingChunkId());
		embedding.setSpans(input.getSpans());
		embedding.setVector(input.getEmbedding());

		EmbeddingChunk chunk = new EmbeddingChunk();
		chunk.setId(input.getId());
		chunk.setEmbedding(embedding);

		return chunk;
	}

}
