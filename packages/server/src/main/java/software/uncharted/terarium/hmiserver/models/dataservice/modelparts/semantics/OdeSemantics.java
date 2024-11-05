package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.AMRSchemaType;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.SupportAdditionalProperties;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;

@Data
@EqualsAndHashCode(callSuper = true)
@AMRSchemaType
@Accessors(chain = true)
public class OdeSemantics extends SupportAdditionalProperties implements Serializable {

	@Serial
	private static final long serialVersionUID = 8943488983879443909L;

	private List<Rate> rates = new ArrayList<>();

	@TSOptional
	private List<Initial> initials = new ArrayList<>();

	@TSOptional
	private List<ModelParameter> parameters = new ArrayList<>();

	@TSOptional
	private List<Observable> observables = new ArrayList<>();

	@TSOptional
	private JsonNode time;

	@Override
	public OdeSemantics clone() {
		final OdeSemantics clone = (OdeSemantics) super.clone();

		if (this.rates != null) {
			clone.rates = new ArrayList<>();
			for (final Rate rate : this.rates) {
				clone.rates.add(rate.clone());
			}
		}

		if (this.initials != null) {
			clone.initials = new ArrayList<>();
			for (final Initial init : this.initials) {
				clone.initials.add(init.clone());
			}
		}

		if (this.parameters != null) {
			clone.parameters = new ArrayList<>();
			for (final ModelParameter parameter : this.parameters) {
				clone.parameters.add(parameter.clone());
			}
		}

		if (this.observables != null) {
			clone.observables = new ArrayList<>();
			for (final Observable observable : this.observables) {
				clone.observables.add(observable.clone());
			}
		}

		if (this.time != null) {
			clone.time = this.time.deepCopy();
		}

		return clone;
	}
}
