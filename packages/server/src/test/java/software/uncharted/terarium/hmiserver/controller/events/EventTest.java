package software.uncharted.terarium.hmiserver.controller.events;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.models.EventType;
import software.uncharted.terarium.hmiserver.models.user.Event;
import software.uncharted.terarium.hmiserver.service.EventService;

public class EventTest extends TerariumApplicationTests {

	@Autowired
	private EventService eventService;

	@Test
	public void canEventBeSavedThenRetrieved() {
		final UUID projectId = UUID.randomUUID();

		final Event e = new Event().setType(EventType.TEST_TYPE).setProjectId(projectId).setUserId("test").setValue("test");
		final Event givenEvent = eventService.save(e);

		Assertions.assertNotNull(givenEvent);

		final List<Event> foundEvents = eventService.findEvents(EventType.TEST_TYPE, projectId, "test", "test", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(1, foundEvents.size());
	}

	@Test
	public void canEventBeSavedThenRetrievedWithByProjectId() {
		UUID p1 = UUID.randomUUID();
		UUID p2 = UUID.randomUUID();
		UUID p3 = UUID.randomUUID();

		final Event e1 = new Event().setType(EventType.TEST_TYPE).setProjectId(p1).setUserId("test").setValue("test");
		Event givenEvent = eventService.save(e1);
		Assertions.assertNotNull(givenEvent);

		final Event e2 = new Event().setType(EventType.TEST_TYPE).setProjectId(p2).setUserId("test").setValue("test");
		givenEvent = eventService.save(e2);
		Assertions.assertNotNull(givenEvent);

		final Event e3 = new Event().setType(EventType.TEST_TYPE).setProjectId(p2).setUserId("test").setValue("test");
		givenEvent = eventService.save(e3);
		Assertions.assertNotNull(givenEvent);

		final Event e4 = new Event().setType(EventType.TEST_TYPE).setProjectId(p3).setUserId("test").setValue("test");
		givenEvent = eventService.save(e4);
		Assertions.assertNotNull(givenEvent);

		List<Event> foundEvents = eventService.findEvents(EventType.TEST_TYPE, p2, "test", "test", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(2, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, p1, "test", "test", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(1, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, p3, "test", "test", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(1, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, null, "test", "test", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(4, foundEvents.size());
	}

	@Test
	public void canEventBeSavedThenRetrievedByLike() {
		UUID p1 = UUID.randomUUID();

		final Event e1 = new Event().setType(EventType.TEST_TYPE).setProjectId(p1).setUserId("test").setValue("hello");
		Event givenEvent = eventService.save(e1);
		Assertions.assertNotNull(givenEvent);

		final Event e2 = new Event().setType(EventType.TEST_TYPE).setProjectId(p1).setUserId("test").setValue("hello");
		givenEvent = eventService.save(e2);
		Assertions.assertNotNull(givenEvent);

		final Event e3 = new Event().setType(EventType.TEST_TYPE).setProjectId(p1).setUserId("test").setValue("hello");
		givenEvent = eventService.save(e3);
		Assertions.assertNotNull(givenEvent);

		final Event e4 = new Event().setType(EventType.TEST_TYPE).setProjectId(p1).setUserId("test").setValue("world");
		givenEvent = eventService.save(e4);
		Assertions.assertNotNull(givenEvent);

		List<Event> foundEvents = eventService.findEvents(EventType.TEST_TYPE, p1, "test", "hello", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(3, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, p1, "test", "world", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(1, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, p1, "test", "foobar", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(0, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, p1, "test", null, 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(4, foundEvents.size());
	}

	@Test
	public void canEventBeSavedThenRetrievedByUserId() {
		UUID p1 = UUID.randomUUID();

		final Event e1 = new Event().setType(EventType.TEST_TYPE).setProjectId(p1).setUserId("test").setValue("hello");
		Event givenEvent = eventService.save(e1);
		Assertions.assertNotNull(givenEvent);

		final Event e2 = new Event().setType(EventType.TEST_TYPE).setProjectId(p1).setUserId("test").setValue("hello");
		givenEvent = eventService.save(e2);
		Assertions.assertNotNull(givenEvent);

		final Event e3 = new Event().setType(EventType.TEST_TYPE).setProjectId(p1).setUserId("test").setValue("hello");
		givenEvent = eventService.save(e3);
		Assertions.assertNotNull(givenEvent);

		final Event e4 = new Event().setType(EventType.TEST_TYPE).setProjectId(p1).setUserId("test1").setValue("world");
		givenEvent = eventService.save(e4);
		Assertions.assertNotNull(givenEvent);

		List<Event> foundEvents = eventService.findEvents(EventType.TEST_TYPE, p1, "test", "hello", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(3, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, p1, "test1", "world", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(1, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, p1, "test2", "foobar", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(0, foundEvents.size());
	}
}
