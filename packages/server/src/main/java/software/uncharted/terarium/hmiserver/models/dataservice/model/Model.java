package software.uncharted.terarium.hmiserver.models.dataservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serial;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAssetThatSupportsAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelSemantics;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Initial;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class Model extends TerariumAssetThatSupportsAdditionalProperties {

	@Serial
	private static final long serialVersionUID = 398195277271188277L;

	private ModelHeader header;

	@TSOptional
	private String userId;

	private Map<String, JsonNode> model;

	@TSOptional
	private JsonNode properties;

	@TSOptional
	private ModelSemantics semantics;

	@TSOptional
	private ModelMetadata metadata;


	public Model() {
		super();
	}

	// Copy constructor
	public Model(final Model other) {
		super();
		this.setId(other.getId());
		this.setName(other.getName());
		this.setTemporary(other.getTemporary());
		this.setPublicAsset(other.getPublicAsset());

		if (other.getCreatedOn() != null) {
			this.setCreatedOn((Timestamp) other.getCreatedOn());
		}

		if (other.getUpdatedOn() != null) {
			this.setUpdatedOn((Timestamp) other.getUpdatedOn());
		}

		if (other.getDeletedOn() != null) {
			this.setDeletedOn((Timestamp) other.getDeletedOn());
		}

		this.header = other.header;
		this.userId = other.userId;
		this.model = new HashMap<>(other.model);
		this.properties = other.properties;
		this.semantics = other.semantics;
		this.metadata = other.metadata;
	}

	@JsonIgnore
	@TSIgnore
	public List<ModelParameter> getParameters() {
		final ObjectMapper objectMapper = new ObjectMapper();
		if (this.isRegnet()) {
			return objectMapper.convertValue(
					this.getModel().get("parameters"), new TypeReference<List<ModelParameter>>() {});
		} else {
			return this.getSemantics().getOde().getParameters();
		}
	}

	@JsonIgnore
	@TSIgnore
	public List<Initial> getInitials() {
		final ObjectMapper objectMapper = new ObjectMapper();
		if (this.isRegnet()) {
			return objectMapper.convertValue(this.getModel().get("initials"), new TypeReference<List<Initial>>() {});
		} else {
			return this.getSemantics().getOde().getInitials();
		}
	}

	@JsonIgnore
	@TSIgnore
	public boolean isRegnet() {
		return this.getHeader().getSchemaName().equalsIgnoreCase("regnet");
	}
}
