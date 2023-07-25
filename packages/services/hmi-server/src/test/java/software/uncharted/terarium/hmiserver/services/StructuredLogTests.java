package software.uncharted.terarium.hmiserver.services;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

@QuarkusTest
public class StructuredLogTests {
	@Inject
	StructuredLog structuredLog;


	public void testItThrowsWhenOddNumberOfArguments() {
		Assertions.assertThrows(RuntimeException.class, () -> structuredLog.log(StructuredLog.Type.EVENT, "adam", "single_element"));
	}


	public void testItDoesntThrowWhenNoArguments() {
		Assertions.assertDoesNotThrow(() -> structuredLog.log(StructuredLog.Type.EVENT, "adam"));
	}


	public void testItAllowsNullUser() {
		Assertions.assertDoesNotThrow(() -> structuredLog.log(StructuredLog.Type.EVENT, null, "foo", "bar"));
	}
}
