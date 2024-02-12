package software.uncharted.terarium.esingest.models.input.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelAMRDeserializer extends JsonDeserializer<ModelAMR> {

	@Override
	public ModelAMR deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException {

		ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
		mapper.enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS.mappedFeature());
		JsonNode node = mapper.readTree(jsonParser);

		ModelAMR model = new ModelAMR();
		model.setAmr(node);
		return model;
	}
}
