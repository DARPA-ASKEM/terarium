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

	toJSON() {
		return {
			templateId: SituationalAwarenessScenario.templateId,
			workflowName: this.workflowName,
			modelSpec: this.modelSpec,
			datasetSpec: this.datasetSpec,
			modelConfigSpec: this.modelConfigSpec,
			calibrateSpec: this.calibrateSpec
		};
	}

	createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON());

		// Add nodes
		// Model
		const modelNode = wf.addNode(
			ModelOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// Dataset
		const datasetNode = wf.addNode(
			DatasetOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// Model Configuration
		const modelConfigNode = wf.addNode(
			ModelConfigOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// Calibrate
		const calibrateNode = wf.addNode(
			CalibrateCiemssOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// Add edges
		wf.addEdge(modelNode.id, modelNode.outputs[0].id, modelConfigNode.id, modelConfigNode.inputs[0].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);
		wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, calibrateNode.id, calibrateNode.inputs[0].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);
		wf.addEdge(datasetNode.id, datasetNode.outputs[0].id, calibrateNode.id, calibrateNode.inputs[1].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);

		wf.runDagreLayout();

		return wf.dump();
	}
}
