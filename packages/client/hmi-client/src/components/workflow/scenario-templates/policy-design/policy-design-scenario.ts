import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';
import { operation as ModelOp } from '@/components/workflow/ops/model/mod';
import { operation as ModelConfigOp } from '@/components/workflow/ops/model-config/mod';
import { operation as InterventionOp } from '@/components/workflow/ops/intervention-policy/mod';
import { operation as OptimizeOp } from '@/components/workflow/ops/optimize-ciemss/mod';
import { operation as DatasetOp } from '@/components/workflow/ops/dataset/mod';
import { operation as CalibrateOp } from '@/components/workflow/ops/calibrate-ciemss/mod';
import { OperatorNodeSize } from '@/services/workflow';
import _ from 'lodash';

export class PolicyDesignScenario extends BaseScenario {
	public static templateId = 'policy-design';

	public static templateName = 'Policy Design';

	private modelConfigId;

	private interventionPolicyId;

	private datasetId;

	constructor() {
		super();
		this.workflowName = PolicyDesignScenario.templateName;
	}

	toJSON() {
		return {
			templateId: PolicyDesignScenario.templateId,
			workflowName: this.workflowName,
			modelConfigId: this.modelConfigId,
			interventionPolicyId: this.interventionPolicyId,
			datasetId: this.datasetId
		};
	}

	async createWorkflow() {
		let wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON()); // TOM: How does this work?
		wf = this.addAllNodes(wf);
		wf.runDagreLayout();
		return wf.dump();
	}

	getModelConfigId() {
		return this.modelConfigId;
	}

	setModelConfigId(modelConfigId: string) {
		this.modelConfigId = modelConfigId;
	}

	getInterventionPolicyId() {
		return this.interventionPolicyId;
	}

	setInterventionPolicyId(interventionPolicyId: string) {
		this.interventionPolicyId = interventionPolicyId;
	}

	getDatasetId() {
		return this.datasetId;
	}

	setDatasetId(datasetId: string) {
		this.datasetId = datasetId;
	}

	addAllNodes(wf: workflowService.WorkflowWrapper): workflowService.WorkflowWrapper {
		// Add Default Nodes (not calibrate + dataset):
		const POSITION = { x: 0, y: 0 };
		const SIZE = { size: OperatorNodeSize.medium };

		const modelNode = wf.addNode(ModelOp, POSITION, SIZE);

		const modelConfigNode = wf.addNode(ModelConfigOp, POSITION, SIZE);

		const interventionNode = wf.addNode(InterventionOp, POSITION, SIZE);

		const optimizeNode = wf.addNode(OptimizeOp, POSITION, SIZE);

		// Add Edges:
		// Model Config Input
		wf.addEdge(modelNode.id, modelNode.outputs[0].id, modelConfigNode.id, modelConfigNode.inputs[0].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);
		// Intevention Input
		wf.addEdge(modelNode.id, modelNode.outputs[0].id, interventionNode.id, interventionNode.inputs[0].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);

		// If a dataset is provided we will create the dataset node as well as the calibrate node.
		if (!_.isEmpty(this.datasetId)) {
			const datasetNode = wf.addNode(DatasetOp, POSITION, SIZE);

			const calibrateNode = wf.addNode(CalibrateOp, POSITION, SIZE);

			// Calibrate Inputs:
			wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, calibrateNode.id, calibrateNode.inputs[0].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);

			wf.addEdge(datasetNode.id, datasetNode.outputs[0].id, calibrateNode.id, calibrateNode.inputs[1].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);

			// Optimize Model Config
			wf.addEdge(calibrateNode.id, calibrateNode.outputs[0].id, optimizeNode.id, optimizeNode.inputs[0].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);
		} else {
			// Optimize Model Config
			wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, optimizeNode.id, optimizeNode.inputs[0].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);
		}

		// Optimize Dataset:
		wf.addEdge(interventionNode.id, interventionNode.outputs[0].id, optimizeNode.id, optimizeNode.inputs[1].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);

		// Manage States:

		return wf;
	}
}
