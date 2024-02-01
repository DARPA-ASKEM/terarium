package software.uncharted.terarium.esingest.models.input.covid;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CovidEmbedding implements Serializable {

	@JsonAlias("doc_id")
	private UUID documentId;

	@JsonAlias("uuid")
	private UUID embeddingChunkId;

	private Pair<Long, Long> spans;
	private String title;
	private List<String> doi;
	private double[] embedding;
}
