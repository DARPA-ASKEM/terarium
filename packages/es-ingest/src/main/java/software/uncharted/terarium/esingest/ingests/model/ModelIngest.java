package software.uncharted.terarium.esingest.ingests.model;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.ingests.IElasticIngest;
import software.uncharted.terarium.esingest.ingests.IElasticPass;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

@Slf4j
public class ModelIngest implements IElasticIngest {

	public void setup(ElasticIngestParams params) {
	}

	public void teardown(ElasticIngestParams params) {
	}

	public List<IElasticPass<?, ?>> getPasses() {
		return List.of(
				new ModelInsertEmbeddingsPass(),
				new ModelAddMetadataPass(),
				new ModelAddAMRPass());

	}

}
