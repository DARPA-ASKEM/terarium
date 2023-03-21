package software.uncharted.terarium.hmiserver.models.petrinet;

import lombok.Data;

import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.smallrye.jwt.config.ConfigLogging.log;

@Data
public class Ontology implements Serializable {
	private final String name;
	private final String curie;
	private String title;
	private String description;
	private URL url;

	public Ontology (String input) {
		final Matcher matcher = Pattern.compile("\\(\\'(.+?)\\', \\'(.+?)\\'\\)").matcher(input);
		final boolean result = matcher.find();
		int i = matcher.groupCount();
		if (result && i > 0) {
			this.name = matcher.group(1);
			this.curie = matcher.group(2);
		} else {
			this.name = this.curie = null;
		}
	}
}
