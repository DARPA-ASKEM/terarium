package software.uncharted.terarium.ingest.service;

import java.util.List;

import lombok.Data;
import software.uncharted.terarium.ingest.input.IAssetInput.AssetType;

@Data
public class IngestParams {

	// Name of the ingest
	String name;

	// The input directory. Ingest expects two child directories:
	// - `{terarium.ingest.input-dir}/embeddings/`
	// - `{terarium.ingest.input-dir}/documents/`
	String inputDir;

	// The output index root to ingest into
	String outputIndexRoot;

	// topics to add to each document
	List<String> topics;

	// The classname used for the ingest.
	String ingestClass;

	// The asset type.
	AssetType assetType;

}
