package software.uncharted.terarium.esingest.ingests.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.ingests.IElasticPass;
import software.uncharted.terarium.esingest.iterators.IInputIterator;
import software.uncharted.terarium.esingest.iterators.JSONLineIterator;
import software.uncharted.terarium.esingest.models.input.model.ModelEmbedding;
import software.uncharted.terarium.esingest.models.output.Embedding;
import software.uncharted.terarium.esingest.models.output.model.Model;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

@Slf4j
public class ModelAddEmbeddingsPass
		implements IElasticPass<ModelEmbedding, Model> {

	final String EMBEDDING_PATH = "embeddings";

	ConcurrentMap<String, UUID> uuidLookup;

	ModelAddEmbeddingsPass(ConcurrentMap<String, UUID> uuidLookup) {
		this.uuidLookup = uuidLookup;
	}

	public void setup(final ElasticIngestParams params) {
	}

	public void teardown(final ElasticIngestParams params) {
	}

	public String message() {
		return "Inserting model cards and embeddings";
	}

	public IInputIterator<ModelEmbedding> getIterator(final ElasticIngestParams params) throws IOException {
		Path embeddingPath = Paths.get(params.getInputDir()).resolve(EMBEDDING_PATH);

		return new JSONLineIterator<>(embeddingPath, ModelEmbedding.class, params.getBatchSize());
	}

	public List<Model> process(List<ModelEmbedding> input) {
		List<Model> res = new ArrayList<>();
		for (ModelEmbedding in : input) {

			if (!uuidLookup.containsKey(in.getId())) {
				// no embedding for this model
				continue;
			}

			UUID uuid = uuidLookup.get(in.getId());

			Model doc = new Model();
			doc.setId(uuid);
			doc.getMetadata().setModelCard(in.getModelCard().toString());

			Embedding embedding = new Embedding();
			embedding.setEmbeddingId(UUID.randomUUID().toString());
			embedding.setVector(in.getEmbedding());

			doc.setEmbeddings(List.of(embedding));
			res.add(doc);
		}
		return res;
	}

}
