package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelDistribution;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Entity
public class InferredParameterSemantic extends Semantic {

	private String referenceId;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private ModelDistribution distribution;

	private boolean isDefault;

	@ManyToOne
	@JsonBackReference
	@Schema(hidden = true)
	@TSIgnore
	@NotNull
	private ModelConfiguration modelConfiguration;

	@Override
	public InferredParameterSemantic clone() {
		final InferredParameterSemantic clone = new InferredParameterSemantic();
		super.cloneSuperFields(clone);
		clone.referenceId = this.referenceId;
		clone.distribution = this.distribution.clone();
		clone.isDefault = this.isDefault;
		return clone;
	}
}
