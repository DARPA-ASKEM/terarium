package software.uncharted.terarium.esingest.service;

import lombok.Data;

@Data
public class ElasticIngestParams {

	private String inputDir;
	private String outputIndex;

}
