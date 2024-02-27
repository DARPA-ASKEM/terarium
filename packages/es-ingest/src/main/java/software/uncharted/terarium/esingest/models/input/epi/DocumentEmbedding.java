package software.uncharted.terarium.esingest.models.input.epi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import software.uncharted.terarium.esingest.models.input.IInputDocument;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentEmbedding implements IInputDocument {

	@JsonProperty("doc_id")
	private String id;

	@JsonProperty("uuid")
	private String embeddingChunkId;

	private long[] spans;
	private String title;
	private List<String> doi;
	private double[] embedding;
}
