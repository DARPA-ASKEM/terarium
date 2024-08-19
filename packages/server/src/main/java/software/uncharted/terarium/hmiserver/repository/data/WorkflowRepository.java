package software.uncharted.terarium.hmiserver.repository.data;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

@Repository
public interface WorkflowRepository extends PSCrudSoftDeleteRepository<Workflow, UUID> {
	/**
	 * Find all workflows who have at least one edge or node marked for deletion
	 */
	@Query(
		value = "SELECT w.* FROM Workflow w " +
		" cross join json_array_elements(edges) as edgesData" +
		" where edgesData->>'isDeleted' = 'true'",
		nativeQuery = true
	)
	List<Workflow> findEdgesToBeDeleted();

	/**
	 * Find all workflows who have at least one edge or node marked for deletion
	 */
	@Query(
		value = "SELECT w.* FROM Workflow w " +
		" cross join json_array_elements(nodes) as nodesData" +
		" where nodesData->>'isDeleted' = 'true'",
		nativeQuery = true
	)
	List<Workflow> findNodesToBeDeleted();
}
