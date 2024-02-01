package software.uncharted.terarium.esingest.models.input.covid;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.esingest.models.input.InputInterface;

@Accessors(chain = true)
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CovidEmbedding implements InputInterface, Serializable {

	@JsonProperty("doc_id")
	private UUID id;

	@JsonProperty("uuid")
	private UUID embeddingChunkId;

	private long[] spans;
	private String title;
	private List<String> doi;
	private double[] embedding;
}
