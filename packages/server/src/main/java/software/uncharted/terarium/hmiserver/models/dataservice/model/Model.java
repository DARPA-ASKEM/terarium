package software.uncharted.terarium.hmiserver.models.dataservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAssetThatSupportsAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelHeader;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelSemantics;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Initial;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Observable;
import software.uncharted.terarium.hmiserver.utils.JsonUtil;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Entity
@TSModel
public class Model extends TerariumAssetThatSupportsAdditionalProperties {

	@Serial
	private static final long serialVersionUID = 398195277271188277L;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private ModelHeader header;

	@TSOptional
	@Column(length = 255)
	private String userId;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private Map<String, JsonNode> model;

	@TSOptional
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private JsonNode properties;

	@TSOptional
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private ModelSemantics semantics;

	@TSOptional
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private ModelMetadata metadata;

	public void retainMetadataFields(final Model other) {
		final Map<String, JsonNode> props = getAdditionalProperties();
		final Map<String, JsonNode> otherProps = other.getAdditionalProperties();

		if (metadata == null) {
			metadata = other.getMetadata();
		} else {
			metadata.retainMetadataFields(other.getMetadata());
		}

		final List<String> propertiesToPreserve = List.of(
			"states",
			"metadata",
			"units",
			"vertices",
			"edges",
			"parameters",
			"initials"
		);

		for (final String property : propertiesToPreserve) {
			if (!otherProps.containsKey(property) || !otherProps.get(property).isArray()) {
				continue;
			}

			if (!props.containsKey(property) || !props.get(property).isArray()) {
				continue;
			}

			final ArrayNode otherProperty = (ArrayNode) otherProps.get(property);
			final ArrayNode thisProperty = (ArrayNode) props.get(property);

			for (final JsonNode element : otherProperty) {
				final JsonNode matching = JsonUtil.getFirstByPredicate(thisProperty, (final JsonNode node) -> {
					// Check if the 'state' object has a 'name' field
					return node.has("id");
				});

				if (matching == null) {
					// does not exist in current model, we can add the old one, otherwise keep the
					// new one
					thisProperty.add(element);
				}
			}

			props.put(property, thisProperty);
		}
	}

	public ModelMetadata getMetadata() {
		if (metadata == null) {
			return new ModelMetadata();
		}
		return metadata;
	}

	@Override
	public Model clone() {
		final Model clone = new Model();
		super.cloneSuperFields(clone);

		if (header != null) {
			clone.header = header.clone();
		}
		clone.userId = this.userId;

		if (model != null) {
			clone.model = new HashMap<>();
			for (final Map.Entry<String, JsonNode> entry : model.entrySet()) {
				clone.model.put(entry.getKey(), entry.getValue().deepCopy());
			}
		}

		if (properties != null) {
			clone.properties = properties.deepCopy();
		}

		if (semantics != null) {
			clone.semantics = semantics.clone();
		}

		if (metadata != null) {
			clone.metadata = metadata.clone();
		}

		return clone;
	}

	@JsonIgnore
	@TSIgnore
	public List<Observable> getObservables() {
		if (
			this.getSemantics() == null ||
			this.getSemantics().getOde() == null ||
			this.getSemantics().getOde().getObservables() == null
		) {
			return new ArrayList<Observable>();
		}
		return this.getSemantics().getOde().getObservables();
	}

	@JsonIgnore
	@TSIgnore
	public List<ModelParameter> getParameters() {
		final ObjectMapper objectMapper = new ObjectMapper();
		if (this.isRegnet()) {
			return objectMapper.convertValue(this.getModel().get("parameters"), new TypeReference<>() {});
		}
		if (
			this.getSemantics() == null ||
			this.getSemantics().getOde() == null ||
			this.getSemantics().getOde().getParameters() == null
		) {
			return new ArrayList<ModelParameter>();
		}
		return this.getSemantics().getOde().getParameters();
	}

	@JsonIgnore
	@TSIgnore
	public List<Initial> getInitials() {
		final ObjectMapper objectMapper = new ObjectMapper();
		if (this.isRegnet()) {
			return objectMapper.convertValue(this.getModel().get("initials"), new TypeReference<>() {});
		} else {
			if (
				this.getSemantics() == null ||
				this.getSemantics().getOde() == null ||
				this.getSemantics().getOde().getInitials() == null
			) {
				return new ArrayList<Initial>();
			}
			return this.getSemantics().getOde().getInitials();
		}
	}

	@JsonIgnore
	@TSIgnore
	public boolean isRegnet() {
		return this.getHeader().getSchemaName().equalsIgnoreCase("regnet");
	}
}
