package software.uncharted.terarium.hmiserver.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import lombok.Data;
import software.uncharted.terarium.hmiserver.TerariumApplicationTests;

public class AMRPropertyNamingStrategyTests extends TerariumApplicationTests {

	@AMRSchemaType
	@Data
	static class MyAMRType {
		@JsonProperty("my_field")
		String myField;

		@JsonProperty("myOtherField")
		String my_other_field;

		String as_is_snake;

		String asIsCamel;
	}

	@Test
	void testAMRPropertyNamingStrategy() throws Exception {

		ObjectMapper mapper = new ObjectMapper()
				.setPropertyNamingStrategy(
						new AMRPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy()));

		String jsonBefore = "{\"my_field\":\"valueA\",\"myOtherField\":\"123\",\"as_is_snake\":\"value\",\"asIsCamel\": \"456\"}";

		// deserialize it
		MyAMRType testClass = mapper.readValue(jsonBefore, MyAMRType.class);

		Assertions.assertEquals(testClass.getMyField(), "valueA");
		Assertions.assertEquals(testClass.getMy_other_field(), "123");
		Assertions.assertEquals(testClass.getAs_is_snake(), "value");
		Assertions.assertEquals(testClass.getAsIsCamel(), "456");

		// serialize it
		String jsonAfter = mapper.writeValueAsString(testClass);

		JsonNode tree1 = mapper.readTree(jsonBefore);
		JsonNode tree2 = mapper.readTree(jsonAfter);

		// Assert
		assertEquals(tree1, tree2, "The two JSON strings should be equal");
	}
}
