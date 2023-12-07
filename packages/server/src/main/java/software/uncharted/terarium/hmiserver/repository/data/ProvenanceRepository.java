package software.uncharted.terarium.hmiserver.repository.data;

import org.springframework.stereotype.Repository;

import software.uncharted.terarium.hmiserver.models.dataservice.provenance.Provenance;
import software.uncharted.terarium.hmiserver.repository.PSCrudRepository;

@Repository
public interface ProvenanceRepository extends PSCrudRepository<Provenance, String> {

}
