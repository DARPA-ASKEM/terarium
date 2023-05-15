package software.uncharted.terarium.hmiserver.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
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
@EqualsAndHashCode(callSuper = true)
public class Annotation extends PanacheEntityBase implements Serializable {
	@Id
	private String id = UUID.randomUUID().toString();

	@Column(nullable = false)
	private Long timestampMillis = Instant.now().toEpochMilli();

	@Column(nullable = true)
	private Long projectId;

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private String username;

	@JsonProperty("artifact_id")
	private String artifactId;

	@JsonProperty("artifact_type")
	private String artifactType;

	@Column(nullable = true)
	private String section;

	/**
	 * Find annotations by artifactType and artifactId
	 */
	public static List<Annotation> findByArtifact(String artifactType, String artifactId) {
		PanacheQuery<Annotation> query;

		query = find("artifacttype = ?1 and artifactid = ?2", artifactType, artifactId);
		return query
			.range(0, 100)
			.list();
	}
}
