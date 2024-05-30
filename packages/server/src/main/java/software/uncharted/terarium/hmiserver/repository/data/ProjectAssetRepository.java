package software.uncharted.terarium.hmiserver.repository.data;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.validation.constraints.NotNull;
import software.uncharted.terarium.hmiserver.models.dataservice.AssetType;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAsset;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ProjectAssetRepository extends PSCrudRepository<ProjectAsset, UUID> {

	ProjectAsset findByProjectIdAndAssetId(@NotNull UUID projectId, @NotNull UUID assetId);

	List<ProjectAsset> findAllByProjectId(@NotNull UUID projectId);

	List<ProjectAsset> findAllByProjectIdAndAssetTypeInAndDeletedOnIsNullAndTemporaryFalse(
			@NotNull UUID projectId, Collection<@NotNull AssetType> assetType);

	ProjectAsset findByProjectIdAndAssetIdAndAssetType(
			@NotNull UUID projectId, @NotNull UUID assetId, @NotNull AssetType type);

	ProjectAsset findByAssetNameAndAssetTypeAndDeletedOnIsNull(@NotNull String assetName, @NotNull AssetType type);

	ProjectAsset findByProjectIdAndAssetNameAndAssetTypeAndDeletedOnIsNull(
			@NotNull UUID projectId, @NotNull String assetName, @NotNull AssetType type);

	List<ProjectAsset> findByAssetId(UUID assetId);

	@Query("SELECT p FROM ProjectAsset p WHERE p.assetId IN (SELECT p2.assetId FROM ProjectAsset p2 WHERE p2.deletedOn IS NULL GROUP BY p2.assetId HAVING COUNT(p2.assetId) > 1)")
	List<ProjectAsset> findNonUniqueAssetId();

}
