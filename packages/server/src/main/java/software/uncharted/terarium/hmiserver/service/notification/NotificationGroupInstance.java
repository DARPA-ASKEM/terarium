package software.uncharted.terarium.hmiserver.service.notification;

import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.models.ClientEvent;
import software.uncharted.terarium.hmiserver.models.notification.NotificationEvent;
import software.uncharted.terarium.hmiserver.models.notification.NotificationGroup;
import software.uncharted.terarium.hmiserver.service.ClientEventService;

@Slf4j
public abstract class NotificationGroupInstance<T> {

	private Double halfTimeSeconds = 2.0;
	private Double startSeconds = 0.0;
	private final ClientEventService clientEventService;
	private final NotificationService notificationService;
	private final NotificationGroup notificationGroup;

	public static final Double DEFAULT_HALF_TIME_SECONDS = 2.0;

	public NotificationGroupInstance(
			final ClientEventService clientEventService,
			final NotificationService notificationService,
			final String type,
			final Double halfTimeSeconds) {
		this.clientEventService = clientEventService;
		this.notificationService = notificationService;
		this.halfTimeSeconds = halfTimeSeconds;
		this.startSeconds = System.currentTimeMillis() / 1000.0;

		// create the notification group
		this.notificationGroup = notificationService.createNotificationGroup(new NotificationGroup().setType(type));
	}

	public NotificationGroupInstance(
			final ClientEventService clientEventService,
			final NotificationService notificationService,
			final String type) {
		this(clientEventService, notificationService, type, DEFAULT_HALF_TIME_SECONDS);
	}

	public abstract ClientEvent<T> produceClientEvent(final Double t, final String message, final String error);

	private Double estimateT() {
		return 1.0f - Math.pow(0.5, (getElapsedSeconds() / halfTimeSeconds));
	}

	private Double getElapsedSeconds() {
		return (System.currentTimeMillis() / 1000.0) - startSeconds;
	}

	private void sendNotification(final String message, final String error, final Double t) {
		// produce the event
		final ClientEvent<T> event = produceClientEvent(t, message, error);

		// generate the notification event
		final NotificationEvent notification = new NotificationEvent();
		notification.setId(event.getId());
		notification.setProgress(t);
		notification.setTimestamp(new Timestamp(event.getCreatedAtMs()));

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
		sendNotification(msg, null, estimateT());
	}

	public void sendFinalMessage(final String msg) {
		sendNotification(msg, null, 1.0);
	}

	public void sendError(final String msg) {
		sendNotification(null, msg, estimateT());
	}

}
