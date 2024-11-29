import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import { getModelConfigurationById } from '@/services/model-configurations';
import * as workflowService from '@/services/workflow';
import _ from 'lodash';
import { operation as ModelOp } from '@/components/workflow/ops/model/mod';
import { operation as ModelConfigOp } from '@/components/workflow/ops/model-config/mod';
import { operation as InterventionOp } from '@/components/workflow/ops/intervention-policy/mod';
import { operation as SimulateOp } from '@/components/workflow/ops/simulate-ciemss/mod';
import { operation as TransformDatasetOp } from '@/components/workflow/ops/dataset-transformer/mod';
import { OperatorNodeSize } from '@/services/workflow';
import { flattenInterventionData, getInterventionPolicyById } from '@/services/intervention-policy';
import { ChartSetting, ChartSettingType } from '@/types/common';
import { updateChartSettingsBySelectedVariables } from '@/services/chart-settings';

export class DecisionMakingScenario extends BaseScenario {
	public static templateId = 'decision-making';

	public static templateName = 'Decision Making';

	modelSpec: { id: string };

	modelConfigSpec: { id: string };

	interventionSpecs: { id: string }[];

	simulateSpec: { ids: string[] };

	constructor() {
		super();
		this.modelSpec = {
			id: ''
		};

		// Start with 2 empty intervention specs
		this.interventionSpecs = [
			{
				id: ''
			},
			{ id: '' }
		];
		this.modelConfigSpec = {
			id: ''
		};
		this.simulateSpec = {
			ids: []
		};
		this.workflowName = '';
	}

	setModelSpec(id: string) {
		this.modelSpec.id = id;
		this.modelConfigSpec.id = '';
		this.interventionSpecs.forEach((interventionSpec) => {
			interventionSpec.id = '';
		});
		this.simulateSpec.ids = [];
	}

	setModelConfigSpec(id: string) {
		this.modelConfigSpec.id = id;
	}

	addInterventionSpec() {
		this.interventionSpecs.push({ id: '' });
	}

	removeInterventionSpec(index: number) {
		this.interventionSpecs.splice(index, 1);
	}

	setInterventionSpecs(id: string, index: number) {
		this.interventionSpecs[index].id = id;
	}

	setSimulateSpec(ids: string[]) {
		this.simulateSpec.ids = ids;
	}

	toJSON() {
		return {
			templateId: DecisionMakingScenario.templateId,
			workflowName: this.workflowName,
			modelSpec: this.modelSpec,
			modelConfigSpec: this.modelConfigSpec,
			interventionSpecs: this.interventionSpecs,
			simulateSpec: this.simulateSpec
		};
	}

	isValid(): boolean {
		return (
			!!this.workflowName &&
			!!this.modelSpec.id &&
			!!this.modelConfigSpec.id &&
			!this.interventionSpecs.some((interventionSpec) => !interventionSpec.id) &&
			!_.isEmpty(this.simulateSpec.ids)
		);
	}

	async createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON());

		// 1. Add model and model config nodes and connect them
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

		const datasetTransformerNode = wf.addNode(
			TransformDatasetOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		wf.addEdge(modelNode.id, modelNode.outputs[0].id, modelConfigNode.id, modelConfigNode.inputs[0].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);

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

		// 2. Add base simulation (no interventions) and connect it to the dataset transformer
		const baseSimulateNode = wf.addNode(
			SimulateOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, baseSimulateNode.id, baseSimulateNode.inputs[0].id, [
			{ x: 0, y: 0 },
			{ x: 0, y: 0 }
		]);

		wf.updateNode(baseSimulateNode, {
			state: {
				chartSettings: simulateChartSettings
			}
		});

		wf.addEdge(
			baseSimulateNode.id,
			baseSimulateNode.outputs[0].id,
			datasetTransformerNode.id,
			datasetTransformerNode.inputs[0].id,
			[
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]
		);

		/* 3. Create intervention and simulate nodes for each intervention
		 model -> intervention -> simulate -> dataset transformer
		 modelConfig -> simulate -> dataset transformer */

		// add input ports for each simulation to the dataset transformer (base simulation + interventions)
		for (let i = 0; i < this.interventionSpecs.length + 1; i++) {
			workflowService.appendInputPort(datasetTransformerNode, {
				type: 'datasetId|simulationId',
				label: 'Dataset or Simulation'
			});
		}

		const promises = this.interventionSpecs.map(async (interventionSpec, i) => {
			const interventionPolicy = await getInterventionPolicyById(interventionSpec.id);

			simulateChartSettings = updateChartSettingsBySelectedVariables(
				simulateChartSettings,
				ChartSettingType.INTERVENTION,
				Object.keys(_.groupBy(flattenInterventionData(interventionPolicy.interventions ?? []), 'appliedTo'))
			);

			const interventionNode = wf.addNode(
				InterventionOp,
				{ x: 0, y: 0 },
				{
					size: OperatorNodeSize.medium
				}
			);

			const simulateNode = wf.addNode(
				SimulateOp,
				{ x: 0, y: 0 },
				{
					size: OperatorNodeSize.medium
				}
			);

			wf.addEdge(modelNode.id, modelNode.outputs[0].id, interventionNode.id, interventionNode.inputs[0].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);
			wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, simulateNode.id, simulateNode.inputs[0].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);
			wf.addEdge(interventionNode.id, interventionNode.outputs[0].id, simulateNode.id, simulateNode.inputs[1].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);

			wf.updateNode(interventionNode, {
				state: {
					interventionPolicy
				},
				output: {
					value: [interventionPolicy.id],
					state: interventionNode.state
				}
			});

			wf.updateNode(simulateNode, {
				state: {
					chartSettings: simulateChartSettings
				}
			});

			wf.addEdge(
				simulateNode.id,
				simulateNode.outputs[0].id,
				datasetTransformerNode.id,
				datasetTransformerNode.inputs[i + 1].id,
				[
					{ x: 0, y: 0 },
					{ x: 0, y: 0 }
				]
			);
		});
		await Promise.all(promises);

		wf.runDagreLayout();

		return wf.dump();
	}
}