package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Arrays;

@Data
@Accessors(chain = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
	@JsonSubTypes.Type(value = DocumentAsset.class, name = "publication"),
	@JsonSubTypes.Type(value = Model.class, name = "model"),
})
public class ResourceType implements Serializable {
	public enum Type {
		DATASETS("datasets"),
		EXTRACTIONS("extractions"),
		INTERMEDIATES("intermediates"),
		MODELS("models"),
		PLANS("plans"),
		PUBLICATIONS("publications"),
		SIMULATION_RUNS("simulation_runs");

		public final String type;

		/**
		 * Returns the enum for a given string representation of a ResourceType
		 *
		 * @param type the string representation of a ResourceType
		 * @return a ResourceType from the type string
		 * @throws IllegalArgumentException if the ResourceType is not found
		 */
		public static Type findByType(final String type) {
			return Arrays.stream(values()).filter(
				value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No ResourceType with type: " + type)
			);
		}

		Type(final String type) {
			this.type = type;
		}
	}


	private String id;

}


