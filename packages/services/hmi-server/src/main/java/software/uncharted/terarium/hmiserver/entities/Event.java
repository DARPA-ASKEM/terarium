package software.uncharted.terarium.hmiserver.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.EventType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@TSModel
@Accessors(chain = true)
@NoArgsConstructor
@Table(indexes = {
	@Index(columnList = "timestampmillis"),
	@Index(columnList = "projectid"),
	@Index(columnList = "username"),
	@Index(columnList = "type"),
	@Index(columnList = "value")
})
@EqualsAndHashCode(callSuper = true)
public class Event extends PanacheEntityBase implements Serializable {
	@Id
	@TSOptional
	private String id = UUID.randomUUID().toString();

	@Column(nullable = false)
	@TSOptional
	private Long timestampMillis = Instant.now().toEpochMilli();

	@TSOptional
	private Long projectId;

	@Column(nullable = false)
	@TSOptional
	private String username;

	@Column(nullable = false)
	private EventType type;

	@Column(length = 2047)
	@TSOptional
	private String value;

	/**
	 * Gets events by type and an option search string for values
	 *
	 * @param type      the {@link EventType}
	 * @param projectId the optional project id
	 * @param username  the username of the current user
	 * @param like      optional search string for values of matching events
	 * @param limit     the maximum number of events to return
	 * @return a list of {@link Event} matching the input
	 */
	public static List<Event> findAllByTypeAndProjectAndUsernameAndLikeLimit(final EventType type, final Long projectId, final String username, String like, int limit) {
		PanacheQuery<Event> query;
		if (like != null && !like.isEmpty()) {
			if (projectId != null) {
				query = find("type = ?1 and projectid = ?2 and username = ?3 and value like ?4", Sort.descending("timestampmillis"), type, projectId, username, "%" + like + "%");
			} else {
				query = find("type = ?1 and username = ?2 and value like ?3", Sort.descending("timestampmillis"), type, username, "%" + like + "%");
			}
		} else {
			if (projectId != null) {
				query = find("type = ?1 and projectid = ?2 and username = ?3", Sort.descending("timestampmillis"), type, projectId, username);
			} else {
				query = find("type = ?1 and username = ?2", Sort.descending("timestampmillis"), type, username);
			}
		}

		return query
			.range(0, limit)
			.list();
	}
}
