package software.uncharted.terarium.esingest.models.output.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import software.uncharted.terarium.esingest.models.input.model.ModelMetadata.Author;
import software.uncharted.terarium.esingest.models.output.Embedding;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;

@Data
@JsonInclude(Include.NON_NULL)
public class Model implements IOutputDocument {

	private String id;

	private String title;
	private String doi;
	private String type;
	private List<String> issn;
	private String journal;
	private String publisher;
	private String year;
	private List<Author> author;

	private JsonNode amr;

	private List<Embedding> embeddings;

	private String modelCard;

	private List<String> topics;

	public void addTopics(List<String> ts) {
		if (topics == null) {
			topics = new ArrayList<>();
		}
		topics.addAll(ts);
	}

}
