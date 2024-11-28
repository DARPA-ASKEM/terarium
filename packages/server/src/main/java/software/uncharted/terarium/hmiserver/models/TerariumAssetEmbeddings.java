package software.uncharted.terarium.hmiserver.models;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class TerariumAssetEmbeddings {

	@Data
	public static class Embedding {

		private String embeddingId;
		private double[] vector;
		private long[] span;
	}

	private List<Embedding> embeddings = new ArrayList<>();
}
