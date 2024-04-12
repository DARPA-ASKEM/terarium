package software.uncharted.terarium.hmiserver.repository.data;

import java.util.UUID;

import org.springframework.stereotype.Repository;

import software.uncharted.terarium.hmiserver.models.dataservice.workflow.Workflow;
import software.uncharted.terarium.hmiserver.repository.PSCrudSoftDeleteRepository;

@Repository
public interface WorkflowRepository extends PSCrudSoftDeleteRepository<Workflow, UUID> {

}
