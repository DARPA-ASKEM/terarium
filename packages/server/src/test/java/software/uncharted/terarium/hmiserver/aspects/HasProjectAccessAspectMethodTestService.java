package software.uncharted.terarium.hmiserver.aspects;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.annotations.HasProjectAccess;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
public class HasProjectAccessAspectMethodTestService {

	@HasProjectAccess
	public void defaultParameterName(String id) {}

	@HasProjectAccess("#projectId")
	public void customParameterName(String projectId) {}

	@HasProjectAccess(level = Schema.Permission.WRITE)
	public void ownerLevel(String id) {}

	@HasProjectAccess(level = Schema.Permission.WRITE, value = "#projectId")
	public void ownerLevelWithCustomParameterName(String projectId) {}

	@HasProjectAccess
	public void incorrectSpel(String projectId) {}

	@HasProjectAccess("#projectId")
	public void incorrectSpel2(String id) {}

	@HasProjectAccess("jladjklsdjkl")
	public void incorrectSpel3(String id) {}

	@HasProjectAccess("#p.getId()")
	public void objectSpel(Project p) {}
}
