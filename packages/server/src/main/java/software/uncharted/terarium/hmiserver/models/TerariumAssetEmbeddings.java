package software.uncharted.terarium.hmiserver.models;

import java.util.List;

import lombok.Data;

@Data
public class TerariumAssetEmbeddings {

	@Data
	static public class Embeddings {
		private String embeddingId;
		private double[] vector;
		private long[] spans;
	}

	private List<String> topics;
	private List<Embeddings> embeddings;

}
