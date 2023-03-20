package software.uncharted.terarium.hmiserver.models.petrinet;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import javax.persistence.criteria.Root;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpeciesJsonDeserializer extends JsonDeserializer<Species> {

	@Override
	public Species deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		Root root = p.readValueAs(Root.class);
		if (root == null) return null;

		Species species = new Species();
		species.setName(root.sname);

		Pattern pattern = Pattern.compile("\\(\\'(.+?)\\', \\'(.+?)\\'\\)");

		Matcher miraIdsMatcher = pattern.matcher(root.miraIds);


		return species;
	}
}
