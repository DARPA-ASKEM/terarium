package software.uncharted.terarium.hmiserver.models.dataservice.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import java.io.Serial;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.Grounding;
import software.uncharted.terarium.hmiserver.models.extraction.Extraction;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors(chain = true)
@Entity
@Slf4j
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
	@Deprecated
	private Map<String, JsonNode> metadata;

	@TSOptional
	@Column(columnDefinition = "text")
	private String source;

	@TSOptional
	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "grounding_id")
	private Grounding grounding;

	@TSOptional
	@Lob
	@JdbcTypeCode(Types.BINARY)
	private byte[] thumbnail;

	@TSOptional
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private Extraction extraction;

	@Override
	public List<String> getFileNames() {
		if (this.fileNames == null) {
			this.fileNames = new ArrayList<>();
		}
		return this.fileNames;
	}

	@Override
	public DocumentAsset clone() {
		final DocumentAsset clone = new DocumentAsset();
		super.cloneSuperFields(clone);

		clone.documentUrl = this.documentUrl;
		clone.thumbnail = this.thumbnail;

		if (this.metadata != null) {
			clone.metadata = new HashMap<>();
			for (final String key : this.metadata.keySet()) {
				// I don't like that this is an "object" because it doesn't clone nicely...
				clone.metadata.put(key, this.metadata.get(key).deepCopy());
			}
		}

		clone.source = this.source;

		if (this.grounding != null) {
			clone.grounding = this.grounding.clone();
		}
		return clone;
	}

	@JsonIgnore
	@TSIgnore
	public String getEmbeddingSourceText() {
		try {
			if (getMetadata() != null && getMetadata().containsKey("gollmCard")) {
				// update embeddings
				final JsonNode card = getMetadata().get("gollmCard");
				final ObjectMapper objectMapper = new ObjectMapper();
				return objectMapper.writeValueAsString(card);
			}
			return null;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to serialize model embedding text into JSON", e);
		}
	}
}
