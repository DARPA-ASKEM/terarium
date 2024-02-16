package software.uncharted.terarium.esingest.ingests.document;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.ingests.IElasticIngest;
import software.uncharted.terarium.esingest.ingests.IElasticPass;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

@Slf4j
public class DocumentIngest implements IElasticIngest {

	public void setup(final ElasticIngestParams params) {
	}

	public void teardown(final ElasticIngestParams params) {
	}

	public List<IElasticPass<?, ?>> getPasses() {
		return List.of(
				new DocumentInsertSourcePass(),
				new DocumentAddEmbeddingsPass());

	}

}
