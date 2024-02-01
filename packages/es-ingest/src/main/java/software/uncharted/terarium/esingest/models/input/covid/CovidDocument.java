package software.uncharted.terarium.esingest.models.input.covid;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor
@Data
public class CovidDocument implements Serializable {

	@Data
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

	private UUID id;

	private String title;

	private String body;

	private Feature feature;

}
