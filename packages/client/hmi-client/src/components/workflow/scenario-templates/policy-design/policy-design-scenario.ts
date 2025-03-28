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
import { getModelConfigurationById } from '@/services/model-configurations';
import { getInterventionPolicyById } from '@/services/intervention-policy';
import { ModelConfiguration } from '@/types/Types';
import { ChartSetting, ChartSettingType } from '@/types/common';
import { updateChartSettingsBySelectedVariables } from '@/services/chart-settings';
import {
	InterventionPolicyGroupForm,
	OptimizeCiemssOperationState
} from '../../ops/optimize-ciemss/optimize-ciemss-operation';
import { setInterventionPolicyGroups } from '../../ops/optimize-ciemss/optimize-utils';

export class PolicyDesignScenario extends BaseScenario {
	public static templateId = 'policy-design';

	public static templateName = 'Policy Design';

	private modelId;

	private modelConfigId;

	private interventionPolicyId;

	private datasetId;

	private optimizeState: OptimizeCiemssOperationState;

	constructor() {
		super();
		this.workflowName = PolicyDesignScenario.templateName;
		this.optimizeState = OptimizeOp.initState!();
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

	isValid(): boolean {
		return !!this.workflowName && !!this.modelId && !!this.modelConfigId && !!this.interventionPolicyId;
	}

	async createWorkflow() {
		let wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON());
		wf = await this.addAllNodes(wf);
		wf.runDagreLayout();
		return wf.dump();
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

	getInterventionPolicyId() {
		return this.interventionPolicyId;
	}

	async setInterventionPolicyId(interventionPolicyId: string, modelConfiguration?: ModelConfiguration) {
		this.interventionPolicyId = interventionPolicyId;
		// Update optimize state if able:
		if (modelConfiguration) {
			const state = this.optimizeState;
			const interventionPolicy = await getInterventionPolicyById(interventionPolicyId);
			if (interventionPolicy) {
				this.optimizeState.interventionPolicyGroups = setInterventionPolicyGroups(
					state,
					interventionPolicy,
					modelConfiguration
				).interventionPolicyGroups;
			}
		}
	}

	getDatasetId() {
		return this.datasetId;
	}

	setDatasetId(datasetId: string) {
		this.datasetId = datasetId;
	}

	getOptimizeState() {
		return this.optimizeState;
	}

	updateInterventionPolicyGroupForm(index: number, config: InterventionPolicyGroupForm) {
		this.optimizeState.interventionPolicyGroups[index] = config;
	}

	getOptimizeOutputSettings() {
		console.log(this.optimizeState.chartSettings);
		return this.optimizeState.chartSettings?.flatMap((chartSettings) => chartSettings.selectedVariables) ?? [];
	}

	setOptimizeOutputSettings(selectedVariables: string[]) {
		let optimizeChartSettings: ChartSetting[] = [];
		optimizeChartSettings = updateChartSettingsBySelectedVariables(
			optimizeChartSettings,
			ChartSettingType.VARIABLE,
			selectedVariables
		);
		this.optimizeState.chartSettings = optimizeChartSettings;
	}

	private async addAllNodes(wf: workflowService.WorkflowWrapper): Promise<workflowService.WorkflowWrapper> {
		// Add Default Nodes (not calibrate + dataset):
		const POSITION = { x: 0, y: 0 };
		const SIZE = { size: OperatorNodeSize.medium };
		const modelConfig = await getModelConfigurationById(this.modelConfigId);
		const interventionPolicy = await getInterventionPolicyById(this.interventionPolicyId);

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

		// Optimize Dataset:
		wf.addEdge(interventionNode.id, interventionNode.outputs[0].id, optimizeNode.id, optimizeNode.inputs[1].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);

		// Manage States:
		// Model:
		wf.updateNode(modelNode, {
			state: {
				modelId: this.modelId
			},
			output: {
				value: [this.modelId]
			}
		});

		// Model Config:
		wf.updateNode(modelConfigNode, {
			state: {
				transientModelConfig: modelConfig
			},
			output: {
				value: [this.modelConfigId],
				state: _.omit(modelConfigNode.state, ['transientModelConfig'])
			}
		});

		// Intervention:
		wf.updateNode(interventionNode, {
			state: {
				interventionPolicy
			},
			output: {
				value: [this.interventionPolicyId],
				state: interventionNode.state
			}
		});

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

			// Dataset state:
			wf.updateNode(datasetNode, {
				state: {
					datasetId: this.datasetId
				},
				output: {
					value: [this.datasetId]
				}
			});
		} else {
			// Optimize Model Config
			wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, optimizeNode.id, optimizeNode.inputs[0].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);
		}

		wf.updateNode(optimizeNode, {
			state: this.optimizeState
		});

		return wf;
	}
}
