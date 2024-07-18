package software.uncharted.terarium.hmiserver.repository.data;

import jakarta.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ProjectAssetRepository extends PSCrudRepository<ProjectAsset, UUID> {
	ProjectAsset findByProjectIdAndAssetId(@NotNull UUID projectId, @NotNull UUID assetId);

	List<ProjectAsset> findAllByProjectIdAndDeletedOnIsNullAndTemporaryFalse(@NotNull UUID projectId);

	List<ProjectAsset> findAllByProjectIdAndAssetTypeInAndDeletedOnIsNullAndTemporaryFalse(
		@NotNull UUID projectId,
		Collection<@NotNull AssetType> assetType
	);

	ProjectAsset findByProjectIdAndAssetIdAndAssetType(
		@NotNull UUID projectId,
		@NotNull UUID assetId,
		@NotNull AssetType type
	);

	ProjectAsset findByAssetNameAndAssetTypeAndDeletedOnIsNull(@NotNull String assetName, @NotNull AssetType type);

	ProjectAsset findByProjectIdAndAssetNameAndAssetTypeAndDeletedOnIsNull(
		@NotNull UUID projectId,
		@NotNull String assetName,
		@NotNull AssetType type
	);

	List<ProjectAsset> findByAssetId(UUID assetId);
}
