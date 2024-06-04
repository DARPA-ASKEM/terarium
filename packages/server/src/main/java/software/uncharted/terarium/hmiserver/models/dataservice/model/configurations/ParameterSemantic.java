package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelDistribution;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelUnit;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.Intervention;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors
@Entity
public class ParameterSemantic extends Semantic {

	private String referenceId;

	private ModelDistribution distribution;

	private boolean isDefault;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private Intervention[] interventions;

	private ModelUnit modelUnit;

	@ManyToOne
	@JsonBackReference
	@NotNull private ModelConfiguration modelConfiguration;
}
