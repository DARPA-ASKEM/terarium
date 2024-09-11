package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
import software.uncharted.terarium.hmiserver.utils.Messages;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
public class ModelConfigurationService
	extends TerariumAssetServiceWithoutSearch<ModelConfiguration, ModelConfigRepository> {

	final Messages messages;

	public ModelConfigurationService(
		final ObjectMapper objectMapper,
		final Config config,
		final ProjectService projectService,
		final ProjectAssetService projectAssetService,
		final ModelConfigRepository repository,
		final S3ClientService s3ClientService,
		final Messages messages
	) {
		super(
			objectMapper,
			config,
			projectService,
			projectAssetService,
			repository,
			s3ClientService,
			ModelConfiguration.class
		);
		this.messages = messages;
	}

	private static final String CONSTANT_TYPE = "Constant";
	private static final String VALUE_PARAM = "value";

	@Override
	public UUID getProjectIdForAsset(final UUID assetId) {
		//TODO Make modelconfigs a project asset to remove this special override

		final Optional<ModelConfiguration> modelConfiguration = super.getAsset(assetId, Schema.Permission.READ);
		if (modelConfiguration.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NO_CONTENT, messages.get("modelconfig.not-found"));
		}
		return super.getProjectIdForAsset(modelConfiguration.get().getModelId());
	}

	@Override
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Model Configurations are not stored in S3");
	}

	@Override
	@Observed(name = "function_profile")
	public ModelConfiguration createAsset(
		final ModelConfiguration asset,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException {
		setSemanticDBRelationships(asset);
		return super.createAsset(asset, projectId, hasWritePermission);
	}

	@Override
	@Observed(name = "function_profile")
	public Optional<ModelConfiguration> updateAsset(
		final ModelConfiguration asset,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException {
		setSemanticDBRelationships(asset);
		return super.updateAsset(asset, projectId, hasWritePermission);
	}

	@Override
	@Observed(name = "function_profile")
	public List<ModelConfiguration> createAssets(
		final List<ModelConfiguration> assets,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException {
		for (final ModelConfiguration modelConfiguration : assets) {
			setSemanticDBRelationships(modelConfiguration);
		}
		return super.createAssets(assets, projectId, hasWritePermission);
	}

	public static ModelConfiguration modelConfigurationFromAMR(
		final Model model,
		final String name,
		final String description
	) {
		final ModelConfiguration modelConfiguration = new ModelConfiguration();
		modelConfiguration.setName(name != null ? name : "Default configuration");
		modelConfiguration.setDescription(description != null ? description : "This is a default configuration.");
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

		if (model == null || model.getInitials() == null) return initialSemantics;

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

		if (model == null || model.getObservables() == null) return observableSemantics;

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

		if (model == null || model.getParameters() == null) return parameterSemantics;

		for (final ModelParameter parameter : model.getParameters()) {
			final ParameterSemantic parameterSemantic = new ParameterSemantic();
			parameterSemantic.setReferenceId(parameter.getId());

			final ModelDistribution distribution = getModelDistribution(parameter);

			parameterSemantic.setDistribution(distribution);
			parameterSemantics.add(parameterSemantic);
		}
		return parameterSemantics;
	}

	private static ModelDistribution getModelDistribution(final ModelParameter parameter) {
		ModelDistribution distribution = parameter.getDistribution();
		// constant distribution
		if (distribution == null || distribution.getType() == null) {
			distribution = new ModelDistribution();
			distribution.setType("Constant");
			distribution.setParameters(Map.of("value", parameter.getValue() != null ? parameter.getValue() : 0));
		}

		// NOTE: there isn't any difference between Uniform1 and StandardUniform1, so we
		// are changing it to
		// StandardUniform1 for consistenty sake
		if (distribution.getType().equals("Uniform1")) {
			distribution.setType("StandardUniform1");
		}
		return distribution;
	}

	public static Model createAMRFromConfiguration(final Model model, final ModelConfiguration modelConfiguration) {
		setModelParameters(model.getParameters(), modelConfiguration.getParameterSemanticList());
		setModelInitials(model.getInitials(), modelConfiguration.getInitialSemanticList());
		setModelObservables(model.getObservables(), modelConfiguration.getObservableSemanticList());
		return model.clone();
	}

	private static void setModelParameters(
		final List<ModelParameter> modelParameters,
		final List<ParameterSemantic> configParameters
	) {
		// Create a map from ConfigParameter IDs to ConfigParameter objects
		final Map<String, ParameterSemantic> configParameterMap = new HashMap<>();
		for (final ParameterSemantic configParameter : configParameters) {
			configParameterMap.put(configParameter.getReferenceId(), configParameter);
		}

		// Iterate through the list of ModelParameter objects
		for (final ModelParameter modelParameter : modelParameters) {
			// Look up the corresponding ConfigParameter in the map
			final ParameterSemantic matchingConfigParameter = configParameterMap.get(modelParameter.getId());
			if (matchingConfigParameter != null) {
				// set distributions
				if (CONSTANT_TYPE.equals(matchingConfigParameter.getDistribution().getType())) {
					try {
						modelParameter.setValue(
							NumberFormat.getInstance()
								.parse(matchingConfigParameter.getDistribution().getParameters().get(VALUE_PARAM).toString())
								.doubleValue()
						);
					} catch (final ParseException e) {
						throw new RuntimeException(e);
					}
					modelParameter.setDistribution(null);
				} else {
					modelParameter.setDistribution(matchingConfigParameter.getDistribution());
				}
			}
		}
	}

	private static void setModelInitials(final List<Initial> modelInitials, final List<InitialSemantic> configInitials) {
		final Map<String, InitialSemantic> configInitialMap = new HashMap<>();
		for (final InitialSemantic configInitial : configInitials) {
			configInitialMap.put(configInitial.getTarget(), configInitial);
		}

		for (final Initial modelInitial : modelInitials) {
			final InitialSemantic matchingConfigInitial = configInitialMap.get(modelInitial.getTarget());
			if (matchingConfigInitial != null) {
				modelInitial.setExpression(matchingConfigInitial.getExpression());
				modelInitial.setExpressionMathml(matchingConfigInitial.getExpressionMathml());
			}
		}
	}

	private static void setModelObservables(
		final List<Observable> modelObservables,
		final List<ObservableSemantic> configObservables
	) {
		final Map<String, ObservableSemantic> configObservableMap = new HashMap<>();
		for (final ObservableSemantic configObservable : configObservables) {
			configObservableMap.put(configObservable.getReferenceId(), configObservable);
		}

		for (final Observable modelObservable : modelObservables) {
			final ObservableSemantic matchingConfigObservable = configObservableMap.get(modelObservable.getId());
			if (matchingConfigObservable != null) {
				modelObservable.setStates(matchingConfigObservable.getStates());
				modelObservable.setExpression(matchingConfigObservable.getExpression());
				modelObservable.setExpressionMathml(matchingConfigObservable.getExpressionMathml());
			}
		}
	}
}
