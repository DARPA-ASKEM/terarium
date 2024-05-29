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
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.State;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors
@Entity
public class ObservableSemantic extends Semantic {
	private String id;

	@Type(JsonType.class)
	@Column(columnDefinition = "json")
	private State[] states;

	private String expression;

	@ManyToOne
	@JsonBackReference
	@NotNull private ModelConfiguration modelConfiguration;
}
