package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors
@Entity
public class ModelConfiguration extends TerariumAsset {
	private UUID calibrationRunId;
	private UUID modelId;

	@OneToMany(mappedBy = "modelConfiguration", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@JsonManagedReference
	private List<ObservableSemantic> observableSemanticList;

	@OneToMany(mappedBy = "modelConfiguration", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@JsonManagedReference
	private List<ParameterSemantic> parameterSemanticList;

	@OneToMany(mappedBy = "modelConfiguration", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@JsonManagedReference
	private List<InitialSemantic> initialSemanticList;
}
