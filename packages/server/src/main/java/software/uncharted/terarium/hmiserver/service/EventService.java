package software.uncharted.terarium.hmiserver.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.EventType;
import software.uncharted.terarium.hmiserver.models.user.Event;
import software.uncharted.terarium.hmiserver.repository.EventRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {

	final EventRepository eventRepository;

	public List<Event> findEvents(
		final EventType type,
		final UUID projectId,
		final String currentUserId,
		final String like,
		final int limit
	) {
		final Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestampMillis"));
		if (like != null && !like.isEmpty()) {
			final String likeQuery = "%" + like + "%";
			if (projectId != null) {
				return eventRepository.findAllByTypeAndProjectIdAndUserIdAndValueLike(
					type,
					projectId,
					currentUserId,
					likeQuery,
					pageable
				);
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
