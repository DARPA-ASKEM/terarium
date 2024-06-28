package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ProjectRepository extends PSCrudRepository<Project, UUID>, JpaSpecificationExecutor<Project> {

	List<Project> findAllByIdInAndDeletedOnIsNull(final List<UUID> ids);

	Optional<Project> getByIdAndDeletedOnIsNull(final UUID id);

	@Query("select "
			+ " p.id, "
			+ " p.createdOn, "
			+ " p.deletedOn, "
			+ " p.description, "
			+ " p.fileNames, "
			+ " p.name, "
			+ " p.overviewContent, "
			+ " p.publicAsset, "
			+ " p.temporary, "
			+ " p.updatedOn, "
			+ " p.thumbnail, "
			+ " p.userId, "
			+ " p2.assetCount, "
			+ " p2.assetType "
			+ "from "
			+ " Project p "
			+ "left join ("
			+ "select "
			+ " pa.project.id as projectId, "
			+ " pa.assetType as assetType, "
			+ " count(*) as assetCount "
			+ "from "
			+ " ProjectAsset pa "
			+ "where "
			+ " pa.deletedOn is null "
			+ "group by pa.project.id, pa.assetType "
			+ ") as p2 "
			+ "on p.id = p2.projectId "
			+ "where "
			+ " p.id in (:ids) "
			+ " and p.deletedOn is null")
	List<Project> findByIdsWithAssets(final List<UUID> ids);
}
