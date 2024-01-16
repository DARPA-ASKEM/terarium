package software.uncharted.terarium.hmiserver.service.data;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectAssetService {

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

	public List<ProjectAsset> findActiveAssetsForProject(@NotNull final UUID projectId,
			final Collection<@NotNull AssetType> types) {
		return projectAssetRepository.findAllByProjectIdAndAssetTypeInAndDeletedOnIsNull(projectId, types);
	}

	public ProjectAsset save(final ProjectAsset asset) {
		return projectAssetRepository.save(asset);
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

	private boolean populateProjectAssetFields(ProjectAsset projectAsset, AssetType assetType, UUID id)
			throws IOException {
		switch (assetType) {
			case DATASET:
				final Optional<Dataset> dataset = datasetService.getDataset(id);
				if (dataset.isPresent()) {
					projectAsset.setAssetName(dataset.get().getName());
				}
				return dataset.isPresent();
			case MODEL:
				final Optional<Model> model = modelService.getModel(id);
				if (model.isPresent()) {
					projectAsset.setAssetName(model.get().getHeader().getName());
				}
				return model.isPresent();
			case DOCUMENT:
				final Optional<DocumentAsset> document = documentService.getDocumentAsset(id);
				if (document.isPresent()) {
					projectAsset.setAssetName(document.get().getName());
				}
				return document.isPresent();
			case WORKFLOW:
				final Optional<Workflow> workflow = workflowService.getWorkflow(id);
				if (workflow.isPresent()) {
					projectAsset.setAssetName(workflow.get().getName());
				}
				return workflow.isPresent();
			case PUBLICATION:
				final Optional<ExternalPublication> publication = publicationService.getExternalPublication(id);
				if (publication.isPresent()) {
					projectAsset.setAssetName(publication.get().getTitle());
				}
				return publication.isPresent();
			case CODE:
				final Optional<Code> code = codeService.getCode(id);
				if (code.isPresent()) {
					projectAsset.setAssetName(code.get().getName());
				}
				return code.isPresent();
			case ARTIFACT:
				final Optional<Artifact> artifact = artifactService.getArtifact(id);
				if (artifact.isPresent()) {
					projectAsset.setAssetName(artifact.get().getName());
				}
				return artifact.isPresent();
			default:
				break;
		}
		return false;
	}

	public Optional<ProjectAsset> createProjectAsset(Project project, final AssetType assetType, final UUID assetId)
			throws IOException {

		final ProjectAsset asset = new ProjectAsset();
		if (project.getProjectAssets() == null) {
			project.setProjectAssets(List.of(asset));
		} else {
			project.getProjectAssets().add(asset);
		}
		if (!populateProjectAssetFields(asset, assetType, assetId)) {
			// underlying asset does not exist
			return Optional.empty();
		}
		asset.setAssetType(assetType);
		asset.setAssetId(assetId);
		asset.setProject(project);
		asset.setCreatedOn(Timestamp.from(Instant.now()));

		return Optional.of(projectAssetRepository.save(asset));
	}

	public Optional<ProjectAsset> updateProjectAsset(final ProjectAsset asset) {
		if (!projectAssetRepository.existsById(asset.getId())) {
			return Optional.empty();
		}
		return Optional.of(projectAssetRepository.save(asset));
	}

}
