package software.uncharted.terarium.ingest.input.epi;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import software.uncharted.terarium.ingest.input.IAssetInput;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Document implements IAssetInput {

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

	public UUID getId() {
		return null;
	}

	public JsonNode getAsset() {
		return null;
	}
}
