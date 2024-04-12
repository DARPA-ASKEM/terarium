package software.uncharted.terarium.hmiserver.service.notification;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

	public List<NotificationGroup> getNotificationGroupsForUser(final String userId) {
		return notificationGroupRepository.findAllByUserIdOrderByTimestampDesc(userId);
	}

	public List<NotificationGroup> getNotificationGroupsCreatedSince(final String userId, final Timestamp since) {
		return notificationGroupRepository.findAllByUserIdAndTimestampGreaterThanOrderByTimestampDesc(userId, since);
	}

	public Optional<NotificationGroup> getNotificationGroup(final UUID id) {
		return notificationGroupRepository.findById(id);
	}

	public NotificationGroup createNotificationGroup(final NotificationGroup notificationGroup) {
		return notificationGroupRepository.save(notificationGroup);
	}

	public NotificationEvent createNotificationEvent(final NotificationEvent notificationEvent) {
		return notificationEventRepository.save(notificationEvent);
	}

	// public Optional<NotificationGroup> updateNotificationGroup(final
	// NotificationGroup notificationGroup) {
	// if (!notificationGroupRepository.existsById(notificationGroup.getId())) {
	// return Optional.empty();
	// }
	// return Optional.of(notificationGroupRepository.save(notificationGroup));
	// }

	public Optional<NotificationGroup> delete(final UUID id) {
		final Optional<NotificationGroup> notificationGroup = getNotificationGroup(id);
		if (notificationGroup.isEmpty()) {
			return Optional.empty();
		}
		notificationGroupRepository.delete(notificationGroup.get());
		return notificationGroup;
	}
}
