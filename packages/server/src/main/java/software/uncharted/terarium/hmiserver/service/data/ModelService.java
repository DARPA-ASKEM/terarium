package software.uncharted.terarium.hmiserver.service.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.observation.annotation.Observed;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.configuration.Config;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelDescription;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelMetadata;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelParameter;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata.Annotations;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.GroundedSemantic;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.State;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics.Transition;
import software.uncharted.terarium.hmiserver.repository.data.ModelRepository;
import software.uncharted.terarium.hmiserver.service.s3.S3ClientService;
import software.uncharted.terarium.hmiserver.utils.GreekDictionary;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Slf4j
@Service
public class ModelService extends TerariumAssetService<Model, ModelRepository> {

	public ModelService(
		final ObjectMapper objectMapper,
		final Config config,
		final ProjectService projectService,
		final ProjectAssetService projectAssetService,
		final ModelRepository repository,
		final S3ClientService s3ClientService
	) {
		super(objectMapper, config, projectService, projectAssetService, repository, s3ClientService, Model.class);
	}

	@Observed(name = "function_profile")
	public Optional<ModelDescription> getDescription(final UUID id, final Schema.Permission hasReadPermission)
		throws IOException {
		final Optional<Model> model = getAsset(id, hasReadPermission);
		if (model.isPresent()) {
			final ModelDescription md = ModelDescription.fromModel(model.get());
			return Optional.of(md);
		}

		return Optional.empty();
	}

	@Override
	@Observed(name = "function_profile")
	protected String getAssetPath() {
		throw new UnsupportedOperationException("Models are not stored in S3");
	}

	@Observed(name = "function_profile")
	public Optional<Model> getModelFromModelConfigurationId(
		final UUID modelConfigurationId,
		final Schema.Permission hasReadPermission
	) {
		return repository.findModelByModelConfigurationId(modelConfigurationId);
	}

	@Override
	@Observed(name = "function_profile")
	public Model createAsset(final Model asset, final UUID projectId, final Schema.Permission hasWritePermission)
		throws IOException {
		// Make sure that the model framework is set to lowercase
		if (asset.getHeader() != null && asset.getHeader().getSchemaName() != null) asset
			.getHeader()
			.setSchemaName(asset.getHeader().getSchemaName().toLowerCase());

		// Set default value for model parameters (0.0)
		asset
			.getParameters()
			.forEach(param -> {
				if (param.getValue() == null) {
					param.setValue(1.0);
				}
			});

		// Force proper annotation metadata
		final ModelMetadata metadata = asset.getMetadata();
		if (metadata.getAnnotations() == null) {
			metadata.setAnnotations(new Annotations());
			asset.setMetadata(metadata);
		}

		if (asset.getHeader() != null && asset.getHeader().getName() != null) {
			asset.setName(asset.getHeader().getName());
		}

		final JsonNode time = asset.getSemantics().getOde().getTime();
		// set a time parameter and default to day for the model if it doesn't exist
		if (time == null || time.get("id") == null) {
			asset.getSemantics().getOde().setTime(objectMapper.createObjectNode().put("id", "t"));
		}

		// if there is a time parameter, set the default to date if it is "day"
		if (time != null && time.get("units") != null && time.get("units").get("expression").asText().equals("day")) {
			final String id = asset.getSemantics().getOde().getTime().get("id").asText();
			final ObjectNode unitsNode = objectMapper
				.createObjectNode()
				.put("expression", "date")
				.put("expression_mathml", "<ci>date</ci>");
			final ObjectNode timeNode = objectMapper.createObjectNode().put("id", id).set("units", unitsNode);
			asset.getSemantics().getOde().setTime(timeNode);
		}
		return super.createAsset(asset, projectId, hasWritePermission);
	}

	@Override
	@Observed(name = "function_profile")
	public Optional<Model> updateAsset(
		final Model asset,
		final UUID projectId,
		final Schema.Permission hasWritePermission
	) throws IOException, IllegalArgumentException {
		if (asset.getHeader() != null && asset.getHeader().getName() != null) {
			asset.setName(asset.getHeader().getName());
		}

		// Force proper annotation metadata
		final ModelMetadata metadata = asset.getMetadata();
		if (metadata.getAnnotations() == null) {
			metadata.setAnnotations(new Annotations());
			asset.setMetadata(metadata);
		}

		return super.updateAsset(asset, projectId, hasWritePermission);
	}

	/**
	 * Fix Greek letters in a Petri Net AMR to be displayed correctly in the name of the variables.
	 */
	public void fixGreekLetters(Model model) {
		// Only edit Petri Net models
		if (!model.isPetrinet()) return;

		// States
		model
			.getStates()
			.forEach(state -> {
				final String englishName = state.getName() != null ? state.getName() : state.getId();
				if (englishName != null) {
					state.setName(GreekDictionary.englishToGreek(englishName));
				}
			});

		// Transitions
		model
			.getTransitions()
			.forEach(transition -> {
				final String englishName = transition.getName() != null ? transition.getName() : transition.getId();
				if (englishName != null) {
					transition.setName(GreekDictionary.englishToGreek(englishName));
				}
			});

		// Parameters
		model
			.getParameters()
			.forEach(param -> {
				final String englishName = param.getName() != null ? param.getName() : param.getId();
				if (englishName != null) {
					param.setName(GreekDictionary.englishToGreek(englishName));
				}
			});
	}
}
