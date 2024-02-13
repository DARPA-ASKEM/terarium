package software.uncharted.terarium.esingest.models.output.document;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import software.uncharted.terarium.esingest.models.output.Embedding;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;

@Data
@JsonInclude(Include.NON_NULL)
public class Document implements IOutputDocument {

	private String id;

	private String title;

	private String fullText;

	private List<Embedding> embeddings;

	private List<String> topics;

	public void addTopics(List<String> ts) {
		if (topics == null) {
			topics = new ArrayList<>();
		}
		topics.addAll(ts);
	}

}
