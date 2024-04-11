package software.uncharted.terarium.esingest.ingests.model;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.esingest.ingests.IElasticIngest;
import software.uncharted.terarium.esingest.ingests.IElasticPass;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;
import software.uncharted.terarium.esingest.util.ConcurrentBiMap;

@Slf4j
public class ModelIngest implements IElasticIngest {

  ConcurrentBiMap<String, UUID> uuidLookup = new ConcurrentBiMap<>();

  public void setup(ElasticIngestParams params) {}

  public void teardown(ElasticIngestParams params) {}

  public List<IElasticPass<?, ?>> getPasses() {
    return List.of(
        new ModelInsertAMRPass(uuidLookup),
        new ModelAddEmbeddingsPass(uuidLookup),
        new ModelAddMetadataPass(uuidLookup));
  }
}
