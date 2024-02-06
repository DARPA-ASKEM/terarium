package software.uncharted.terarium.esingest.ingests;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.uncharted.terarium.esingest.models.input.covid.CovidDocument;
import software.uncharted.terarium.esingest.models.input.covid.CovidEmbedding;
import software.uncharted.terarium.esingest.models.output.Document;
import software.uncharted.terarium.esingest.models.output.Embedding;
import software.uncharted.terarium.esingest.models.output.EmbeddingChunk;

public class CovidIngest
		implements IElasticIngest<CovidDocument, Document, CovidEmbedding, EmbeddingChunk> {

	ObjectMapper mapper = new ObjectMapper();

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

	public CovidDocument deserializeDocument(String line) {
		try {
			return mapper.readValue(line, CovidDocument.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public CovidEmbedding deserializeEmbedding(String line) {
		try {
			return mapper.readValue(line, CovidEmbedding.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
