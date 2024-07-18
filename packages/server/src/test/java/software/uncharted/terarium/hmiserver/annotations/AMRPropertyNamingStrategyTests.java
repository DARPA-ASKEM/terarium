package software.uncharted.terarium.hmiserver.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
		final ObjectMapper mapper = new ObjectMapper()
			.setPropertyNamingStrategy(new AMRPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy()));

		final String jsonBefore =
			"{\"my_field\":\"valueA\",\"myOtherField\":\"123\",\"as_is_snake\":\"value\",\"asIsCamel\": \"456\"}";

		// deserialize it
		final MyAMRType testClass = mapper.readValue(jsonBefore, MyAMRType.class);

		Assertions.assertEquals(testClass.getMyField(), "valueA");
		Assertions.assertEquals(testClass.getMy_other_field(), "123");
		Assertions.assertEquals(testClass.getAs_is_snake(), "value");
		Assertions.assertEquals(testClass.getAsIsCamel(), "456");

		// serialize it
		final String jsonAfter = mapper.writeValueAsString(testClass);

		final JsonNode tree1 = mapper.readTree(jsonBefore);
		final JsonNode tree2 = mapper.readTree(jsonAfter);

		// Assert
		assertEquals(tree1, tree2, "The two JSON strings should be equal");
	}
}
