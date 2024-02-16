package software.uncharted.terarium.esingest.service;

import java.util.List;

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

	// topics to add to each document
	List<String> topics;

	// The work queue size, determines how many documents / embeddings can queue up
	// while workers are busy
	int workQueueSize = 36;

	// The number of documents to fail to ingest before the entire ingest is failed.
	int errorsThreshold = 10;

	// The number of documents to process in a single batch.
	int batchSize = 500;

	// The classname used for the ingest.
	String ingestClass;

}
