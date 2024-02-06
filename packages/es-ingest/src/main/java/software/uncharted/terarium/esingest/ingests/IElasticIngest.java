package software.uncharted.terarium.esingest.ingests;

import java.io.IOException;
import java.nio.file.Path;

import software.uncharted.terarium.esingest.iterators.IInputIterator;
import software.uncharted.terarium.esingest.models.input.IInputDocument;
import software.uncharted.terarium.esingest.models.input.IInputEmbeddingChunk;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;
import software.uncharted.terarium.esingest.models.output.IOutputEmbeddingChunk;

public interface IElasticIngest<DocInputType extends IInputDocument, DocOutputType extends IOutputDocument, EmbeddingInputChunkType extends IInputEmbeddingChunk, EmbeddingOutputChunkType extends IOutputEmbeddingChunk> {

	public IInputIterator<DocInputType> getDocumentInputIterator(Path inputPath, long batchSize) throws IOException;

	public IInputIterator<EmbeddingInputChunkType> getEmbeddingInputIterator(Path inputPath, long batchSize)
			throws IOException;

	public DocOutputType processDocument(DocInputType input);

	public EmbeddingOutputChunkType processEmbedding(EmbeddingInputChunkType input);
}
