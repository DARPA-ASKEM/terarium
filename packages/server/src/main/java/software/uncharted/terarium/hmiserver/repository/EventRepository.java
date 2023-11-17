package software.uncharted.terarium.hmiserver.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.entities.Event;
import software.uncharted.terarium.hmiserver.models.EventType;

import java.util.List;

@Repository
public interface EventRepository extends PSCrudRepository<Event, String> {
	List<Event> findAllByTypeAndProjectIdAndUserIdAndValueLike(final EventType type, final Long projectId, final String userId, final String like, Pageable pageable);
	List<Event> findAllByTypeAndUserIdAndValueLike(final EventType type, final String userId, final String like, Pageable pageable);
	List<Event> findAllByTypeAndProjectIdAndUserId(final EventType type, final Long projectId, final String userId, Pageable pageable);
	List<Event> findAllByTypeAndUserId(final EventType type, final String userId, Pageable pageable);
	List<Event> findAllByUserId(final String userId);

}
