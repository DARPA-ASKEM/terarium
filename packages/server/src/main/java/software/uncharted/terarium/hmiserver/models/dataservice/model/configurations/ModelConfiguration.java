package software.uncharted.terarium.hmiserver.models.dataservice.model.configurations;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import software.uncharted.terarium.hmiserver.annotations.TSIgnore;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.TerariumAsset;
import software.uncharted.terarium.hmiserver.models.simulationservice.parts.Intervention;

@EqualsAndHashCode(callSuper = true)
@Data
@TSModel
@Accessors(chain = true)
@Entity
public class ModelConfiguration extends TerariumAsset {
	@TSOptional
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

	@Override
	public ModelConfiguration clone() {
		final ModelConfiguration clone = new ModelConfiguration();
		super.cloneSuperFields(clone);

		clone.setModelId(this.modelId);

		if (this.observableSemanticList != null) {
			clone.setObservableSemanticList(new ArrayList<>());
			for (final ObservableSemantic semantic : observableSemanticList) {
				clone.getObservableSemanticList().add(semantic.clone());
			}
		}

		if (this.parameterSemanticList != null) {
			clone.setParameterSemanticList(new ArrayList<>());
			for (final ParameterSemantic semantic : parameterSemanticList) {
				clone.getParameterSemanticList().add(semantic.clone());
			}
		}

		if (this.initialSemanticList != null) {
			clone.setInitialSemanticList(new ArrayList<>());
			for (final InitialSemantic semantic : initialSemanticList) {
				clone.getInitialSemanticList().add(semantic.clone());
			}
		}
		return clone;
	}

	@JsonIgnore
	@TSIgnore
	public List<Intervention> getInterventions() {
		final List<Intervention> interventions = new ArrayList<>();
		if (this.parameterSemanticList != null) {
			for (final ParameterSemantic parameterSemantic : this.parameterSemanticList) {
				if (parameterSemantic.getInterventions() != null) {
					interventions.addAll(parameterSemantic.getInterventions());
				}
			}
		}
		return interventions;
	}
}
