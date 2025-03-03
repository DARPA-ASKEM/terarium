package software.uncharted.terarium.hmiserver.aspects;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.annotations.HasProjectAccess;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
@HasProjectAccess
public class HasProjectAccessAspectClassTestService {

	public void defaultParameterName(String projectId) {}

	@HasProjectAccess("#id")
	public void methodOverride(String id) {}

	@HasProjectAccess(level = Schema.Permission.WRITE)
	public void ownerLevel(String projectId) {}
}
