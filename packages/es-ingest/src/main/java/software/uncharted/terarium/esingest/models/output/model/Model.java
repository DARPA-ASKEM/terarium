package software.uncharted.terarium.esingest.models.output.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import software.uncharted.terarium.esingest.models.input.model.ModelMetadata.Author;
import software.uncharted.terarium.esingest.models.output.Embedding;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;

@Data
@JsonInclude(Include.NON_EMPTY)
public class Model implements IOutputDocument {

	@Data
	@JsonInclude(Include.NON_EMPTY)
	static public class Metadata {
		private String title;
		private String doi;
		private String type;
		private List<String> issn;
		private String journal;
		private String publisher;
		private String year;
		private List<Author> author;
		private Object gollmCard;
		private JsonNode annotations;
	}

	private UUID id;

	@JsonProperty
	public void setId(UUID id) {
		this.id = id;
	}

	@JsonIgnore
	public UUID getId() {
		return id;
	}

	private JsonNode header;
	private JsonNode model;
	private JsonNode semantics;
	private Metadata metadata = new Metadata();

	private Timestamp createdOn = new Timestamp(System.currentTimeMillis());
	private Timestamp updatedOn = createdOn;
	private Timestamp deletedOn = null;

	private Boolean temporary = false;
	private Boolean publicAsset = true;

	private List<Embedding> embeddings;
	private List<String> topics;

	public void addTopics(List<String> ts) {
		if (topics == null) {
			topics = new ArrayList<>();
		}
		topics.addAll(ts);
	}

}
