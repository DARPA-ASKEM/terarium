package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
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
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

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

	@Override
	@Observed(name = "function_profile")
	public ModelConfiguration createAsset(final ModelConfiguration asset, final Schema.Permission hasWritePermission)
			throws IOException {
		setSemanticDBRelationships(asset);
		return super.createAsset(asset, hasWritePermission);
	}

	@Override
	@Observed(name = "function_profile")
	public List<ModelConfiguration> createAssets(
			final List<ModelConfiguration> assets, final Schema.Permission hasWritePermission) throws IOException {

		for (final ModelConfiguration modelConfiguration : assets) {
			setSemanticDBRelationships(modelConfiguration);
		}
		return super.createAssets(assets, hasWritePermission);
	}

	public static ModelConfiguration modelConfigurationFromAMR(
			final Model model, final String name, final String description) {
		final ModelConfiguration modelConfiguration = new ModelConfiguration();
		modelConfiguration.setName(name == null ? name : "Default Configuration");
		modelConfiguration.setDescription(description == null ? description : "This is a default configuration.");
		modelConfiguration.setModelId(model.getId());
		modelConfiguration.setParameterSemanticList(createParameterSemanticList(model));
		modelConfiguration.setInitialSemanticList(createInitialSemanticList(model));
		modelConfiguration.setObservableSemanticList(createObservableSemanticList(model));
		return modelConfiguration;
	}

	private static void setSemanticDBRelationships(final ModelConfiguration modelConfiguration) {
		if (modelConfiguration.getObservableSemanticList() != null) {
			for (final ObservableSemantic semantic : modelConfiguration.getObservableSemanticList()) {
				semantic.setModelConfiguration(modelConfiguration);
			}
		}

		if (modelConfiguration.getParameterSemanticList() != null) {
			for (final ParameterSemantic semantic : modelConfiguration.getParameterSemanticList()) {
				semantic.setModelConfiguration(modelConfiguration);
			}
		}

		if (modelConfiguration.getInitialSemanticList() != null) {
			for (final InitialSemantic semantic : modelConfiguration.getInitialSemanticList()) {
				semantic.setModelConfiguration(modelConfiguration);
			}
		}
	}

	private static List<InitialSemantic> createInitialSemanticList(final Model model) {
		final List<InitialSemantic> initialSemantics = new ArrayList<>();
		for (final Initial initial : model.getInitials()) {
			final InitialSemantic initialSemantic = new InitialSemantic();
			initialSemantic.setTarget(initial.getTarget());
			initialSemantic.setExpression(initial.getExpression());
			initialSemantic.setExpressionMathml(initial.getExpressionMathml());
			initialSemantics.add(initialSemantic);
		}
		return initialSemantics;
	}

	private static List<ObservableSemantic> createObservableSemanticList(final Model model) {
		final List<ObservableSemantic> observableSemantics = new ArrayList<>();
		for (final Observable observable : model.getObservables()) {
			final ObservableSemantic observableSemantic = new ObservableSemantic();
			observableSemantic.setReferenceId(observable.getId());
			observableSemantic.setStates(observable.getStates());
			observableSemantic.setExpression(observable.getExpression());
			observableSemantic.setExpressionMathml(observable.getExpressionMathml());
			observableSemantics.add(observableSemantic);
		}
		return observableSemantics;
	}

	private static List<ParameterSemantic> createParameterSemanticList(final Model model) {
		final List<ParameterSemantic> parameterSemantics = new ArrayList<>();
		for (final ModelParameter parameter : model.getParameters()) {
			final ParameterSemantic parameterSemantic = new ParameterSemantic();
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
