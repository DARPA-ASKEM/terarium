package software.uncharted.terarium.hmiserver.services;

import software.uncharted.terarium.hmiserver.entities.Event;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;

@ApplicationScoped
public class EventService {
	@Transactional
	public void persistEvent(final Event event) {
		event.persist();
	}
}
