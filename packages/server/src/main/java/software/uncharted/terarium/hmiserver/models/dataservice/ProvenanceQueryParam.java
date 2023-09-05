package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@TSModel
public class ProvenanceQueryParam implements Serializable {


	private Number rootId;

	private ProvenanceType rootType;

	private Number userId;

	@TSIgnore
	private String curie;


	// Our front end (and server) want to use the non-underscored version of these properties
	// but TDS needs underscores.  Its "easy" when taking data *From* TDS to hmi to do this by
	// simply using the @JsonAlias annotation, however when going the opposite way we need to be
	// very explicit with setters and getters as JsonAlias only works one way.

	@JsonSetter(value = "rootId")
	@TSOptional
	public void setRootId(Number rootId) {
		this.rootId = rootId;
	}

	@JsonGetter(value = "root_id")
	@TSIgnore
	public Number getRootId() {
		return rootId;
	}

	@JsonSetter(value = "rootType")
	@TSOptional
	public void setRootType(ProvenanceType rootType) {
		this.rootType = rootType;
	}

	@JsonGetter(value = "root_type")
	@TSIgnore
	public ProvenanceType getRootType() {
		return rootType;
	}

	@JsonSetter(value = "userId")
	@TSOptional
	public void setUserId(Number userId) {
		this.userId = userId;
	}

	@JsonGetter(value = "user_id")
	@TSIgnore
	public Number getUserId() {
		return userId;
	}


}
