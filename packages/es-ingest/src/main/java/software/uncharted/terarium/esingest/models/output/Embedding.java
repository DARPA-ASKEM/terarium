package software.uncharted.terarium.esingest.models.output;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class Embedding implements Serializable {

	private UUID documentId;
	private UUID embeddingChunkId;
	private Pair<Long, Long> spans;
	private String title;
	private List<String> doi;
	private double[] vector;
}
