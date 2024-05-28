package software.uncharted.terarium.hmiserver.models.dataservice.code;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Code extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = 3041175096070970227L;

	/* Files that contain dynamics */
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "{}")
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@MapKeyColumn(name = "fileName")
	@JoinColumn(name = "code_id")
	private Map<String, CodeFile> files;

	/* The optional URL for where this code came from */
	@TSOptional
	@JsonAlias("repo_url")
	@Column(length = 512)
	private String repoUrl;

	/* The optional metadata for this code */
	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY, defaultValue = "{}")
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private Map<String, String> metadata;

	@Override
	public Code clone() {
		final Code clone = new Code();
		cloneSuperFields(clone);

		if (this.files != null) {
			clone.files = new HashMap<>();
			for (final String fileName : this.files.keySet()) {
				clone.files.put(fileName, this.files.get(fileName).clone());
			}
		}

		clone.repoUrl = this.repoUrl;
		if (this.metadata != null) {
			clone.metadata = new HashMap<>();
			for (final String key : this.metadata.keySet()) {
				clone.metadata.put(key, this.metadata.get(key));
			}
		}

		return clone;
	}
}
