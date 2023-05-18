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
public class DatasetAnnotatedGeo extends DatasetAnnotatedField{
	@JsonAlias("geo_type")
	private String geoType;

	@JsonAlias("primary_geo")
	private Boolean primaryGeo;

	@JsonAlias("resolve_to_gadm")
	private Boolean resolveToGadm;

	@JsonAlias("is_geo_pair")
	private String isGeoPair; // why isn't this a boolean?

	@JsonAlias("coord_format")
	private String coordFormat;

	@JsonAlias("gadm_level")
	private String gadmLevel;

}
