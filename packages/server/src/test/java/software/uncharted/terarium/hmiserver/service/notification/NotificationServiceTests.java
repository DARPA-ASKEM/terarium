package software.uncharted.terarium.hmiserver.service.notification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.configuration.MockUser;
import software.uncharted.terarium.hmiserver.models.notification.NotificationEvent;
import software.uncharted.terarium.hmiserver.models.notification.NotificationGroup;

@Slf4j
public class NotificationServiceTests extends TerariumApplicationTests {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private NotificationService notificationService;

	JsonNode getData() {
		return mapper.createObjectNode().put("test", "test");
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateNotificationGroup() throws Exception {

		final NotificationGroup group = notificationService
				.createNotificationGroup(new NotificationGroup().setType("test"));

		Assertions.assertNotNull(group.getId());
		Assertions.assertNotNull(group.getTimestamp());
	}

	@Test
	@WithUserDetails(MockUser.URSULA)
	public void testItCanCreateNotificationEvent() throws Exception {

		final NotificationGroup group = notificationService
				.createNotificationGroup(new NotificationGroup().setType("test"));

		notificationService.createNotificationEvent(group, new NotificationEvent().setData(getData()));
		notificationService.createNotificationEvent(group, new NotificationEvent().setData(getData()));
		notificationService.createNotificationEvent(group.getId(), new NotificationEvent().setData(getData()));

		final NotificationGroup after = notificationService.getNotificationGroup(group.getId()).orElseThrow();

		Assertions.assertNotNull(after.getId());
		Assertions.assertNotNull(after.getTimestamp());
		Assertions.assertNotNull(after.getNotificationEvents());
		Assertions.assertEquals(3, after.getNotificationEvents().size());
	}

}
