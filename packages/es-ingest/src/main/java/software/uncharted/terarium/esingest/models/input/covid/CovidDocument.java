package software.uncharted.terarium.esingest.models.input.covid;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;
import software.uncharted.terarium.esingest.models.input.IInputDocument;

@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CovidDocument implements IInputDocument, Serializable {

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static public class Source implements Serializable {
		private String title;

		private String body;

		private Feature feature;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static public class Feature implements Serializable {
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
	UUID id;

	@JsonAlias("_source")
	Source source;
}
