package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

public interface GroundedSemantic {
	String getId();

	@TSOptional
	String getName();

	@TSOptional
	ModelGrounding getGrounding();

	@TSOptional
	String getDescription();

	void setId(String id);

	@TSOptional
	void setName(String name);

	@TSOptional
	void setGrounding(ModelGrounding grounding);

	@TSOptional
	void setDescription(String description);
}
