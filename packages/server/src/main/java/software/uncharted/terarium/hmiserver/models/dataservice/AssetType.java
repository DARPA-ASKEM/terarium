package software.uncharted.terarium.hmiserver.models.dataservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import software.uncharted.terarium.hmiserver.annotations.TSModel;

import java.util.Arrays;

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
	DOCUMENT
}
