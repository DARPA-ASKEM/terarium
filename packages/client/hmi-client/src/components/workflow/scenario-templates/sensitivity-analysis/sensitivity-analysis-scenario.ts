import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';
import { operation as ModelOp } from '@/components/workflow/ops/model/mod';
import { operation as ModelConfigOp } from '@/components/workflow/ops/model-config/mod';
import { operation as SimulateCiemssOp } from '@/components/workflow/ops/simulate-ciemss/mod';
import { operation as TransformDatasetOp } from '@/components/workflow/ops/dataset-transformer/mod';
import { OperatorNodeSize } from '@/services/workflow';
import { getModelConfigurationById } from '@/services/model-configurations';
import _ from 'lodash';
import { ChartSetting, ChartSettingType } from '@/types/common';
import { updateChartSettingsBySelectedVariables } from '@/services/chart-settings';

export class SensitivityAnalysisScenario extends BaseScenario {
	public static templateId = 'sensitivity-analysis';

	public static templateName = 'Sensitivity Analysis';

	public static header = {
		title: 'Sensitivity Analysis Template',
		question: 'Which parameters introduces the most uncertainty in the outcomes of interest?',
		description:
			'Configure the model with parameter distributions that reflect all the sources of uncertainty, then simulate into the near future.',
		examples: ['Unknown severity of new variant.', 'Unknown speed of waning immunity.']
	};

	modelSpec: { id: string };

	modelConfigSpec: { id: string };

	simulateSpec: { ids: string[] };

	constructor() {
		super();
		this.workflowName = '';
		this.modelSpec = {
			id: ''
		};
		this.modelConfigSpec = {
			id: ''
		};
		this.simulateSpec = {
			ids: []
		};
	}

	setModelSpec(id: string) {
		this.modelSpec.id = id;
		this.modelConfigSpec.id = '';
		this.simulateSpec.ids = [];
	}

	setModelConfigSpec(id: string) {
		this.modelConfigSpec.id = id;
	}

	setCalibrateSpec(ids: string[]) {
		this.simulateSpec.ids = ids;
	}

	toJSON() {
		return {
			templateId: SensitivityAnalysisScenario.templateId,
			workflowName: this.workflowName,
			modelSpec: this.modelSpec,
			modelConfigSpec: this.modelConfigSpec,
			simulateSpec: this.simulateSpec
		};
	}

	isValid(): boolean {
		return !!this.workflowName && !!this.modelSpec.id && !!this.modelConfigSpec.id && !_.isEmpty(this.simulateSpec.ids);
	}

	async createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON());

		// 1. Add nodes
		const modelNode = wf.addNode(
			ModelOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		const modelConfig = await getModelConfigurationById(this.modelConfigSpec.id);
		const modelConfigNode = wf.addNode(
			ModelConfigOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);
		const simulateNode = wf.addNode(
			SimulateCiemssOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		const datasetNode = wf.addNode(
			TransformDatasetOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// 2. Add edges
		wf.addEdge(modelNode.id, modelNode.outputs[0].id, modelConfigNode.id, modelConfigNode.inputs[0].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);
		wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, simulateNode.id, simulateNode.inputs[0].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);
		wf.addEdge(simulateNode.id, simulateNode.outputs[0].id, datasetNode.id, datasetNode.inputs[0].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);

		// 3. Setting node states/outputs
		wf.updateNode(modelNode, {
			state: {
				modelId: this.modelSpec.id
			},
			output: {
				value: [this.modelSpec.id]
			}
		});

		wf.updateNode(modelConfigNode, {
			state: {
				transientModelConfig: modelConfig
			},
			output: {
				value: [modelConfig.id],
				state: _.omit(modelConfigNode.state, ['transientModelConfig'])
			}
		});

		let simulateChartSettings: ChartSetting[] = [];
		simulateChartSettings = updateChartSettingsBySelectedVariables(
			simulateChartSettings,
			ChartSettingType.VARIABLE,
			this.simulateSpec.ids
		);

		wf.updateNode(simulateNode, {
			state: {
				chartSettings: simulateChartSettings
			}
		});

		// 4. Run layout
		wf.runDagreLayout();

		return wf.dump();
	}
}
