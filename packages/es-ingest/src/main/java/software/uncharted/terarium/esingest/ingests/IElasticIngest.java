package software.uncharted.terarium.esingest.ingests;

import java.util.List;
import software.uncharted.terarium.esingest.models.input.IInputDocument;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

public interface IElasticIngest {

  public void setup(final ElasticIngestParams params);

  public void teardown(final ElasticIngestParams params);

  public List<IElasticPass<? extends IInputDocument, ? extends IOutputDocument>> getPasses();
}
