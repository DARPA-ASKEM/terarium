package software.uncharted.terarium.hmiserver.service.notification;

import io.micrometer.observation.annotation.Observed;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.notification.NotificationEvent;
import software.uncharted.terarium.hmiserver.models.notification.NotificationGroup;
import software.uncharted.terarium.hmiserver.repository.notification.NotificationEventRepository;
import software.uncharted.terarium.hmiserver.repository.notification.NotificationGroupRepository;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationService {

	final NotificationGroupRepository notificationGroupRepository;
	final NotificationEventRepository notificationEventRepository;

	@Observed(name = "function_profile")
	public List<NotificationGroup> getNotificationGroupsForUser(final String userId) {
		return notificationGroupRepository.findAllByUserIdOrderByCreatedOnDesc(userId);
	}

	@Observed(name = "function_profile")
	public List<NotificationGroup> getUnAckedNotificationGroupsCreatedSince(final String userId, final Timestamp since) {
		return notificationGroupRepository.findAllUnackedByUserReturnAllEvents(userId, since);
	}

	@Observed(name = "function_profile")
	public List<NotificationGroup> getUnAckedNotificationGroupsCreatedSinceLatestEventOnly(
		final String userId,
		final Timestamp since
	) {
		final List<NotificationGroup> groups = notificationGroupRepository.findAllUnackedByUserReturnAllEvents(
			userId,
			since
		);
		for (final NotificationGroup group : groups) {
			final List<NotificationEvent> events = group.getNotificationEvents();
			if (!events.isEmpty()) {
				NotificationEvent latestEvent = events.get(0);
				for (final NotificationEvent event : events) {
					if (event.getCreatedOn().after(latestEvent.getCreatedOn())) {
						latestEvent = event;
					}
				}
				final List<NotificationEvent> latestEventList = new ArrayList<>();
				latestEventList.add(latestEvent);
				group.setNotificationEvents(latestEventList);
			}
		}
		return groups;
	}

	@Observed(name = "function_profile")
	public List<NotificationGroup> getNotificationGroupsCreatedSince(final String userId, final Timestamp since) {
		return notificationGroupRepository.findAllByUserIdAndCreatedOnGreaterThanOrderByCreatedOnDesc(userId, since);
	}

	@Observed(name = "function_profile")
	public List<NotificationGroup> getNotificationGroupsCreatedSinceLatestEventOnly(
		final String userId,
		final Timestamp since
	) {
		final List<NotificationGroup> groups =
			notificationGroupRepository.findAllByUserIdAndCreatedOnGreaterThanOrderByCreatedOnDesc(userId, since);
		for (final NotificationGroup group : groups) {
			final List<NotificationEvent> events = group.getNotificationEvents();
			if (!events.isEmpty()) {
				NotificationEvent latestEvent = events.get(0);
				for (final NotificationEvent event : events) {
					if (event.getCreatedOn().after(latestEvent.getCreatedOn())) {
						latestEvent = event;
					}
				}
				final List<NotificationEvent> latestEventList = new ArrayList<>();
				latestEventList.add(latestEvent);
				group.setNotificationEvents(latestEventList);
			}
		}
		return groups;
	}

	@Observed(name = "function_profile")
	public Optional<NotificationGroup> getNotificationGroup(final UUID id) {
		return notificationGroupRepository.findById(id);
	}

	@Observed(name = "function_profile")
	public NotificationGroup createNotificationGroup(final NotificationGroup notificationGroup) {
		return notificationGroupRepository.save(notificationGroup);
	}

	@Observed(name = "function_profile")
	public void createNotificationEvent(final UUID groupId, final NotificationEvent notificationEvent) {
		final NotificationGroup group = notificationGroupRepository.findById(groupId).orElseThrow();
		notificationEvent.setNotificationGroup(group);
		group.getNotificationEvents().add(notificationEvent);
		notificationGroupRepository.save(group);
	}

	@Observed(name = "function_profile")
	public void acknowledgeNotificationGroup(final UUID groupId) {
		notificationEventRepository.setAcknowledgedOnWhereNotificationGroupIdEquals(groupId, Timestamp.from(Instant.now()));
	}

	@Observed(name = "function_profile")
	public Optional<NotificationGroup> delete(final UUID id) {
		final Optional<NotificationGroup> notificationGroup = getNotificationGroup(id);
		if (notificationGroup.isEmpty()) {
			return Optional.empty();
		}
		notificationGroupRepository.delete(notificationGroup.get());
		return notificationGroup;
	}
}
