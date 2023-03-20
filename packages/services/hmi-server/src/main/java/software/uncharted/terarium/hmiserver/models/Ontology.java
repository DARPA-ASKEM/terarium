package software.uncharted.terarium.hmiserver.models;

import lombok.Data;

import java.io.Serializable;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Ontology implements Serializable {
	private final String type;
	private final String curie;
	private String title;
	private String description;
	private URL url;

	public Ontology (String input) {
		// input = "('identity', 'ido:0000514')"
		final Matcher matcher = Pattern.compile("\\(\\'(.+?)\\', \\'(.+?)\\'\\)").matcher(input);

		this.type = matcher.group(1);
		this.curie = matcher.group(2);

		// API Call

		this.title = "title";
		this.description = "description";
		try {
			this.url = new URL("github.io");
		} catch (Exception e) {
			this.url = null;
		}
	}
}
