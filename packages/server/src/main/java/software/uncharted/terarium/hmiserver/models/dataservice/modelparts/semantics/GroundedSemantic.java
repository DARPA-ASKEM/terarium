package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

public interface GroundedSemantic {
	String getId();
	String getName();
	ModelGrounding getGrounding();
	void setId(String id);
	void setName(String name);
	void setGrounding(ModelGrounding grounding);
}
