package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.Charset;
import org.apache.http.entity.ContentType;

public class ContentTypeDeserializer extends JsonDeserializer<ContentType> {

	@Override
	public ContentType deserialize(final JsonParser jp, final DeserializationContext ctxt)
		throws IOException, JsonProcessingException {
		final ObjectMapper mapper = (ObjectMapper) jp.getCodec();
		final JsonNode node = mapper.readTree(jp);

		final String mimeTypeStr = node.get("mimeType").asText();
		if (!node.hasNonNull("charset")) {
			return ContentType.create(mimeTypeStr);
		}

		final String charsetStr = node.get("charset").asText();
		final Charset charset = Charset.forName(charsetStr);
		return ContentType.create(mimeTypeStr, charset);
	}
}
