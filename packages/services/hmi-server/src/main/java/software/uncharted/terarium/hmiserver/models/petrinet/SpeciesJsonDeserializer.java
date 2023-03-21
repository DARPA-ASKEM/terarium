package software.uncharted.terarium.hmiserver.models.petrinet;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import software.uncharted.terarium.hmiserver.models.Ontology;

import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeciesJsonDeserializer extends JsonDeserializer<Species> {
	/*
		{
			"sname": "S",
			"mira_ids": "[('identity', 'ido:0000514'), ('identity', 'ido:0000511')]",
			"mira_context": [('identity', 'ido:0000514')]",
		}
	 */


	@Override
	public Species deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException, JacksonException {
		JsonNode node = p.getCodec().readTree(p);
		final Species species = new Species();
		species.setName(node.get("sname").asText());

		final Pattern pattern = Pattern.compile("\\(\\'\\w+\\', \\'\\w+:\\d+\\'\\)");

		final Matcher miraIdsMatcher = pattern.matcher(node.get("mira_ids").asText());
		species.setMiraIds(new ArrayList<>());
		int i = miraIdsMatcher.groupCount();
		if (i > 0) {
			while (miraIdsMatcher.find()) {
				species.getMiraIds().add(new Ontology(miraIdsMatcher.group(i)));
				i++;
			}
		}

		final Matcher miraContextMatcher = pattern.matcher(node.get("mira_context").asText());
		species.setMiraContext(new ArrayList<>());
		i = miraContextMatcher.groupCount();
		if (i > 0) {
			while (miraContextMatcher.find()) {
				species.getMiraContext().add(new Ontology(miraContextMatcher.group(i)));
				i++;
			}
		}

		return species;
	}
}
