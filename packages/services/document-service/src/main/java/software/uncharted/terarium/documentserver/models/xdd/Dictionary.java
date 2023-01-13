package software.uncharted.terarium.documentserver.models.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class Dictionary implements Serializable {

	private Number dict_id;

	private String name;

	@JsonbProperty("base_classification")
	private String baseClassification;

	private String source;

	@JsonbProperty("case_sensitive")
	private Boolean caseSensitive;

	@JsonbProperty("last_updated")
	private Instant lastUpdated;

}
