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
import lombok.experimental.Accessors;

import java.util.List;

import org.hibernate.annotations.Type;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors
@Entity
public class ObservableSemantic extends Semantic {

	private String referenceId;

	private List<String> states;

	private String expression;

	private String expressionMathml;

	@ManyToOne
	@JsonBackReference
	@Schema(hidden = true)
	@NotNull private ModelConfiguration modelConfiguration;
}
