package software.uncharted.terarium.hmiserver.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.entities.Event;
import software.uncharted.terarium.hmiserver.models.EventType;

import java.util.List;

@Repository
public interface EventRepository extends PSCrudRepository<Event, String> {
	List<Event> findAllByTypeAndProjectIdAndUsernameAndValueLike(final EventType type, final Long projectId, final String username, final String like, Pageable pageable);
	List<Event> findAllByTypeAndUsernameAndValueLike(final EventType type, final String username, final String like, Pageable pageable);
	List<Event> findAllByTypeAndProjectIdAndUsername(final EventType type, final Long projectId, final String username, Pageable pageable);
	List<Event> findAllByTypeAndUsername(final EventType type, final String username, Pageable pageable);;
}
