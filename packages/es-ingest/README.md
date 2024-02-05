# Terarium Elasticsearch Ingest

This package is designed to quickly import source documents along with their embeddings for knn semantic search in Elasticsearch.

## How to setup an ingest:

### Create the input class definitions:

An ingest requires an input `InputDocument` class that implements the `IInputDocument` interface and an `InputEmbeddingChunk` class that implements the `IInputEmbeddingChunk` interface.

```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExampleDocument implements IInputDocument {
	UUID id;
  String title;
  String body;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExampleEmbedding implements IInputEmbeddingChunk {
	private UUID id;
	private UUID embeddingChunkId;
	private long[] spans;
	private String title;
	private double[] embedding;
}
```

### Create an `IElasticIngest` implementation:

Each ingest will require some logic to convert the `input` types to output types, this is done by implementing the `IElasticIngest` interface:

```java
public class ExampleIngest implements IElasticIngest<ExampleDocument, Document, ExampleEmbedding, EmbeddingChunk> {

	ObjectMapper mapper = new ObjectMapper();

	public Document processDocument(ExampleDocument input) {
		Document doc = new Document();
		doc.setId(input.getId());
		doc.setTitle(input.getTitle());
		doc.setFullText(input.getBody());
		return doc;
	}

	public EmbeddingChunk processEmbedding(ExampleEmbedding input) {
		Embedding embedding = new Embedding();
		embedding.setEmbeddingId(input.getEmbeddingChunkId());
		embedding.setSpans(input.getSpans());
		embedding.setVector(input.getEmbedding());
		EmbeddingChunk chunk = new EmbeddingChunk();
		chunk.setId(input.getId());
		chunk.setEmbedding(embedding);
		return chunk;
	}

	public ExampleDocument deserializeDocument(String line) {
		try {
			return mapper.readValue(line, CovidDocument.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ExampleEmbedding deserializeEmbedding(String line) {
		try {
			return mapper.readValue(line, CovidEmbedding.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
```

### Configuring the ingest in `application.properties`:

Add an ingest entry to the `application.properties`:

```
terarium.esingest.ingestParams[0].name="A sample ingest"
terarium.esingest.ingestParams[0].inputDir=/path/to/source/dir
terarium.esingest.ingestParams[0].outputIndexRoot=example
terarium.esingest.ingestParams[0].ingestClass=software.uncharted.terarium.esingest.ingests.CovidIngest
terarium.esingest.ingestParams[0].clearBeforeIngest=true
```
