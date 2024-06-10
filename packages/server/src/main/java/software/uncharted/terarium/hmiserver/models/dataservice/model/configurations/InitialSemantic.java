package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors
@TSModel
@Entity
public class InitialSemantic extends Semantic {
	private String target;
	private String expression;
	private String expression_mathml;

	@ManyToOne
	@JsonBackReference
	@NotNull private ModelConfiguration modelConfiguration;
}
