package software.uncharted.terarium.hmiserver.service.data;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.dataservice.Artifact;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.code.Code;
import software.uncharted.terarium.hmiserver.models.dataservice.dataset.Dataset;
import software.uncharted.terarium.hmiserver.models.dataservice.document.DocumentAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.externalpublication.ExternalPublication;
import software.uncharted.terarium.hmiserver.models.dataservice.model.Model;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.repository.data.ProjectAssetRepository;
import software.uncharted.terarium.hmiserver.repository.data.ProjectRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectAssetService {

	final ProjectRepository projectRepository;
	final ProjectAssetRepository projectAssetRepository;
	final DatasetService datasetService;
	final ModelService modelService;
	final DocumentAssetService documentService;
	final WorkflowService workflowService;
	final ExternalPublicationService publicationService;
	final CodeService codeService;
	final ArtifactService artifactService;

	public List<ProjectAsset> findAllByProjectId(@NotNull final UUID projectId) {
		return projectAssetRepository.findAllByProjectId(projectId);
	}

	/**
	 * Find all active assets for a project.  Active assets are defined as those that are not deleted and not temporary.
	 * @param projectId
	 * @param types
	 * @return
	 */
	public List<ProjectAsset> findActiveAssetsForProject(@NotNull final UUID projectId,
			final Collection<@NotNull AssetType> types) {
		return projectAssetRepository.findAllByProjectIdAndAssetTypeInAndDeletedOnIsNullAndTemporaryFalse(projectId, types);
	}

	public ProjectAsset save(final ProjectAsset asset) {
		return projectAssetRepository.save(asset);
	}

	public boolean deleteByAssetId(@NotNull final UUID projectId, @NotNull final AssetType type,
			@NotNull final UUID originalAssetId) {
		final ProjectAsset asset = projectAssetRepository
				.findByProjectIdAndAssetIdAndAssetType(projectId, originalAssetId, type);
		if (asset == null) {
			return false;
		}
		asset.setDeletedOn(Timestamp.from(Instant.now()));
		return (save(asset) != null);
	}

	public boolean delete(final UUID id) {
		final ProjectAsset asset = projectAssetRepository.findById(id).orElse(null);
		if (asset == null) {
			return false;
		}
		asset.setDeletedOn(Timestamp.from(Instant.now()));
		return (save(asset) != null);
	}

	public ProjectAsset findByProjectIdAndAssetIdAndAssetType(@NotNull final UUID projectId,
			@NotNull final UUID assetId,
			@NotNull final AssetType type) {
		return projectAssetRepository.findByProjectIdAndAssetIdAndAssetType(projectId, assetId, type);
	}

	private boolean populateProjectAssetFields(final ProjectAsset projectAsset, final AssetType assetType, final UUID id)
			throws IOException {
		switch (assetType) {
			case DATASET:
				final Optional<Dataset> dataset = datasetService.getAsset(id);
				if (dataset.isPresent()) {
					projectAsset.setAssetName(dataset.get().getName());
				}
				return dataset.isPresent();
			case MODEL:
				final Optional<Model> model = modelService.getAsset(id);
				if (model.isPresent()) {
					projectAsset.setAssetName(model.get().getHeader().getName());
				}
				return model.isPresent();
			case DOCUMENT:
				final Optional<DocumentAsset> document = documentService.getAsset(id);
				if (document.isPresent()) {
					projectAsset.setAssetName(document.get().getName());
				}
				return document.isPresent();
			case WORKFLOW:
				final Optional<Workflow> workflow = workflowService.getAsset(id);
				if (workflow.isPresent()) {
					projectAsset.setAssetName(workflow.get().getName());
				}
				return workflow.isPresent();
			case PUBLICATION:
				final Optional<ExternalPublication> publication = publicationService.getAsset(id);
				if (publication.isPresent()) {
					projectAsset.setAssetName(publication.get().getTitle());
				}
				return publication.isPresent();
			case CODE:
				final Optional<Code> code = codeService.getAsset(id);
				if (code.isPresent()) {
					projectAsset.setAssetName(code.get().getName());
				}
				return code.isPresent();
			case ARTIFACT:
				final Optional<Artifact> artifact = artifactService.getAsset(id);
				if (artifact.isPresent()) {
					projectAsset.setAssetName(artifact.get().getName());
				}
				return artifact.isPresent();
			default:
				break;
		}
		return false;
	}

	public Optional<ProjectAsset> createProjectAsset(final Project project, final AssetType assetType, final UUID assetId)
			throws IOException {

		final ProjectAsset asset = new ProjectAsset();
		if (!populateProjectAssetFields(asset, assetType, assetId)) {
			// underlying asset does not exist
			return Optional.empty();
		}
		asset.setAssetType(assetType);
		asset.setProject(project);
		asset.setAssetId(assetId);

		if (project.getProjectAssets() == null) {
			project.setProjectAssets(new ArrayList<>(List.of(asset)));
		} else {
			project.getProjectAssets().add(asset);
		}

		return Optional.of(projectAssetRepository.save(asset));
	}

	public Optional<ProjectAsset> updateProjectAsset(final ProjectAsset asset) {
		if (!projectAssetRepository.existsById(asset.getId())) {
			return Optional.empty();
		}
		return Optional.of(projectAssetRepository.save(asset));
	}

	public Optional<ProjectAsset> getProjectAssetByNameAndType(final String assetName, final AssetType assetType) {
		return Optional
				.ofNullable(projectAssetRepository.findByAssetNameAndAssetTypeAndDeletedOnIsNull(assetName, assetType));
	}

	public Optional<ProjectAsset> getProjectAssetByNameAndTypeAndProjectId(final UUID projectId, final String assetName,
			final AssetType assetType) {
		return Optional.ofNullable(projectAssetRepository
				.findByProjectIdAndAssetNameAndAssetTypeAndDeletedOnIsNull(projectId, assetName, assetType));
	}

}
