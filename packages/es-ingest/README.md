# Terarium Elasticsearch Ingest

This package is designed to quickly import source documents along with their embeddings for knn semantic search in Elasticsearch.

## How to setup an ingest:

### Create the input and output class definitions:

An ingest requires an input class that implements the `IInputDocument` interface and output class that implements the `IOutputDocument` interface.
```java
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExampleInputDocument implements IInputDocument {
	UUID id;
  String title;
  String body;
}

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExampleOutputDocument implements IOutputDocument {
	UUID id;
  String title;
  String body;
  List<String> topics;

	void addTopics(List<String> topics) {
		if (topics == null) {
			topics = new ArrayList<>();
		}
		topics.addAll(ts);
  }

}

```

### Create an `IElasticPass` implementation:

```java

public class ExampleInsertPass implements IElasticPass<ExampleInputDocument, ExampleOutputDocument> {

	public void setup(final ElasticIngestParams params) {
	}

	public void teardown(final ElasticIngestParams params) {
	}

	public String message() {
		return "Inserting documents";
	}

	public IInputIterator<ExampleInputDocument> getIterator(final ElasticIngestParams params) throws IOException {
		return new JSONLineIterator<>(Paths.get(params.getInputDir()), ExampleInputDocument.class, params.getBatchSize());
	}

	public List<ExampleOutputDocument> process(List<ExampleInputDocument> input) {
		List<ExampleOutputDocument> res = new ArrayList<>();
		for (ExampleInputDocument in : input) {
			ExampleOutputDocument doc = new ExampleOutputDocument();
			doc.setId(in.getId());
			doc.setTitle(in.getSource().getTitle());
		  doc.setFullText(input.getBody());
			res.add(doc);
		}
		return res;
	}
}

```

### Create an `IElasticIngest` implementation:

Each ingest will require some logic to convert the `input` types to output types, this is done by implementing the `IElasticIngest` interface:

```java
public class ExampleIngest implements IElasticIngest {

	public void setup(final ElasticIngestParams params) {
	}

	public void teardown(final ElasticIngestParams params) {
	}

	public List<IElasticPass<?, ?>> getPasses() {
		return List.of(new ExampleInsertPass());
	}

}
```

### Configuring the ingest in `application.properties`:

Add an ingest entry to the `application.properties`:

```
terarium.esingest.ingestParams[0].name="A sample ingest"
terarium.esingest.ingestParams[0].inputDir=/path/to/source/dir
terarium.esingest.ingestParams[0].topics=some,topics,to,add,to,each,doc
terarium.esingest.ingestParams[0].outputIndexRoot=example
terarium.esingest.ingestParams[0].ingestClass=software.uncharted.terarium.esingest.ingests.ExampleIngest
```
