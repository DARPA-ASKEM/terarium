package software.uncharted.terarium.esingest.service;

import lombok.Data;

@Data
public class ElasticIngestParams {

	// Name of the ingest
	String name;

	// The input directory. Ingest expects two child directories:
	// - `{terarium.esingest.input-dir}/embeddings/`
	// - `{terarium.esingest.input-dir}/documents/`
	String inputDir;

	// The output index root to ingest into
	String outputIndexRoot;

	// Whether or not to clear the index before ingesting
	private boolean clearBeforeIngest = false;

	// The work queue size, determines how many documents / embeddings can queue up
	// while workers are busy
	private int workQueueSize = 36;

	// The number of documents to fail to ingest before the entire ingest is failed.
	private int errorsThreshold = 10;

	// The number of documents to ingest in a single batch
	private int documentBatchSize = 500;

	// The number of embedding chunks to ingest in a single batch
	private int embeddingsBatchSize = 500;

	// The classname used for the ingest.
	String ingestClass;

}
