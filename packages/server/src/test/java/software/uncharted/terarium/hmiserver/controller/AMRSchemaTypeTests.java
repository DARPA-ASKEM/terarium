package software.uncharted.terarium.hmiserver.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import software.uncharted.terarium.hmiserver.TerariumApplicationTests;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;

class AMRSchemaTypeTest extends TerariumApplicationTests {

	@AMRSchemaType
	class TestClass {
	}

	@Test
	void testGetAdditionalPropertiesExists() {
		// Arrange
		Class<?> clazz = TestClass.class;

		// Act
		Method method;
		try {
			method = clazz.getMethod("getAdditionalProperties");
		} catch (NoSuchMethodException e) {
			method = null;
		}

		// Assert
		assertNotNull(method, "getAdditionalProperties method should exist in TestClass");
	}
}
