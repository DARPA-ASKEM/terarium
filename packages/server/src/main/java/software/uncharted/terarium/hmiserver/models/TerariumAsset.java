package software.uncharted.terarium.hmiserver.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
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

	public static void removeFieldsWithKeys(final ObjectNode objectNode, final List<String> keys) {
		final Iterator<String> keysIterator = objectNode.fieldNames();
		while (keysIterator.hasNext()) {
			final String key = keysIterator.next();
			if (keys.contains(key)) {
				keysIterator.remove();
			} else {
				final JsonNode node = objectNode.get(key);
				if (node.isObject()) {
					removeFieldsWithKeys((ObjectNode) node, keys);
				}
			}
		}
	}

	/**
	 * Serialize the asset to a JSON string, removing the fields that are not
	 * needed.
	 *
	 * @param keepFields             A list of fields that should not be removed.
	 * @param additionalDeleteFields Additional fields to remove.
	 * @return The JSON string.
	 */
	public String serializeWithoutTerariumFields(
		@Nullable final String[] keepFields,
		@Nullable final String[] additionalDeleteFields
	) {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.setConfig(mapper.getSerializationConfig().with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
		final ObjectNode objectNode = mapper.convertValue(this, ObjectNode.class);

		// Fields to delete
		final String[] deleteFields = new String[] {
			"id",
			"createdOn",
			"updatedOn",
			"deletedOn",
			"name",
			"description",
			"temporary",
			"publicAsset",
			"fileNames",
			"userId"
		};
		final List<String> deleteFieldsList = new ArrayList<>(Arrays.asList(deleteFields));
		if (keepFields != null) {
			for (final String field : keepFields) {
				deleteFieldsList.removeIf(key -> key.equals(field));
			}
		}

		// Remove the fields that are not needed
		for (final String key : deleteFieldsList) {
			objectNode.remove(key);
		}

		// Remove the field metadata.description as well
		final JsonNode metadata = objectNode.get("metadata");
		if (metadata != null) {
			((ObjectNode) metadata).remove("description");
		}
		objectNode.set("metadata", metadata);

		// Remove additional fields if they exist
		if (additionalDeleteFields != null) {
			removeFieldsWithKeys(objectNode, Arrays.asList(additionalDeleteFields));
		}

		return objectNode.toString();
	}

	@JsonIgnore
	@TSIgnore
	public String getEmbeddingSourceText() {
		return name + " " + description;
	}

	@JsonIgnore
	@TSIgnore
	public Map<TerariumAssetEmbeddingType, String> getEmbeddingsSourceByType() {
		final Map<TerariumAssetEmbeddingType, String> sources = new HashMap<>();
		if (name != null) {
			sources.put(TerariumAssetEmbeddingType.NAME, name);
		}
		if (description != null) {
			sources.put(TerariumAssetEmbeddingType.DESCRIPTION, description);
		}
		return sources;
	}
}
