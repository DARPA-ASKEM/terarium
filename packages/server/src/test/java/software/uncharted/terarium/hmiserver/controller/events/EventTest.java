package software.uncharted.terarium.hmiserver.controller.events;


import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.entities.Event;
import software.uncharted.terarium.hmiserver.models.EventType;
import software.uncharted.terarium.hmiserver.service.EventService;

import java.util.List;

public class EventTest extends TerariumApplicationTests {

	@Autowired
	private EventService eventService;



	@Test
	@Transactional
	public void canEventBeSavedThenRetrieved() {
		Event e = new Event().setType(EventType.TEST_TYPE).setProjectId(1L).setUserId("test").setValue("test");
		Event givenEvent = eventService.save(e);

		Assertions.assertNotNull(givenEvent);

		List<Event> foundEvents = eventService.findEvents(EventType.TEST_TYPE, 1L, "test", "test", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(1, foundEvents.size());
	}

	@Test
	@Transactional
	public void canEventBeSavedThenRetrievedWithByProjectId() {
		Event e1 = new Event().setType(EventType.TEST_TYPE).setProjectId(1L).setUserId("test").setValue("test");
		Event givenEvent = eventService.save(e1);
		Assertions.assertNotNull(givenEvent);

		Event e2 = new Event().setType(EventType.TEST_TYPE).setProjectId(2L).setUserId("test").setValue("test");
		givenEvent = eventService.save(e2);
		Assertions.assertNotNull(givenEvent);

		Event e3 = new Event().setType(EventType.TEST_TYPE).setProjectId(2L).setUserId("test").setValue("test");
		givenEvent = eventService.save(e3);
		Assertions.assertNotNull(givenEvent);

		Event e4 = new Event().setType(EventType.TEST_TYPE).setProjectId(3L).setUserId("test").setValue("test");
		givenEvent = eventService.save(e4);
		Assertions.assertNotNull(givenEvent);

		List<Event> foundEvents = eventService.findEvents(EventType.TEST_TYPE, 2L, "test", "test", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(2, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, 1L, "test", "test", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(1, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, 3L, "test", "test", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(1, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, null, "test", "test", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(4, foundEvents.size());
	}

	@Test
	@Transactional
	public void canEventBeSavedThenRetrievedByLike() {
		Event e1 = new Event().setType(EventType.TEST_TYPE).setProjectId(1L).setUserId("test").setValue("hello");
		Event givenEvent = eventService.save(e1);
		Assertions.assertNotNull(givenEvent);

		Event e2 = new Event().setType(EventType.TEST_TYPE).setProjectId(1L).setUserId("test").setValue("hello");
		givenEvent = eventService.save(e2);
		Assertions.assertNotNull(givenEvent);

		Event e3 = new Event().setType(EventType.TEST_TYPE).setProjectId(1L).setUserId("test").setValue("hello");
		givenEvent = eventService.save(e3);
		Assertions.assertNotNull(givenEvent);

		Event e4 = new Event().setType(EventType.TEST_TYPE).setProjectId(1L).setUserId("test").setValue("world");
		givenEvent = eventService.save(e4);
		Assertions.assertNotNull(givenEvent);

		List<Event> foundEvents = eventService.findEvents(EventType.TEST_TYPE, 1L, "test", "hello", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(3, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, 1L, "test", "world", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(1, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, 1L, "test", "foobar", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(0, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, 1L, "test", null, 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(4, foundEvents.size());
	}

	@Test
	@Transactional
	public void canEventBeSavedThenRetrievedByUserId(){
		Event e1 = new Event().setType(EventType.TEST_TYPE).setProjectId(1L).setUserId("test").setValue("hello");
		Event givenEvent = eventService.save(e1);
		Assertions.assertNotNull(givenEvent);

		Event e2 = new Event().setType(EventType.TEST_TYPE).setProjectId(1L).setUserId("test").setValue("hello");
		givenEvent = eventService.save(e2);
		Assertions.assertNotNull(givenEvent);

		Event e3 = new Event().setType(EventType.TEST_TYPE).setProjectId(1L).setUserId("test").setValue("hello");
		givenEvent = eventService.save(e3);
		Assertions.assertNotNull(givenEvent);

		Event e4 = new Event().setType(EventType.TEST_TYPE).setProjectId(1L).setUserId("test1").setValue("world");
		givenEvent = eventService.save(e4);
		Assertions.assertNotNull(givenEvent);

		List<Event> foundEvents = eventService.findEvents(EventType.TEST_TYPE, 1L, "test", "hello", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(3, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, 1L, "test1", "world", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(1, foundEvents.size());

		foundEvents = eventService.findEvents(EventType.TEST_TYPE, 1L, "test2", "foobar", 100);
		Assertions.assertNotNull(foundEvents);
		Assertions.assertEquals(0, foundEvents.size());

	}

}
