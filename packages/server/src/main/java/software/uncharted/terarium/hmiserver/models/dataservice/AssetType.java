package software.uncharted.terarium.hmiserver.models.dataservice;

import software.uncharted.terarium.hmiserver.annotations.TSModel;

@TSModel
public enum AssetType {
	DATASET,
	MODEL_CONFIGURATION,
	MODEL,
	PUBLICATION,
	SIMULATION,
	WORKFLOW,
	ARTIFACT,
	CODE,
	DOCUMENT,
	NETCDF
}
