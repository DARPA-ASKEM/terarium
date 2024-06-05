package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
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

	/** This is "simulation" in the sense of our POJO. It actually corresponds to a pyciemss calibration */
	private UUID simulationId;

	@Transient
	private Map<String, Semantic> values;

	@TSIgnore
	@OneToMany(mappedBy = "modelConfiguration", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@JsonManagedReference
	private List<ObservableSemantic> observableSemanticList;

	@TSIgnore
	@OneToMany(mappedBy = "modelConfiguration", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@JsonManagedReference
	private List<ParameterSemantic> parameterSemanticList;

	@TSIgnore
	@OneToMany(mappedBy = "modelConfiguration", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	@JsonManagedReference
	private List<InitialSemantic> initialSemanticList;
}
