package software.uncharted.terarium.hmiserver.models;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class TerariumAssetEmbeddings {

	@Data
	public static class Embeddings {

		private String embeddingId;
		private double[] vector;
		private long[] spans;
	}

	private List<Embeddings> embeddings = new ArrayList<>();
}
