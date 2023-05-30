package software.uncharted.terarium.hmiserver.models.dataservice.modelparts.metadata;

import lombok.Data;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;


import java.util.List;

@Data
@Accessors(chain = true)
public class Variable {
	private String id;
	private String name;
	private List<VariableMetadata> metadata;

	@JsonAlias("dkg_groundings")
	@JsonSetter("dkg_groundings")
	private List<DKGConcept> dkgGroundings;

	private List<DataColumn> column;

	private Paper paper;

	private List<Equation> equations;
}

