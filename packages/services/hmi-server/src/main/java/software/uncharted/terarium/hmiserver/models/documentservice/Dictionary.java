package software.uncharted.terarium.hmiserver.models.documentservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class Dictionary implements Serializable {

	@JsonAlias("dict_id")
	private Number dictId;

	private String name;

	@JsonAlias("base_classification")
	private String baseClassification;

	private String source;

	@JsonAlias("case_sensitive")
	private Boolean caseSensitive;

	@JsonAlias("last_updated")
	private Instant lastUpdated;

}
