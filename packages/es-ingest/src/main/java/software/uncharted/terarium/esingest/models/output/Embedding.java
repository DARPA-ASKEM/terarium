package software.uncharted.terarium.esingest.models.output;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class Embedding implements Serializable {

	private UUID embeddingId;
	private double[] vector;
	private long[] spans;

}
