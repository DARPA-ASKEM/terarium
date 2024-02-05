package software.uncharted.terarium.esingest.models.output;

import java.util.UUID;

import lombok.Data;

@Data
public class Embedding {

	private UUID embeddingId;
	private double[] vector;
	private long[] spans;

}
