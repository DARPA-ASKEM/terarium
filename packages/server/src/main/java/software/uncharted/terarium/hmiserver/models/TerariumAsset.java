package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TSModel
@MappedSuperclass
public abstract class TerariumAsset extends TerariumEntity {

	@TSOptional
	@Column(length = 512)
	@Schema(defaultValue = "Default Name")
	private String name;

	@TSOptional
	@Schema(defaultValue = "Default Description")
	@Column(columnDefinition = "text")
	private String description;

	@TSOptional
	@JsonAlias("file_names")
	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	protected List<String> fileNames = new ArrayList<>();

	@TSOptional
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@Column(columnDefinition = "TIMESTAMP WITH TIME ZONE")
	private Timestamp deletedOn;

	@TSOptional
	private Boolean temporary = false;

	@TSOptional
	private Boolean publicAsset = false;

	// This is here just to satisfy the service interface.
	@Override
	public TerariumAsset clone() {
		throw new RuntimeException(
			"This should not be called. Override this method on the derived class and call cloneSuperFields instead."
		);
	}

	protected TerariumAsset cloneSuperFields(final TerariumAsset asset) {
		// TODO this should be a part of the clone method, and this should implement
		// Cloneable

		super.cloneSuperFields(asset);

		asset.name = name;
		asset.description = description;
		asset.fileNames = fileNames != null ? new ArrayList<>(fileNames) : new ArrayList<>();
		asset.deletedOn = deletedOn != null ? new Timestamp(deletedOn.getTime()) : null;
		asset.temporary = temporary;
		asset.publicAsset = publicAsset;

		return asset;
	}

	public String serializeWithoutTerariumFields() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setConfig(mapper.getSerializationConfig().with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
		final ObjectNode objectNode = mapper.convertValue(this, ObjectNode.class);
		objectNode.remove("id");
		objectNode.remove("createdOn");
		objectNode.remove("updatedOn");
		objectNode.remove("deletedOn");
		objectNode.remove("name");
		objectNode.remove("description");
		objectNode.remove("temporary");
		objectNode.remove("publicAsset");
		objectNode.remove("fileNames");
		objectNode.remove("userId");
		return objectNode.toString();
	}

	public String serializeWithoutTerariumFieldsKeepId() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setConfig(mapper.getSerializationConfig().with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
		final ObjectNode objectNode = mapper.convertValue(this, ObjectNode.class);
		objectNode.remove("createdOn");
		objectNode.remove("updatedOn");
		objectNode.remove("deletedOn");
		objectNode.remove("name");
		objectNode.remove("description");
		objectNode.remove("temporary");
		objectNode.remove("publicAsset");
		objectNode.remove("fileNames");
		objectNode.remove("userId");
		return objectNode.toString();
	}

	public String serializeWithTerariumFields() throws JsonProcessingException {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setConfig(mapper.getSerializationConfig().with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
		return mapper.writeValueAsString(this);
	}
}
