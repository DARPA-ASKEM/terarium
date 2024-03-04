package software.uncharted.terarium.hmiserver.models.dataservice.workflow;

public enum WorkflowOperationTypes {
	/*
	JAva implementation of the ts enum:
	ADD = 'add', // temp for test to work
	TEST = 'TestOperation',
	CALIBRATION_JULIA = 'CalibrationOperationJulia',
	CALIBRATION_CIEMSS = 'CalibrationOperationCiemss',
	DATASET = 'Dataset',
	MODEL = 'ModelOperation',
	SIMULATE_JULIA = 'SimulateJuliaOperation',
	SIMULATE_CIEMSS = 'SimulateCiemssOperation',
	STRATIFY_JULIA = 'StratifyJulia',
	STRATIFY_MIRA = 'StratifyMira',
	SIMULATE_ENSEMBLE_CIEMSS = 'SimulateEnsembleCiemms',
	CALIBRATE_ENSEMBLE_CIEMSS = 'CalibrateEnsembleCiemms',
	DATASET_TRANSFORMER = 'DatasetTransformer',
	MODEL_TRANSFORMER = 'ModelTransformer',
	MODEL_FROM_CODE = 'ModelFromCode',
	FUNMAN = 'Funman',
	CODE = 'Code',
	MODEL_CONFIG = 'ModelConfiguration',
	MODEL_OPTIMIZE = 'ModelOptimize',
	MODEL_COUPLING = 'ModelCoupling',
	MODEL_EDIT = 'ModelEdit',
	DOCUMENT = 'Document'
	 */

	ADD("add"),
	TEST("TestOperation"),
	CALIBRATION_JULIA("CalibrationOperationJulia"),
	CALIBRATION_CIEMSS("CalibrationOperationCiemss"),
	DATASET("Dataset"),
	MODEL("ModelOperation"),
	SIMULATE_JULIA("SimulateJuliaOperation"),
	SIMULATE_CIEMSS("SimulateCiemssOperation"),
	STRATIFY_JULIA("StratifyJulia"),
	STRATIFY_MIRA("StratifyMira"),
	SIMULATE_ENSEMBLE_CIEMSS("SimulateEnsembleCiemms"),
	CALIBRATE_ENSEMBLE_CIEMSS("CalibrateEnsembleCiemms"),
	DATASET_TRANSFORMER("DatasetTransformer"),
	MODEL_TRANSFORMER("ModelTransformer"),
	MODEL_FROM_CODE("ModelFromCode"),
	FUNMAN("Funman"),
	CODE("Code"),
	MODEL_CONFIG("ModelConfiguration"),
	MODEL_OPTIMIZE("ModelOptimize"),
	MODEL_COUPLING("ModelCoupling"),
	MODEL_EDIT("ModelEdit"),
	DOCUMENT("Document");

	private final String value;

	WorkflowOperationTypes(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
