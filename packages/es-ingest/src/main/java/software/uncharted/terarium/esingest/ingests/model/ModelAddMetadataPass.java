package software.uncharted.terarium.esingest.ingests.model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.ingests.IElasticPass;
import software.uncharted.terarium.esingest.iterators.IInputIterator;
import software.uncharted.terarium.esingest.iterators.JSONKeyIterator;
import software.uncharted.terarium.esingest.models.input.model.ModelMetadata;
import software.uncharted.terarium.esingest.models.output.model.Model;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

@Slf4j
public class ModelAddMetadataPass
		implements IElasticPass<ModelMetadata, Model> {

	final ObjectMapper mapper = new ObjectMapper();
	final String MODEL_PATH = "models";

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
			Model doc = new Model();
			doc.setId(in.getId());
			doc.setTitle(in.getPublicationMetadata().getTitle());
			doc.setDoi(in.getPublicationMetadata().getDoi());
			doc.setType(in.getPublicationMetadata().getType());
			doc.setIssn(in.getPublicationMetadata().getIssn());
			doc.setJournal(in.getPublicationMetadata().getJournal());
			doc.setPublisher(in.getPublicationMetadata().getPublisher());
			doc.setYear(in.getPublicationMetadata().getYear());
			doc.setAuthor(in.getPublicationMetadata().getAuthor());
			res.add(doc);
		}
		return res;
	}

}
