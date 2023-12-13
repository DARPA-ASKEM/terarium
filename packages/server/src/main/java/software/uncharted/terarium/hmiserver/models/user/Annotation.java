package software.uncharted.terarium.hmiserver.models.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;


/**
 * Definition for user annotations. An annotation can be applied to TERARium artiacts (e.g. document, model, dataset ...) as additional
 * user-generated metadata. Note to uniquely identify an artifact in TERARium we need a composite key (artifactType, artifactId).
 **/

@Entity
@Data
@Accessors(chain = true)
@NoArgsConstructor
@Table(indexes = {
	@Index(columnList = "artifactType, artifactId")
})
public class Annotation implements Serializable {
	@Serial
	private static final long serialVersionUID = 4939620703969267901L;
	@Id
	private String id = UUID.randomUUID().toString();

	@Column(nullable = false)
	private Long timestampMillis = Instant.now().toEpochMilli();

	@Column()
	private UUID projectId;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private String userId;

	@JsonProperty("artifact_id")
	private String artifactId;

	@JsonProperty("artifact_type")
	private String artifactType;

	private String section;

}
