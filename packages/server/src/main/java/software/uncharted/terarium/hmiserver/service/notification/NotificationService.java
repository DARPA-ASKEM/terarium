package software.uncharted.terarium.hmiserver.service.notification;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.User;
import software.uncharted.terarium.hmiserver.models.notification.NotificationEvent;
import software.uncharted.terarium.hmiserver.models.notification.NotificationGroup;
import software.uncharted.terarium.hmiserver.repository.notification.NotificationEventRepository;
import software.uncharted.terarium.hmiserver.repository.notification.NotificationGroupRepository;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;

@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationService {

	final NotificationGroupRepository notificationGroupRepository;
	final NotificationEventRepository notificationEventRepository;
	final CurrentUserService currentUserService;

	public List<NotificationGroup> getNotificationGroupsForUser(final String userId) {
		return notificationGroupRepository.findAllByUserIdOrderByCreatedOnDesc(userId);
	}

	public List<NotificationGroup> getUnAckedNotificationGroupsCreatedSince(final String userId, final Timestamp since) {
		return notificationGroupRepository.findAllUnackedByUserReturnAllEvents(userId, since);
	}

	public List<NotificationGroup> getNotificationGroupsCreatedSince(final String userId, final Timestamp since) {
		return notificationGroupRepository.findAllByUserIdAndCreatedOnGreaterThanOrderByCreatedOnDesc(userId, since);
	}

	public Optional<NotificationGroup> getNotificationGroup(final UUID id) {
		return notificationGroupRepository.findById(id);
	}

	public NotificationGroup createNotificationGroup(final NotificationGroup notificationGroup) {
		if (notificationGroup.getUserId() == null || notificationGroup.getUserId().isEmpty()) {
			try {
				final User user = currentUserService.get();
				notificationGroup.setUserId(user != null ? user.getId() : "anonymous");
			} catch (final Exception e) {
				log.error("No userId set on notification group, unabled to acquire from thread");
			}
		}
		return notificationGroupRepository.save(notificationGroup);
	}

	public NotificationEvent createNotificationEvent(final UUID groupId, final NotificationEvent notificationEvent) {
		final NotificationGroup group = notificationGroupRepository.findById(groupId).orElseThrow();

		notificationEvent.setNotificationGroup(group);

		group.getNotificationEvents().add(notificationEvent);

		notificationGroupRepository.save(group);

		return notificationEvent;
	}

	public void acknowledgeNotificationGroup(final UUID groupId) {
		notificationEventRepository.setAcknowledgedOnWhereNotificationGroupIdEquals(groupId, Timestamp.from(Instant.now()));
	}

	public Optional<NotificationGroup> delete(final UUID id) {
		final Optional<NotificationGroup> notificationGroup = getNotificationGroup(id);
		if (notificationGroup.isEmpty()) {
			return Optional.empty();
		}
		notificationGroupRepository.delete(notificationGroup.get());
		return notificationGroup;
	}
}
