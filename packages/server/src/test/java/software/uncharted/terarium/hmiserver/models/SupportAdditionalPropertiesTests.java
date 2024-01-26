package software.uncharted.terarium.hmiserver.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;

public class SupportAdditionalPropertiesTests extends TerariumApplicationTests {

	@Autowired
	ObjectMapper mapper;

	@Data
	static class TestClass implements SupportAdditionalProperties {
		private String myFieldA;
		private Integer myFieldB;
	}

	@Test
	void testGetAdditionalProperties() throws Exception {

		String jsonBefore = "{\"myFieldA\":\"valueA\",\"myFieldB\":123,\"myAdditionalField1\":\"value\",\"myAdditionalField2\":456}";

		// deserialize it
		TestClass testClass = mapper.readValue(jsonBefore, TestClass.class);

		Assertions.assertEquals(testClass.getMyFieldA(), "valueA");
		Assertions.assertEquals(testClass.getMyFieldB(), 123);
		Assertions.assertEquals(testClass.getAdditionalProperties().get("myAdditionalField1"), "value");
		Assertions.assertEquals(testClass.getAdditionalProperties().get("myAdditionalField2"), 456);

		// serialize it
		String jsonAfter = mapper.writeValueAsString(testClass);

		JsonNode tree1 = mapper.readTree(jsonBefore);
		JsonNode tree2 = mapper.readTree(jsonAfter);

		// Assert
		assertEquals(tree1, tree2, "The two JSON strings should be equal");
	}
}
