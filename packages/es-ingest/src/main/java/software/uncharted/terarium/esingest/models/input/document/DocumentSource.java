package software.uncharted.terarium.esingest.models.input.document;

import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import software.uncharted.terarium.esingest.models.input.IInputDocument;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentSource implements IInputDocument {

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static public class Source {
		private String title;

		private String body;

		private Feature feature;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static public class Feature {
		private List<Timestamp> date;

		private List<String> website;

		private List<String> doi;

		private List<String> language;

		private List<String> version;

		private List<String> pubname;

		private List<String> organization;

		private List<String> name;
	}

	@JsonAlias("_id")
	String id;

	@JsonAlias("_source")
	Source source;
}
