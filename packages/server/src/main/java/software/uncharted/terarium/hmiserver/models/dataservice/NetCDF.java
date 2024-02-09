package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import software.uncharted.terarium.hmiserver.annotations.TSModel;
import software.uncharted.terarium.hmiserver.annotations.TSOptional;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TSModel
public class NetCDF extends TerariumAsset {
	/* UserId of who created this asset */
	private String userId;

	/* The name of the asset. */
	private String name;

	@TSOptional
	@JsonAlias("file_names")
	private List<String> fileNames;

	/* TODO: read NetCDF File for "Global Attributes" */
//	private String Conventions;
//	private String activity_id;
//	private String creation_date;
//	private String data_specs_version;
//	private String experiment;
//	private String experiment_id;
//	private String forcing_index;
//	private String frequency;
//	private String further_info_url;
//	private String grid;
//	private String grid_label;
//	private String initialization_index;
//	private String institution;
//	private String institution_id;
//	private String license;
//	private String mip_era;
//	private String nominal_resolution;
//	private String physics_index;
//	private String product;
//	private String realization_index;
//	private String realm;
//	private String source;
//	private String source_id;
//	private String source_type;
//	private String sub_experiment;
//	private String sub_experiment_id;
//	private String table_id;
//	private String tracking_id;
//	private String variable_id;
//	private String variant_label;
}
