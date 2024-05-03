package software.uncharted.terarium.hmiserver.models.dataservice;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.Data;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

/** Represents a grounding document from TDS */
@Data
@Accessors(chain = true)
@TSModel
public class Grounding implements Serializable {

	@Serial
	private static final long serialVersionUID = 302308407252037615L;

	/** Ontological identifier per DKG */
	@Convert(converter = ObjectConverter.class)
	@Column(columnDefinition = "text")
	private List<Identifier> identifiers;

	/** (Optional) Additional context that informs the grounding */
	@TSOptional
	@Convert(converter = ObjectConverter.class)
	@Column(columnDefinition = "text")
	private Map<String, Object> context;

	@Override
	public Grounding clone() {

		final Grounding clone = new Grounding();
		if (this.identifiers != null) {
			clone.identifiers = new ArrayList<>();
			clone.identifiers.addAll(this.identifiers);
		}
		if (this.context != null) {
			clone.context = new HashMap<>();
			for (final String key : this.context.keySet()) {
				clone.context.put(key, context.get(key));
			}
		}

		return clone;
	}
}
