package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
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
	@TSOptional
	private UUID simulationId;

	@OneToMany(mappedBy = "modelConfiguration", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<ObservableSemantic> observableSemanticList = new ArrayList<>();

	@OneToMany(mappedBy = "modelConfiguration", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<ParameterSemantic> parameterSemanticList = new ArrayList<>();

	@OneToMany(mappedBy = "modelConfiguration", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<InitialSemantic> initialSemanticList = new ArrayList<>();
}
