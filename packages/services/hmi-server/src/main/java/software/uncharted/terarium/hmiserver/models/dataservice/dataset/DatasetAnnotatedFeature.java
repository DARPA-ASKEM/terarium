package software.uncharted.terarium.hmiserver.models.dataservice.dataset;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain=true)
@TSModel
public class DatasetAnnotatedFeature extends DatasetAnnotatedField{
	@JsonAlias("feature_type")
	private String featureType;

	private String units;

	@JsonAlias("units_description")
	private String unitsDescription;

	@JsonAlias("primary_ontology_id")
	private String primaryOntologyId;

	@JsonAlias("qualifierrole") // How awful is this...
	private String qualifierRole;
}
