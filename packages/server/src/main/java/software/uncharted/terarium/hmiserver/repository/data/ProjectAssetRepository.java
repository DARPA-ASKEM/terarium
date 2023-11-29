package software.uncharted.terarium.hmiserver.repository.data;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.data.project.ResourceType;
import software.uncharted.terarium.hmiserver.models.data.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectAssetRepository extends PSCrudRepository<ProjectAsset, UUID> {

	List<ProjectAsset> findAllByProjectId(@NotNull UUID projectId);

	List<ProjectAsset> findAllByProjectIdAndResourceTypeInAndDeletedOnIsNull(@NotNull UUID projectId, Collection<@NotNull ResourceType> resourceType);

	ProjectAsset findByProjectIdAndResourceIdAndResourceType(@NotNull UUID projectId,@NotNull String resourceId, @NotNull ResourceType type);

}
