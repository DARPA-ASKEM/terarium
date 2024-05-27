package software.uncharted.terarium.hmiserver.models.dataservice.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.documentservice.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors(chain = true)
@Entity
public class DocumentAsset extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -8425680186002783351L;

	@TSOptional
	@Column(length = 255)
	private String userId;

	@TSOptional
	@JsonAlias("document_url")
	@Column(length = 1024)
	private String documentUrl;

	@TSOptional
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private Map<String, Object> metadata;

	@TSOptional
	@Column(columnDefinition = "text")
	private String source;

	@TSOptional
	@Column(columnDefinition = "text")
	private String text;

	@TSOptional
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "grounding_id")
	private Grounding grounding;

	@TSOptional
	@Column(columnDefinition = "text")
	private String documentAbstract;

	@TSOptional
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private List<DocumentExtraction> assets;

	/**
	 * Get the DOI of a document
	 *
	 * @param doc
	 * @return the DOI of the document, or an empty string if no DOI is found
	 */
	public static String getDocumentDoi(final Document doc) {
		String docIdentifier = "";
		if (doc != null && doc.getIdentifier() != null && !doc.getIdentifier().isEmpty()) {
			for (final Map<String, String> identifier : doc.getIdentifier()) {
				if (identifier.get("type").equals("doi")) {
					docIdentifier = identifier.get("id");
					break;
				}
			}
		}
		return docIdentifier;
	}

	@Override
	public DocumentAsset clone() {
		final DocumentAsset clone = new DocumentAsset();
		super.cloneSuperFields(clone);

		clone.documentUrl = this.documentUrl;

		if (this.metadata != null) {
			clone.metadata = new HashMap<>();
			for (final String key : this.metadata.keySet()) {
				// I don't like that this is an "object" because it doesn't clone nicely...
				clone.metadata.put(key, this.metadata.get(key));
			}
		}

		clone.source = this.source;
		clone.text = this.text;

		if (this.grounding != null) {
			clone.grounding = this.grounding.clone();
		}

		if (this.assets != null) {
			clone.assets = new ArrayList<>();
			for (final DocumentExtraction asset : this.assets) {
				clone.assets.add(asset.clone());
			}
		}

		return clone;
	}
}
