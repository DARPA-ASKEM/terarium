package software.uncharted.terarium.esingest.ingests.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.ingests.IElasticPass;
import software.uncharted.terarium.esingest.iterators.IInputIterator;
import software.uncharted.terarium.esingest.iterators.JSONKeyIterator;
import software.uncharted.terarium.esingest.models.input.model.ModelMetadata;
import software.uncharted.terarium.esingest.models.output.model.Model;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;
import software.uncharted.terarium.esingest.util.ConcurrentBiMap;

@Slf4j
public class ModelAddMetadataPass
		implements IElasticPass<ModelMetadata, Model> {

	final ObjectMapper mapper = new ObjectMapper();
	final String MODEL_PATH = "models";

	ConcurrentBiMap<String, UUID> uuidLookup;

	ModelAddMetadataPass(ConcurrentBiMap<String, UUID> uuidLookup) {
		this.uuidLookup = uuidLookup;
	}

	public void setup(final ElasticIngestParams params) {
	}

	public void teardown(final ElasticIngestParams params) {
	}

	public String message() {
		return "Inserting metadata into models";
	}

	public IInputIterator<ModelMetadata> getIterator(final ElasticIngestParams params) throws IOException {
		Path modelPath = Paths.get(params.getInputDir()).resolve(MODEL_PATH);

		return new JSONKeyIterator<>(modelPath, ModelMetadata.class, params.getBatchSize());
	}

	public List<Model> process(List<ModelMetadata> input) {
		List<Model> res = new ArrayList<>();
		for (ModelMetadata in : input) {

			if (!uuidLookup.containsKey(in.getId())) {
				// no amr for this model
				continue;
			}

			UUID uuid = uuidLookup.get(in.getId());

			Model doc = new Model();
			doc.setId(uuid);
			doc.getMetadata().setTitle(in.getPublicationMetadata().getTitle());
			doc.getMetadata().setDoi(in.getPublicationMetadata().getDoi());
			doc.getMetadata().setType(in.getPublicationMetadata().getType());
			doc.getMetadata().setIssn(in.getPublicationMetadata().getIssn());
			doc.getMetadata().setJournal(in.getPublicationMetadata().getJournal());
			doc.getMetadata().setPublisher(in.getPublicationMetadata().getPublisher());
			doc.getMetadata().setYear(in.getPublicationMetadata().getYear());
			doc.getMetadata().setAuthor(in.getPublicationMetadata().getAuthor());
			res.add(doc);
		}
		return res;
	}

}
