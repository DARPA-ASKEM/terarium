package software.uncharted.terarium.hmiserver.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.EventType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class Event extends PanacheEntityBase implements Serializable {
	@Id
	private String id = UUID.randomUUID().toString();

	@Column(nullable = false)
	private Long timestampMillis = Instant.now().toEpochMilli();

	@Column(nullable = false)
	private Long projectId;

	@Column(nullable = false)
	private String username;

	@Column(nullable = false)
	private EventType type;

	@Column(length = 2047)
	private String value;

	/**
	 * Gets events by type and an option search string for values
	 * @param type				the {@link EventType}
	 * @param projectId		the project id
	 * @param username		the username of the current user
	 * @param like				optional search string for values of matching events
	 * @param limit				the maximum number of events to return
	 * @return						a list of {@link Event} matching the input
	 */
	public static List<Event> findAllByTypeAndProjectAndUsernameAndLikeLimit(final EventType type, final Long projectId, final String username, String like, int limit) {
		PanacheQuery<Event> query;
		if (like != null && !like.isEmpty()) {
			query = find("type = ?1 and projectid = ?2 and username = ?3 and value like ?4", Sort.descending("timestampmillis"), type, projectId, username, "%" + like + "%");
		} else {
			query = find("type = ?1 and projectid = ?2 and username = ?3", Sort.descending("timestampmillis"), type, projectId, username);
		}

		return query
			.range(0, limit)
			.list();
	}
}
