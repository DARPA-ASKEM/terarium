package software.uncharted.terarium.esingest.ingests;

import java.io.IOException;
import java.util.List;

import software.uncharted.terarium.esingest.iterators.IInputIterator;
import software.uncharted.terarium.esingest.models.input.IInputDocument;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;
import software.uncharted.terarium.esingest.service.ElasticIngestParams;

public interface IElasticPass<DocInputType extends IInputDocument, DocOutputType extends IOutputDocument> {

	public void setup(final ElasticIngestParams params);

	public void teardown(final ElasticIngestParams params);

	public String message();

	public IInputIterator<DocInputType> getIterator(final ElasticIngestParams params) throws IOException;

	public List<DocOutputType> process(List<DocInputType> input);
}
