package software.uncharted.terarium.hmiserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.entities.Event;
import software.uncharted.terarium.hmiserver.models.EventType;
import software.uncharted.terarium.hmiserver.repository.EventRepository;

import java.util.List;

@Service
@Slf4j
public class EventService {
	@Autowired
	private EventRepository eventRepository;

	public List<Event> findEvents(final EventType type, final Long projectId, final String currentUserId, final String like, final int limit) {
		final Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestampMillis"));
		if (like != null && !like.isEmpty()) {
			final String likeQuery = "%" + like + "%";
			if (projectId != null) {
				return eventRepository.findAllByTypeAndProjectIdAndUserIdAndValueLike(type, projectId, currentUserId, likeQuery, pageable);
			} else {
				return eventRepository.findAllByTypeAndUserIdAndValueLike(type, currentUserId, likeQuery, pageable);
			}
		} else {
			if (projectId != null) {
				return eventRepository.findAllByTypeAndProjectIdAndUserId(type, projectId, currentUserId, pageable);
			} else {
				return eventRepository.findAllByTypeAndUserId(type, currentUserId, pageable);
			}
		}
	}

	public List<Event> findAllByUserId(final String userId) {
		return eventRepository.findAllByUserId(userId);
	}

	public Event save(final Event e) {
		return eventRepository.save(e);
	}
}
