package software.uncharted.terarium.hmiserver.models;

import lombok.Data;

import java.io.Serializable;
import java.net.URL;

@Data
public class Ontology implements Serializable {

	private final String curie;
	private String title;
	private String description;
	private URL url;

	public Ontology (String curie) {
		this.curie = curie;

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
