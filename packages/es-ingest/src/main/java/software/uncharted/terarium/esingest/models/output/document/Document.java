package software.uncharted.terarium.esingest.models.output.document;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import software.uncharted.terarium.esingest.models.output.Embedding;
import software.uncharted.terarium.esingest.models.output.IOutputDocument;

@Data
@JsonInclude(Include.NON_EMPTY)
public class Document implements IOutputDocument {

	private UUID id;
	private String name;
	private String description;
	private String text;
	private List<String> doi;
	private List<String> filenames;

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
