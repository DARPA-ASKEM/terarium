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
import software.uncharted.terarium.esingest.iterators.JSONFileIterator;
import software.uncharted.terarium.esingest.models.input.model.ModelAMR;
import software.uncharted.terarium.esingest.models.output.model.Model;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

@Slf4j
public class ModelInsertAMRPass
		implements IElasticPass<ModelAMR, Model> {

	final String AMR_PATH = "amr";

	ConcurrentMap<String, UUID> uuidLookup;

	ModelInsertAMRPass(ConcurrentMap<String, UUID> uuidLookup) {
		this.uuidLookup = uuidLookup;
	}

	public void setup(final ElasticIngestParams params) {
	}

	public void teardown(final ElasticIngestParams params) {
	}

	public String message() {
		return "Inserting AMRs into models";
	}

	public IInputIterator<ModelAMR> getIterator(final ElasticIngestParams params) throws IOException {
		Path amrPath = Paths.get(params.getInputDir()).resolve(AMR_PATH);

		return new JSONFileIterator<>(amrPath, ModelAMR.class, params.getBatchSize());
	}

	public List<Model> process(List<ModelAMR> input) {
		List<Model> res = new ArrayList<>();
		for (ModelAMR in : input) {

			UUID uuid = UUID.randomUUID();
			uuidLookup.put(in.getId(), uuid);

			Model doc = new Model();
			doc.setId(uuid);
			doc.setHeader(in.getHeader());
			doc.setModel(in.getModel());
			doc.setSemantics(in.getSemantics());
			if (in.getMetadata() != null) {
				doc.getMetadata().setAnnotations(in.getMetadata().get("annotations"));
			}
			res.add(doc);
		}
		return res;
	}

}
