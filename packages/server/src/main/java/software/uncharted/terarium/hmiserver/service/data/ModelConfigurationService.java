package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.InitialSemantic;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ModelConfiguration;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ObservableSemantic;
import software.uncharted.terarium.hmiserver.models.dataservice.model.configurations.ParameterSemantic;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelDistribution;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Initial;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Observable;
import software.uncharted.terarium.hmiserver.repository.data.ModelConfigRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;

@Service
public class ModelConfigurationService
		extends TerariumAssetServiceWithoutSearch<ModelConfiguration, ModelConfigRepository> {
	public ModelConfigurationService(
			final ObjectMapper objectMapper,
			final Config config,
			final ProjectAssetService projectAssetService,
			final ModelConfigRepository repository,
			final S3ClientService s3ClientService) {
		super(objectMapper, config, projectAssetService, repository, s3ClientService, ModelConfiguration.class);
	}

	@Override
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Model Configurations are not stored in S3");
	}

	public ModelConfiguration modelConfigurationFromAMR(
			final Model model, final String name, final String description) {
		ModelConfiguration modelConfiguration = new ModelConfiguration();
		modelConfiguration.setName(name == null ? name : "Default Configuration");
		modelConfiguration.setDescription(description == null ? description : "This is a default configuration.");
		modelConfiguration.setModelId(model.getId());
		modelConfiguration.setParameterSemanticList(createParameterSemanticList(model));
		modelConfiguration.setInitialSemanticList(createInitialSemanticList(model));
		modelConfiguration.setObservableSemanticList(createObservableSemanticList(model));
		return modelConfiguration;
	}

	private List<InitialSemantic> createInitialSemanticList(final Model model) {
		List<InitialSemantic> initialSemantics = new ArrayList<InitialSemantic>();
		for (Initial initial : model.getInitials()) {
			InitialSemantic initialSemantic = new InitialSemantic();
			initialSemantic.setTarget(initial.getTarget());
			initialSemantic.setExpression(initial.getExpression());
			initialSemantic.setExpressionMathml(initial.getExpressionMathml());
			initialSemantics.add(initialSemantic);
		}
		return initialSemantics;
	}

	private List<ObservableSemantic> createObservableSemanticList(final Model model) {
		List<ObservableSemantic> observableSemantics = new ArrayList<ObservableSemantic>();
		for (Observable observable : model.getObservables()) {
			ObservableSemantic observableSemantic = new ObservableSemantic();
			observableSemantic.setReferenceId(observable.getId());
			observableSemantic.setStates(observable.getStates());
			observableSemantic.setExpression(observable.getExpression());
			observableSemantic.setExpressionMathml(observable.getExpressionMathml());
			observableSemantics.add(observableSemantic);
		}
		return observableSemantics;
	}

	private List<ParameterSemantic> createParameterSemanticList(final Model model) {
		List<ParameterSemantic> parameterSemantics = new ArrayList<ParameterSemantic>();
		for (ModelParameter parameter : model.getParameters()) {
			ParameterSemantic parameterSemantic = new ParameterSemantic();
			parameterSemantic.setReferenceId(parameter.getId());
			parameterSemantic.setDefault(true);

			ModelDistribution distribution = parameter.getDistribution();
			// constant distribution
			if (distribution == null || distribution.getType() == null) {
				distribution = new ModelDistribution();
				distribution.setType("Constant");
				distribution.setParameters(Map.of("value", parameter.getValue() == null ? parameter.getValue() : 0));
			}

			parameterSemantic.setDistribution(distribution);
			parameterSemantics.add(parameterSemantic);
		}
		return parameterSemantics;
	}
}
