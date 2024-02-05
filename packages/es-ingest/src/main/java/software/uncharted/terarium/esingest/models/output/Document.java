package software.uncharted.terarium.esingest.models.output;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class Document implements IOutputDocument {

	private UUID id;

	private String title;

	private String fullText;

	private List<Embedding> embeddings;

	public void addEmbedding(Embedding embedding) {
		if (embeddings == null) {
			embeddings = new ArrayList<>();
		}
		embeddings.add(embedding);
	}

}
