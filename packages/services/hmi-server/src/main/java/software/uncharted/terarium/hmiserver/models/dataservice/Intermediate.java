package software.uncharted.terarium.hmiserver.models.dataservice;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;

@Data
@Accessors(chain = true)
public class Intermediate implements Serializable {

	private String id;

	private Instant timestamp;

	private IntermediateSource source;

	private IntermediateFormat type;

	private String content;
}

enum IntermediateFormat {
	BILAYER("bilayer"),
	GROMET("gromet"),
	OTHER("other"),
	SBML("sbml");

	public final String type;

	/**
	 * Returns the enum for a given string representation of a IntermediateFormat
	 *
	 * @param type the string representation of a IntermediateFormat
	 * @return a IntermediateFormat from the type string
	 * @throws IllegalArgumentException if the IntermediateFormat is not found
	 */
	public static IntermediateFormat findByType(final String type) {
		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No IntermediateFormat with type: " + type)
		);
	}

	IntermediateFormat(final String type) {
		this.type = type;
	}
}

enum IntermediateSource {
	MREPRESENTATIONA("mrepresentationa"),
	SKEMA("skema");

	public final String type;

	/**
	 * Returns the enum for a given string representation of a IntermediateSource
	 *
	 * @param type the string representation of a IntermediateSource
	 * @return a IntermediateSource from the type string
	 * @throws IllegalArgumentException if the IntermediateSource is not found
	 */
	public static IntermediateSource findByType(final String type) {
		return Arrays.stream(values()).filter(
			value -> type.equalsIgnoreCase(value.type)).findFirst().orElseThrow(() -> new IllegalArgumentException("No IntermediateSource with type: " + type)
		);
	}

	IntermediateSource(final String type) {
		this.type = type;
	}
}
