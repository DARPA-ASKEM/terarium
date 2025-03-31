import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import { getInterventionPolicyById } from '@/services/intervention-policy';
import { getModelConfigurationById } from '@/services/model-configurations';
import * as workflowService from '@/services/workflow';
import { OperatorNodeSize } from '@/services/workflow';
import { operation as ModelOp } from '@/components/workflow/ops/model/mod';
import { operation as ModelConfigOp } from '@/components/workflow/ops/model-config/mod';
import { operation as InterventionOp } from '@/components/workflow/ops/intervention-policy/mod';
import { operation as DatasetOp } from '@/components/workflow/ops/dataset/mod';
import { operation as CalibrateOp } from '@/components/workflow/ops/calibrate-ciemss/mod';
import { operation as SimulateOp } from '@/components/workflow/ops/simulate-ciemss/mod';
import { operation as CompareDatasetOp } from '@/components/workflow/ops/compare-datasets/mod';

export class CounterfactualScenario extends BaseScenario {
	public static templateId = 'counterfactual';

	public static templateName = 'Necessary or sufficient analysis';

	private modelId: string;

	private modelConfigId: string;

	private interventionPolicyId: string;

	private datasetId: string;

	constructor() {
		super();
		this.workflowName = '';
		this.modelId = '';
		this.modelConfigId = '';
		this.interventionPolicyId = '';
		this.datasetId = '';
	}

	getModelId() {
		return this.modelId;
	}

	setModelId(modelId: string) {
		this.modelId = modelId;
	}

	getModelConfigId() {
		return this.modelConfigId;
	}

	setModelConfigId(modelConfigId: string) {
		this.modelConfigId = modelConfigId;
	}

	getDatasetId() {
		return this.datasetId;
	}

	setDatasetId(datasetId: string) {
		this.datasetId = datasetId;
	}

	getInterventionPolicyId() {
		return this.interventionPolicyId;
	}

	setInterventionPolicyId(interventionPolicyId: string) {
		this.interventionPolicyId = interventionPolicyId;
	}

	toJSON() {
		return {
			templateId: CounterfactualScenario.templateId,
			workflowName: this.workflowName
		};
	}

	async createWorkflow() {
		let wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON());
		wf = await this.addAllNodes(wf);
		wf.runDagreLayout();
		return wf.dump();
	}

	private async addAllNodes(wf: workflowService.WorkflowWrapper): Promise<workflowService.WorkflowWrapper> {
		// Add Default Nodes (not calibrate + dataset):
		const POSITION = { x: 0, y: 0 };
		const SIZE = { size: OperatorNodeSize.medium };
		const modelConfig = await getModelConfigurationById(this.modelConfigId);
		const interventionPolicy = await getInterventionPolicyById(this.interventionPolicyId);

		// Add Nodes:
		const modelNode = wf.addNode(ModelOp, POSITION, SIZE);

		const modelConfigNode = wf.addNode(ModelConfigOp, POSITION, SIZE);

		const interventionNode = wf.addNode(InterventionOp, POSITION, SIZE);

		const datasetNode = wf.addNode(DatasetOp, POSITION, SIZE);

		const calibrateNode = wf.addNode(CalibrateOp, POSITION, SIZE);

		const simulateBaseNode = wf.addNode(SimulateOp, POSITION, SIZE);

		const simulateWithInterventionNode = wf.addNode(SimulateOp, POSITION, SIZE);

		const compareDatasetNode = wf.addNode(CompareDatasetOp, POSITION, SIZE);
		// Add Edges:
		// Model Config
		wf.addEdge(modelNode.id, modelNode.outputs[0].id, modelConfigNode.id, modelConfigNode.inputs[0].id, [
			POSITION,
			POSITION
		]);

		// Calibrate:
		wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, calibrateNode.id, calibrateNode.inputs[0].id, [
			POSITION,
			POSITION
		]);

		wf.addEdge(datasetNode.id, datasetNode.outputs[0].id, calibrateNode.id, calibrateNode.inputs[1].id, [
			POSITION,
			POSITION
		]);

		wf.addEdge(interventionNode.id, interventionNode.outputs[0].id, calibrateNode.id, calibrateNode.inputs[2].id, [
			POSITION,
			POSITION
		]);

		// Simulate:
		wf.addEdge(calibrateNode.id, calibrateNode.outputs[0].id, simulateBaseNode.id, simulateBaseNode.inputs[0].id, [
			POSITION,
			POSITION
		]);

		wf.addEdge(
			calibrateNode.id,
			calibrateNode.outputs[0].id,
			simulateWithInterventionNode.id,
			simulateWithInterventionNode.inputs[0].id,
			[POSITION, POSITION]
		);

		wf.addEdge(
			interventionNode.id,
			interventionNode.outputs[0].id,
			simulateWithInterventionNode.id,
			simulateWithInterventionNode.inputs[1].id,
			[POSITION, POSITION]
		);

		// Compare Dataset Node
		wf.addEdge(
			simulateBaseNode.id,
			simulateBaseNode.outputs[0].id,
			compareDatasetNode.id,
			compareDatasetNode.inputs[0].id,
			[POSITION, POSITION]
		);

		wf.addEdge(
			simulateWithInterventionNode.id,
			simulateWithInterventionNode.outputs[0].id,
			compareDatasetNode.id,
			compareDatasetNode.inputs[1].id,
			[POSITION, POSITION]
		);

		return wf;
	}
}
