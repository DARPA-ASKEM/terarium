package software.uncharted.terarium.hmiserver.models.dataservice.concept;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.models.dataservice.TaggableType;

@Data
@Accessors(chain = true)
public class ConceptFacetSearchResponse implements Serializable {

	@Data
	@Accessors(chain = true)
	public static class Result {
		private TaggableType type;
		private UUID id;
		private String curie;
		private String name;
	}

	@Data
	@Accessors(chain = true)
	public static class Concept {
		private Long count;
		private String name;
	}

	@Data
	@Accessors(chain = true)
	public static class Facets {
		private Map<TaggableType, Long> types;
		private Map<String, Concept> concepts;
	}

	private Facets facets;
	private List<Result> results;
}
