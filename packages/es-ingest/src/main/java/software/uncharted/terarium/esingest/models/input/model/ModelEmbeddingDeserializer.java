package software.uncharted.terarium.esingest.models.input.model;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelEmbeddingDeserializer extends JsonDeserializer<ModelEmbedding> {

	@Override
	public ModelEmbedding deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException {

		ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
		JsonNode node = mapper.readTree(jsonParser);

		ModelEmbedding embedding = new ModelEmbedding();

		// The only top level key is the ID
		Iterator<String> fieldNames = node.fieldNames();

		JsonNode body = null;
		if (fieldNames.hasNext()) {
			String id = fieldNames.next();
			// Use firstKey
			embedding.setId(id);

			body = node.get(id);
		}

		if (body == null) {
			throw new IOException("Expected a top level key");
		}

		embedding.setModelCard(body.get("response"));

		JsonNode embeddingsNode = body.get("embedding");

		if (!embeddingsNode.isArray()) {
			throw new IOException("Expected an \"embedding\" array");
		}

		double[] embeddings = new double[embeddingsNode.size()];
		for (int i = 0; i < embeddingsNode.size(); i++) {
			embeddings[i] = embeddingsNode.get(i).asDouble();
		}
		embedding.setEmbedding(embeddings);

		return embedding;
	}
}
