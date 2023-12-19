package software.uncharted.terarium.hmiserver.repository.data;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ResourceType;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ProjectAssetRepository extends PSCrudRepository<ProjectAsset, UUID> {

	List<ProjectAsset> findAllByProjectId(@NotNull UUID projectId);

	List<ProjectAsset> findAllByProjectIdAndResourceTypeInAndDeletedOnIsNull(@NotNull UUID projectId,
			Collection<@NotNull ResourceType> resourceType);

	ProjectAsset findByProjectIdAndResourceIdAndResourceType(@NotNull UUID projectId, @NotNull UUID resourceId,
			@NotNull ResourceType type);

}
