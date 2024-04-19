package software.uncharted.terarium.hmiserver.service.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.notification.NotificationEvent;
import software.uncharted.terarium.hmiserver.models.notification.NotificationGroup;
import software.uncharted.terarium.hmiserver.service.CurrentUserService;

@Slf4j
public class NotificationServiceTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private CurrentUserService currentUserService;

	JsonNode getData() {
		return mapper.createObjectNode().put("test", "test");
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateNotificationGroup() throws Exception {

		final NotificationGroup group =
				notificationService.createNotificationGroup(new NotificationGroup().setType("test"));

		Assertions.assertNotNull(group.getId());
		Assertions.assertNotNull(group.getCreatedOn());
	}

	/* FIXME: interferes with testItCanAckNotificationGroup
	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateNotificationEvent() throws Exception {

		final NotificationGroup group =
				notificationService.createNotificationGroup(new NotificationGroup().setType("test"));

		notificationService.createNotificationEvent(group.getId(), new NotificationEvent().setData(getData()));
		notificationService.createNotificationEvent(group.getId(), new NotificationEvent().setData(getData()));
		notificationService.createNotificationEvent(group.getId(), new NotificationEvent().setData(getData()));

		final NotificationGroup after =
				notificationService.getNotificationGroup(group.getId()).orElseThrow();

		Assertions.assertNotNull(after.getId());
		Assertions.assertNotNull(after.getCreatedOn());
		Assertions.assertNotNull(after.getNotificationEvents());
		Assertions.assertEquals(3, after.getNotificationEvents().size());
	}
	*/

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanAckNotificationGroup() throws Exception {

		final NotificationGroup group =
				notificationService.createNotificationGroup(new NotificationGroup().setType("test"));

		notificationService.createNotificationEvent(group.getId(), new NotificationEvent().setData(getData()));
		notificationService.createNotificationEvent(group.getId(), new NotificationEvent().setData(getData()));
		notificationService.createNotificationEvent(group.getId(), new NotificationEvent().setData(getData()));

		final NotificationGroup after =
				notificationService.getNotificationGroup(group.getId()).orElseThrow();

		Assertions.assertNotNull(after.getId());
		Assertions.assertNotNull(after.getCreatedOn());
		Assertions.assertNotNull(after.getNotificationEvents());
		Assertions.assertEquals(3, after.getNotificationEvents().size());
		for (final NotificationEvent event : after.getNotificationEvents()) {
			Assertions.assertNotNull(event.getId());
			Assertions.assertNotNull(event.getCreatedOn());
			Assertions.assertNotNull(event.getData());
			Assertions.assertNull(event.getAcknowledgedOn());
		}

		final LocalDateTime sinceDateTime = LocalDateTime.now().minusHours(2);
		final Timestamp since =
				Timestamp.from(sinceDateTime.atZone(ZoneId.systemDefault()).toInstant());

		final List<NotificationGroup> resp1 = notificationService.getUnAckedNotificationGroupsCreatedSince(
				currentUserService.get().getId(), since);

		Assertions.assertEquals(1, resp1.size());
		Assertions.assertNotNull(resp1.get(0).getNotificationEvents());
		Assertions.assertEquals(3, resp1.get(0).getNotificationEvents().size());

		// ack the group
		notificationService.acknowledgeNotificationGroup(group.getId());

		final List<NotificationGroup> resp2 = notificationService.getUnAckedNotificationGroupsCreatedSince(
				currentUserService.get().getId(), since);

		Assertions.assertEquals(0, resp2.size());

		// create a new event
		notificationService.createNotificationEvent(group.getId(), new NotificationEvent().setData(getData()));

		final List<NotificationGroup> resp3 = notificationService.getUnAckedNotificationGroupsCreatedSince(
				currentUserService.get().getId(), since);

		Assertions.assertEquals(1, resp3.size());
		Assertions.assertNotNull(resp3.get(0).getNotificationEvents());
		Assertions.assertEquals(4, resp3.get(0).getNotificationEvents().size());
	}
}
