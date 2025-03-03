package software.uncharted.terarium.hmiserver.aspects;

import org.springframework.stereotype.Service;
import software.uncharted.terarium.hmiserver.annotations.HasProjectAccess;
import software.uncharted.terarium.hmiserver.models.dataservice.project.Project;
import software.uncharted.terarium.hmiserver.utils.rebac.Schema;

@Service
public class HasProjectAccessAspectMethodTestService {

	@HasProjectAccess
	public void defaultParameterName(String projectId) {}

	@HasProjectAccess("#id")
	public void customParameterName(String id) {}

	@HasProjectAccess(level = Schema.Permission.WRITE)
	public void ownerLevel(String projectId) {}

	@HasProjectAccess(level = Schema.Permission.WRITE, value = "#id")
	public void ownerLevelWithCustomParameterName(String id) {}

	@HasProjectAccess
	public void incorrectSpel(String id) {}

	@HasProjectAccess("#id")
	public void incorrectSpel2(String projectId) {}

	@HasProjectAccess("jladjklsdjkl")
	public void incorrectSpel3(String projectId) {}

	@HasProjectAccess("#p.getId()")
	public void objectSpel(Project p) {}
}
