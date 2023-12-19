package software.uncharted.terarium.hmiserver.repository.data;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectAssetRepository extends PSCrudRepository<ProjectAsset, UUID> {

	List<ProjectAsset> findAllByProjectId(@NotNull UUID projectId);

	List<ProjectAsset> findAllByProjectIdAndAssetTypeInAndDeletedOnIsNull(@NotNull UUID projectId,
			Collection<@NotNull AssetType> assetType);

	ProjectAsset findByProjectIdAndAssetIdAndAssetType(@NotNull UUID projectId, @NotNull UUID assetId,
			@NotNull AssetType type);

}
