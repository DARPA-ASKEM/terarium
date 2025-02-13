package software.uncharted.terarium.hmiserver.service.notification;

import java.sql.Timestamp;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.ClientEventType;
import software.uncharted.terarium.hmiserver.models.StatusUpdate;
import software.uncharted.terarium.hmiserver.models.dataservice.simulation.ProgressState;
import software.uncharted.terarium.hmiserver.models.notification.NotificationEvent;
import software.uncharted.terarium.hmiserver.models.notification.NotificationGroup;
import software.uncharted.terarium.hmiserver.service.ClientEventService;

@Slf4j
public class NotificationGroupInstance<T> {

	private final Double halfTimeSeconds;
	private final Double startSeconds;
	private final ClientEventService clientEventService;
	private final NotificationService notificationService;
	private final NotificationGroup notificationGroup;

	private final UUID projectId;

	private final ClientEventType type;

	private final T data;

	public static final Double DEFAULT_HALF_TIME_SECONDS = 2.0;
	public static final String DEFAULT_USER_ID = null;

	public NotificationGroupInstance(
		final ClientEventService clientEventService,
		final NotificationService notificationService,
		final ClientEventType type,
		final UUID projectId,
		final T data,
		final Double halfTimeSeconds,
		final UUID notificationGroupId,
		final String userId
	) {
		this.clientEventService = clientEventService;
		this.notificationService = notificationService;
		this.halfTimeSeconds = halfTimeSeconds;
		this.startSeconds = System.currentTimeMillis() / 1000.0;
		this.projectId = projectId;
		this.type = type;
		this.data = data;

		// Create a new notification group
		final NotificationGroup newNotificationGroup =
			((NotificationGroup) new NotificationGroup().setId(notificationGroupId));
		newNotificationGroup.setType(type.name()).setProjectId(projectId);

		if (userId == null || userId.isEmpty()) {
			newNotificationGroup.setUserId("anonymous");
		} else {
			newNotificationGroup.setUserId(userId);
		}

		this.notificationGroup = notificationService.createNotificationGroup(newNotificationGroup);
	}

	// Version of the constructor that generates a random notification group ID
	public NotificationGroupInstance(
		final ClientEventService clientEventService,
		final NotificationService notificationService,
		final ClientEventType type,
		final UUID projectId,
		final T data,
		final String userId
	) {
		this(
			clientEventService,
			notificationService,
			type,
			projectId,
			data,
			DEFAULT_HALF_TIME_SECONDS,
			UUID.randomUUID(),
			userId
		);
	}

	private Double estimateT() {
		// Make sure this value never reaches 1.0 since 1.0 is reserved for the final message
		return Math.max(0.9 - Math.pow(0.5, (getElapsedSeconds() / halfTimeSeconds)), 0);
	}

	private Double getElapsedSeconds() {
		return (System.currentTimeMillis() / 1000.0) - startSeconds;
	}

	private void sendNotification(final String message, final String error, final Double t, final ProgressState state) {
		final StatusUpdate<T> statusUpdate = StatusUpdate.<T>builder()
			.progress(t)
			.message(message)
			.error(error)
			.state(state)
			.data(data)
			.build();

		// produce the event
		final ClientEvent<StatusUpdate<T>> event = ClientEvent.<StatusUpdate<T>>builder()
			.type(type)
			.projectId(projectId)
			.notificationGroupId(notificationGroup.getId())
			.data(statusUpdate)
			.build();

		// generate the notification event
		final NotificationEvent notification = new NotificationEvent();
		notification.setId(event.getId());
		notification.setProgress(t);
		notification.setCreatedOn(new Timestamp(event.getCreatedAtMs()));

		try {
			notification.setData(event.getData());
		} catch (final Exception e) {
			log.error("Failed to serialize notification data", e);
		}

		// save the notification event
		notificationService.createNotificationEvent(notificationGroup.getId(), notification);

		// send the update to the user
		clientEventService.sendToUser(event, notificationGroup.getUserId());
	}

	public void sendMessage(final String msg) {
		sendNotification(msg, null, estimateT(), ProgressState.RUNNING);
	}

	public void sendMessage(final String msg, final ProgressState state) {
		sendNotification(msg, null, estimateT(), state);
	}

	public void sendFinalMessage(final String msg) {
		sendNotification(msg, null, 1.0, ProgressState.COMPLETE);
	}

	public void sendFinalMessage(final String msg, final ProgressState state) {
		if (state.equals(ProgressState.ERROR) || state.equals(ProgressState.FAILED)) {
			sendNotification(null, msg, 1.0, state);
		} else {
			sendNotification(msg, null, 1.0, state);
		}
	}

	public void sendError(final String msg) {
		sendNotification(null, msg, estimateT(), ProgressState.ERROR);
	}
}
