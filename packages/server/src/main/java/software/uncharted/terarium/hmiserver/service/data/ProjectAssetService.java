package software.uncharted.terarium.hmiserver.service.data;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.models.data.project.ResourceType;
import software.uncharted.terarium.hmiserver.models.data.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.repository.data.ProjectAssetRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectAssetService {

	final ProjectAssetRepository projectAssetRepository;

	public List<ProjectAsset> findAllByProjectId(@NotNull UUID projectId) { return projectAssetRepository.findAllByProjectId(projectId);}

	public List<ProjectAsset> findActiveAssetsForProject(@NotNull UUID projectId, Collection<@NotNull ResourceType> types) {
		return projectAssetRepository.findAllByProjectIdAndResourceTypeInAndDeletedOnIsNull(projectId, types);
	}

	public ProjectAsset save(final ProjectAsset asset){
		return projectAssetRepository.save(asset);
	}

	public ProjectAsset findByProjectIdAndResourceIdAndResourceType(@NotNull UUID projectId,@NotNull String resourceId, @NotNull ResourceType type){
		return projectAssetRepository.findByProjectIdAndResourceIdAndResourceType(projectId, resourceId, type);
	}

}
