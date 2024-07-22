package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

/**
 * Represents a generic artifact that can be stored in the data service. For example, this could be a text file, a code
 * file, a zip file, or anything else. It should not be used for a dataset or a model, which have their own classes.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
@Entity
public class Artifact extends TerariumAsset {

	@Serial
	private static final long serialVersionUID = -1122602270904707476L;

	/* UserId of who created this asset */
	@Column(length = 255)
	private String userId;

	/* metadata for these files */
	@TSOptional
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private JsonNode metadata;

	@Override
	public Artifact clone() {
		final Artifact clone = new Artifact();
		cloneSuperFields(clone);

		clone.userId = this.userId;

		if (this.metadata != null) {
			clone.metadata = this.metadata.deepCopy();
		}

		return clone;
	}
}
