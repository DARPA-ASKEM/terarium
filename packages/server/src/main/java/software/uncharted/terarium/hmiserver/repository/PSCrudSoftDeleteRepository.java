package software.uncharted.terarium.hmiserver.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import software.uncharted.terarium.hmiserver.models.dataservice.model.ModelFramework;

@NoRepositoryBean
public interface PSCrudSoftDeleteRepository<T, ID> extends PSCrudRepository<T, ID> {
	@Query(value = "SELECT project_id FROM project_asset WHERE asset_id = :assetId", nativeQuery = true)
	UUID getProjectIdForAsset(@Param("assetId") final UUID assetId);

	List<T> findAllByIdInAndDeletedOnIsNull(final List<ID> ids);

	Optional<T> getByIdAndDeletedOnIsNull(final ID id);

	Page<T> findAllByPublicAssetIsTrueAndTemporaryIsFalseAndDeletedOnIsNull(final Pageable pageable);

	List<ModelFramework> findAllByDeletedOnIsNull();
}
