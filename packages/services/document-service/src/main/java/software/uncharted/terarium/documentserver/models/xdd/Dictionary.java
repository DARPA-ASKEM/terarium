package software.uncharted.terarium.documentserver.models.xdd;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;
import java.time.Instant;

@Data
@Accessors(chain = true)
public class Dictionary implements Serializable {

	private Number dictId;

	private String name;
	private String baseClassification;

	private String source;
	private Boolean caseSensitive;
	private Instant lastUpdated;

	@JsonbProperty("dict_id")
	public void setDictId(Number dictId) {
		this.dictId = dictId;
	}

	@JsonbProperty("base_classification")
	public void setBaseClassification(String baseClassification) {
		this.baseClassification = baseClassification;
	}

	@JsonbProperty("case_sensitive")
	public void setCaseSensitive(Boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	@JsonbProperty("last_updated")
	public void setLastUpdated(Instant lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

}
