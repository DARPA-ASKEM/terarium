import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';
import { operation as ModelOp } from '@/components/workflow/ops/model/mod';
import { operation as ModelConfigOp } from '@/components/workflow/ops/model-config/mod';
import { operation as CalibrateCiemssOp } from '@/components/workflow/ops/calibrate-ciemss/mod';
import { operation as DatasetOp } from '@/components/workflow/ops/dataset/mod';
import { OperatorNodeSize } from '@/services/workflow';

export class SituationalAwarenessScenario extends BaseScenario {
	public static templateId = 'situational-awareness';

	public static templateName = 'Situational Awareness';

	modelSpec: { id: string };

	datasetSpec: { id: string };

	modelConfigSpec: { id: string };

	calibrateSpec: { ids: string[] };

	constructor() {
		super();
		this.workflowName = 'Situational Awareness';
		this.modelSpec = {
			id: ''
		};
		this.datasetSpec = {
			id: ''
		};
		this.modelConfigSpec = {
			id: ''
		};
		this.calibrateSpec = {
			ids: []
		};
	}

	setModelSpec(id: string) {
		this.modelSpec.id = id;
		this.modelConfigSpec.id = '';
		this.calibrateSpec.ids = [];
	}

	setDatasetSpec(id: string) {
		this.datasetSpec.id = id;
	}

	setModelConfigSpec(id: string) {
		this.modelConfigSpec.id = id;
	}

	setCalibrateSpec(ids: string[]) {
		this.calibrateSpec.ids = ids;
	}

	createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();

		// Model
		wf.addNode(
			ModelOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// Dataset
		wf.addNode(
			DatasetOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// Model Configuration
		wf.addNode(
			ModelConfigOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// Calibrate
		wf.addNode(
			CalibrateCiemssOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		const workflow = wf.dump();
		workflow.name = this.workflowName;

		return workflow;
	}
}
