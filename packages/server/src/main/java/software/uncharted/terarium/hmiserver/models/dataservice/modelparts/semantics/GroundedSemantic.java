package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.semantics;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;
import software.uncharted.terarium.hmiserver.models.dataservice.modelparts.ModelGrounding;

@JsonPropertyOrder({ "id", "name", "description", "grounding" })
public interface GroundedSemantic {
	String getId();
	void setId(String id);

	@TSOptional
	ModelGrounding getGrounding();

	@TSOptional
	void setGrounding(ModelGrounding grounding);

	@TSOptional
	String getName();

	@TSOptional
	void setName(String name);

	@TSOptional
	String getDescription();

	@TSOptional
	void setDescription(String description);
}
