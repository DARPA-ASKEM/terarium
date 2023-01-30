package software.uncharted.terarium.hmiserver.entities;


import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
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
 * Definition for use rannotations. An annotation can be applied to TERARium artiacts (e.g. document, model, dataset ...) as additional
 * user-generated metadata. Note to uniquely identify an artifact in TERARium we need a composite key (artifactType, artifactId).
 *
 **/

@Entity
@Data
@Accessors(chain = true)
@NoArgsConstructor
@Table(indexes = {
	@Index(columnList = "artifactType, artifactId")
})
public class Annotation extends PanacheEntityBase implements Serializable {
	@Id
	private String id = UUID.randomUUID().toString();

	@Column(nullable = false)
	private Long timestampMillis = Instant.now().toEpochMilli();

	@Column(nullable = true)
	private Long projectId;

	@Column(columnDefinition="TEXT")
	private String content;

	@Column(nullable = false)
	private String username;

	@JsonbProperty("artifact_id")
	private String artifactId;

	@JsonbProperty("artifact_type")
	private String artifactType;

  /**
	 * Find annotations by artifactType and artifactId
	 */
	public static List<Annotation> findByArtifact(String artifactType, String artifactId) {
		PanacheQuery<Annotation> query;

		query = find("artifacttype = ?1 and artifactid = ?2");
		return query
			.range(0, 100)
			.list();
	}
}
