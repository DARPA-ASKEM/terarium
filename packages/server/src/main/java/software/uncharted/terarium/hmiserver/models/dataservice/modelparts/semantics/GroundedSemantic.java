package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

public interface GroundedSemantic {
	String getId();
	void setId(String id);

	@TSOptional
	String getName();

	@TSOptional
	void setName(String name);

	@TSOptional
	ModelGrounding getGrounding();

	@TSOptional
	void setGrounding(ModelGrounding grounding);

	@TSOptional
	String getDescription();

	@TSOptional
	void setDescription(String description);
}
