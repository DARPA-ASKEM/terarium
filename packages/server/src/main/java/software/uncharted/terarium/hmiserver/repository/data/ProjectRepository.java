package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.models.dataservice.project.ProjectAndAssetAggregate;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ProjectRepository extends PSCrudRepository<Project, UUID>, JpaSpecificationExecutor<Project> {
	List<Project> findAllByIdInAndDeletedOnIsNull(final List<UUID> ids);

	Optional<Project> getByIdAndDeletedOnIsNull(final UUID id);

	@Query(
		"""
			select
				p.id as id,
				p.createdOn as createdOn,
				p.updatedOn as updatedOn,
				p.deletedOn as deletedOn,
				p.description as description,
				p.fileNames as fileNames,
				p.name as name,
				p.overviewContent as overviewContent,
				p.publicAsset as publicAsset,
				p.sampleProject as sampleProject,
				p.temporary as temporary,
				p.thumbnail as thumbnail,
				p.userId as userId,
				p2.assetCount as assetCount,
				p2.assetType as assetType
			from
				Project p
			left join (
				select
					pa.project.id as projectId,
					pa.assetType as assetType,
					count(*) as assetCount
				from
					ProjectAsset pa
				where
					pa.deletedOn is null
				group by pa.project.id, pa.assetType) as p2
			on p.id = p2.projectId
			where
				p.id in (:ids)
				and p.deletedOn is null
		"""
	)
	List<ProjectAndAssetAggregate> findByIdsWithAssets(@Param("ids") final List<UUID> ids);

	@Query(
		"""
			select
				p.id as id,
				p.createdOn as createdOn,
				p.updatedOn as updatedOn,
				p.deletedOn as deletedOn,
				p.description as description,
				p.fileNames as fileNames,
				p.name as name,
				p.overviewContent as overviewContent,
				p.publicAsset as publicAsset,
				p.sampleProject as sampleProject,
				p.temporary as temporary,
				p.thumbnail as thumbnail,
				p.userId as userId,
				p2.assetCount as assetCount,
				p2.assetType as assetType
			from
				Project p
			left join (
				select
					pa.project.id as projectId,
					pa.assetType as assetType,
					count(*) as assetCount
				from
					ProjectAsset pa
				where
					pa.deletedOn is null
				group by pa.project.id, pa.assetType) as p2
			on p.id = p2.projectId
			where
				p.deletedOn is null
		"""
	)
	List<ProjectAndAssetAggregate> findWithAssets();

	@Query(value = "SELECT public_asset FROM project WHERE id = :id", nativeQuery = true)
	Optional<Boolean> findPublicAssetByIdNative(@Param("id") UUID id);

	List<Project> findByDeletedOnIsNull();
}
