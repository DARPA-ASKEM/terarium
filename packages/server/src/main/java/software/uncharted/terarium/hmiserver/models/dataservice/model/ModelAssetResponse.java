package software.uncharted.terarium.hmiserver.models.dataservice.model;

import java.util.List;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.simulationservice.interventions.InterventionPolicy;

@TSModel
public class ModelAssetResponse {

	private Model model;
	private List<ModelConfiguration> modelConfigurations;
	private List<InterventionPolicy> interventionPolicies;

	// Getters and setters

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public List<ModelConfiguration> getModelConfigurations() {
		return modelConfigurations;
	}

	public void setModelConfigurations(List<ModelConfiguration> modelConfigurations) {
		this.modelConfigurations = modelConfigurations;
	}

	public List<InterventionPolicy> getInterventionPolicies() {
		return interventionPolicies;
	}

	public void setInterventionPolicies(List<InterventionPolicy> interventionPolicies) {
		this.interventionPolicies = interventionPolicies;
	}
}
