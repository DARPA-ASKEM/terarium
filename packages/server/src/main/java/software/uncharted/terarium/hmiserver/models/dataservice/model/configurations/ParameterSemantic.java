package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelDistribution;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.Intervention;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors
@Entity
public class ParameterSemantic extends Semantic {

	private String referenceId;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private ModelDistribution distribution;

	private boolean isDefault;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private List<Intervention> interventions;

	@ManyToOne
	@JsonBackReference
	@Schema(hidden = true)
	@NotNull private ModelConfiguration modelConfiguration;

	@Override
	public ParameterSemantic clone() {
		final ParameterSemantic clone = new ParameterSemantic();
		super.cloneSuperFields(clone);
		clone.referenceId = this.referenceId;
		clone.distribution = this.distribution.clone();
		clone.isDefault = this.isDefault;
		if (this.interventions != null) {
			clone.interventions = new ArrayList<>();
			for (final Intervention intervention : this.interventions) {
				clone.interventions.add(new Intervention()
						.setName(intervention.getName())
						.setValue(intervention.getValue())
						.setTimestep(intervention.getTimestep()));
			}
		}
		return clone;
	}
}
