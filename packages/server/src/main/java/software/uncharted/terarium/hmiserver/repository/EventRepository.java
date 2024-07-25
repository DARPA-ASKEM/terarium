package software.uncharted.terarium.hmiserver.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.EventType;
import software.uncharted.terarium.hmiserver.models.user.Event;

@Repository
public interface EventRepository extends PSCrudRepository<Event, String> {
	List<Event> findAllByTypeAndProjectIdAndUserIdAndValueLike(
		final EventType type,
		final UUID projectId,
		final String userId,
		final String like,
		Pageable pageable
	);

	List<Event> findAllByTypeAndUserIdAndValueLike(
		final EventType type,
		final String userId,
		final String like,
		Pageable pageable
	);

	List<Event> findAllByTypeAndProjectIdAndUserId(
		final EventType type,
		final UUID projectId,
		final String userId,
		Pageable pageable
	);

	List<Event> findAllByTypeAndUserId(final EventType type, final String userId, Pageable pageable);

	List<Event> findAllByUserId(final String userId);
}
