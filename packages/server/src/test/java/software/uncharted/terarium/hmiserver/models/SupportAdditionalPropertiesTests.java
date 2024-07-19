package software.uncharted.terarium.hmiserver.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;

public class SupportAdditionalPropertiesTests extends TerariumApplicationTests {

	@Data
	@EqualsAndHashCode(callSuper = true)
	static class MyTypeNested extends SupportAdditionalProperties {

		@JsonProperty("my_field")
		String myField;
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	static class MyType extends SupportAdditionalProperties {

		MyTypeNested nested;

		@JsonProperty("my_field")
		String myField;

		@JsonProperty("myOtherField")
		String my_other_field;
	}

	@Test
	void testSupportAdditionalProperties() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final String jsonBefore =
			"{\"nested\": null, \"my_field\":\"valueA\", \"myField\":\"valueA\", \"myOtherField\":\"123\",\"as_is_snake\":\"value\",\"asIsCamel\": \"456\"}";
		// deserialize it
		final MyType testClass = mapper.readValue(jsonBefore, MyType.class);
		Assertions.assertEquals(testClass.getMyField(), "valueA");
		Assertions.assertEquals(testClass.getMy_other_field(), "123");
		// serialize it
		final String jsonAfter = mapper.writeValueAsString(testClass);
		final JsonNode tree1 = mapper.readTree(jsonBefore);
		final JsonNode tree2 = mapper.readTree(jsonAfter);
		// Assert
		assertEquals(tree1, tree2, "The two JSON strings should be equal");
	}

	@Test
	void testSupportAdditionalPropertiesWithNested() throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		final String jsonBefore =
			"{\"nested\":{ \"my_field\":\"valueA\", \"nestedField\":\"valueA\" }, \"my_field\":\"valueA\", \"myField\":\"valueA\", \"myOtherField\":\"123\",\"as_is_snake\":\"value\",\"asIsCamel\": \"456\"}";
		// deserialize it
		final MyType testClass = mapper.readValue(jsonBefore, MyType.class);
		Assertions.assertEquals(testClass.getMyField(), "valueA");
		Assertions.assertEquals(testClass.getMy_other_field(), "123");
		// serialize it
		final String jsonAfter = mapper.writeValueAsString(testClass);
		final JsonNode tree1 = mapper.readTree(jsonBefore);
		final JsonNode tree2 = mapper.readTree(jsonAfter);
		// Assert
		assertEquals(tree1, tree2, "The two JSON strings should be equal");
	}
}
