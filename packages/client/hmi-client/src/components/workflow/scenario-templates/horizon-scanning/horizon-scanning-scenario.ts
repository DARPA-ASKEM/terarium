import _ from 'lodash';
import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';
import { operation as ModelOp } from '@/components/workflow/ops/model/mod';
import { operation as ModelConfigOp } from '@/components/workflow/ops/model-config/mod';
import { operation as SimulateCiemssOp } from '@/components/workflow/ops/simulate-ciemss/mod';
import { operation as CompareDatasetOp } from '@/components/workflow/ops/compare-datasets/mod';
import { operation as InterventionOp } from '@/components/workflow/ops/intervention-policy/mod';
import { OperatorNodeSize } from '@/services/workflow';
import { createModelConfiguration, getModelConfigurationById, getParameter } from '@/services/model-configurations';
import { ChartSetting, ChartSettingType } from '@/types/common';
import { updateChartSettingsBySelectedVariables } from '@/services/chart-settings';
import { AssetType, ParameterSemantic } from '@/types/Types';
import { DistributionType } from '@/services/distribution';
import { calculateUncertaintyRange } from '@/utils/math';
import { getInterventionPolicyById } from '@/services/intervention-policy';
import { useProjects } from '@/composables/project';
import { getMeanCompareDatasetVariables } from '../scenario-template-utils';

export interface HorizonScanningParameter {
	id: string;
	low: number;
	high: number;
}

/*
 * Horizon Scanning scenario

	Configure the model to represent the extremes of uncertainty for some
	parameters, then simulate into the near future with different intervention
	policies and compare the outcomes.

  Users can input a model, a model configuration, a set of interventions,
  and a set of parameters. For each parameter, a low and high value will be specified,
  resulting in the creation of two new model configurations: one with the
  parameter set to the low value and another with the parameter set to the high value.

	Example:
	1 intervention, 2 parameters
  Model Node
  |
  +-- Model Config Node (Low Param 1)
  |     |
  |     +-- Simulate Node (Intervention 1)
  |
  +-- Model Config Node (High Param 1)
  |     |
  |     +-- Simulate Node (Intervention 1)
  |
  +-- Model Config Node (Low Param 2)
  |     |
  |     +-- Simulate Node (Intervention 1)
  |
  +-- Model Config Node (High Param 2)
        |
        +-- Simulate Node (Intervention 1)
 */
export class HorizonScanningScenario extends BaseScenario {
	public static templateId = 'horizon-scanning';

	public static templateName = 'Horizon scanning';

	modelSpec: { id: string };

	modelConfigSpec: { id: string };

	interventionSpecs: { id: string }[];

	simulateSpec: { ids: string[] };

	parameters: (HorizonScanningParameter | null)[];

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
		this.interventionSpecs = [{ id: '' }];
		this.parameters = [null];
	}

	setModelSpec(id: string) {
		this.modelSpec.id = id;
		this.modelConfigSpec.id = '';
		this.interventionSpecs = [{ id: '' }];
		this.simulateSpec.ids = [];
		this.parameters = [null];
	}

	setModelConfigSpec(id: string) {
		this.modelConfigSpec.id = id;
	}

	setSimulateSpec(ids: string[]) {
		this.simulateSpec.ids = ids;
	}

	addInterventionSpec() {
		this.interventionSpecs.push({ id: '' });
	}

	setInterventionSpec(id: string, index: number) {
		this.interventionSpecs[index].id = id;
	}

	deleteInterventionSpec(index: number) {
		this.interventionSpecs.splice(index, 1);
	}

	addParameter() {
		this.parameters.push(null);
	}

	removeParameter(index: number) {
		this.parameters.splice(index, 1);
	}

	setParameter(parameter: ParameterSemantic, index: number) {
		let low = 0;
		let high = 1;
		if (parameter.distribution.type === DistributionType.Constant) {
			const { min, max } = calculateUncertaintyRange(parameter.distribution.parameters.value, 10);
			low = min;
			high = max;
		} else if (parameter.distribution.type === DistributionType.Uniform) {
			low = parameter.distribution.parameters.minimum;
			high = parameter.distribution.parameters.maximum;
		}

		this.parameters[index] = { id: parameter.referenceId, low, high };
	}

	toJSON() {
		return {
			templateId: HorizonScanningScenario.templateId,
			workflowName: this.workflowName,
			modelSpec: this.modelSpec,
			modelConfigSpec: this.modelConfigSpec,
			simulateSpec: this.simulateSpec,
			interventionSpecs: this.interventionSpecs
		};
	}

	isValid(): boolean {
		return (
			!!this.workflowName &&
			!!this.modelSpec.id &&
			!!this.modelConfigSpec.id &&
			!this.parameters.some((parameter) => !parameter) &&
			!_.isEmpty(this.simulateSpec.ids)
		);
	}

	async createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON());

		// 1. Add model and compare dataset nodes
		const modelNode = wf.addNode(
			ModelOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		wf.updateNode(modelNode, {
			state: {
				modelId: this.modelSpec.id
			},
			output: {
				value: [this.modelSpec.id]
			}
		});

		const compareDatasetNode = wf.addNode(
			CompareDatasetOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// add input ports for each simulation to the dataset transformer, this will be a matrix of intervention x parameter low and high
		for (let i = 0; i < this.interventionSpecs.length * (this.parameters.length * 2); i++) {
			workflowService.appendInputPort(compareDatasetNode, {
				type: 'datasetId|simulationId',
				label: 'Dataset or Simulation'
			});
		}

		let compareDatasetIndex = 0;

		const modelConfig = await getModelConfigurationById(this.modelConfigSpec.id);

		// chart settings for simulate node
		let simulateChartSettings: ChartSetting[] = [];
		simulateChartSettings = updateChartSettingsBySelectedVariables(
			simulateChartSettings,
			ChartSettingType.VARIABLE,
			this.simulateSpec.ids
		);

		let compareDatasetChartSettings: ChartSetting[] = [];
		compareDatasetChartSettings = updateChartSettingsBySelectedVariables(
			compareDatasetChartSettings,
			ChartSettingType.VARIABLE,
			getMeanCompareDatasetVariables(this.simulateSpec.ids, modelConfig)
		);

		wf.updateNode(compareDatasetNode, {
			state: {
				chartSettings: compareDatasetChartSettings
			}
		});

		// 2. create model config nodes for each paramter for both the low and high values and attach them to the model node
		const modelConfigPromises = this.parameters.flatMap(async (parameter) => {
			if (!parameter) return [];
			const clonedModelConfig = _.cloneDeep(modelConfig);
			const foundParameter = getParameter(clonedModelConfig, parameter.id);
			if (!foundParameter) return [];

			const modelConfigNodeLow = wf.addNode(
				ModelConfigOp,
				{ x: 0, y: 0 },
				{
					size: OperatorNodeSize.medium
				}
			);

			wf.addEdge(modelNode.id, modelNode.outputs[0].id, modelConfigNodeLow.id, modelConfigNodeLow.inputs[0].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);

			const modelConfigNodeHigh = wf.addNode(
				ModelConfigOp,
				{ x: 0, y: 0 },
				{
					size: OperatorNodeSize.medium
				}
			);

			wf.addEdge(modelNode.id, modelNode.outputs[0].id, modelConfigNodeHigh.id, modelConfigNodeHigh.inputs[0].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);

			// We want to change the distribution of the parameter to a constant distribution with the 1 config with the constant low value, and another with a constant high value
			foundParameter.distribution.type = DistributionType.Constant;
			foundParameter.distribution.parameters = { value: parameter.low };

			clonedModelConfig.name = `${modelConfig.name}_${parameter.id}_low`;
			const newModelConfigLow = await createModelConfiguration(clonedModelConfig);
			await useProjects().addAsset(
				AssetType.ModelConfiguration,
				newModelConfigLow.id,
				useProjects().activeProject.value?.id
			);

			foundParameter.distribution.parameters = { value: parameter.high };

			clonedModelConfig.name = `${modelConfig.name}_${parameter.id}_high`;
			const newModelConfigHigh = await createModelConfiguration(clonedModelConfig);
			await useProjects().addAsset(
				AssetType.ModelConfiguration,
				newModelConfigHigh.id,
				useProjects().activeProject.value?.id
			);

			wf.updateNode(modelConfigNodeLow, {
				state: {
					transientModelConfig: newModelConfigLow
				},
				output: {
					value: [newModelConfigLow.id],
					state: _.omit(modelConfigNodeLow.state, ['transientModelConfig'])
				}
			});

			wf.updateNode(modelConfigNodeHigh, {
				state: {
					transientModelConfig: newModelConfigHigh
				},
				output: {
					value: [newModelConfigHigh.id],
					state: _.omit(modelConfigNodeHigh.state, ['transientModelConfig'])
				}
			});

			return [modelConfigNodeLow, modelConfigNodeHigh];
		});

		// Wait for all modelConfigPromises to resolve and flatten them
		const modelConfigNodes = (await Promise.all(modelConfigPromises)).flat();

		// 3. Add intervention nodes for each intervention and attach them to the model node
		// filter out any interventions that don't have an id
		const interventionPromises = this.interventionSpecs
			.filter((spec) => !!spec.id)
			.map(async (interventionSpec) => {
				const interventionPolicy = await getInterventionPolicyById(interventionSpec.id);
				const interventionNode = wf.addNode(
					InterventionOp,
					{ x: 0, y: 0 },
					{
						size: OperatorNodeSize.medium
					}
				);

				wf.addEdge(modelNode.id, modelNode.outputs[0].id, interventionNode.id, interventionNode.inputs[0].id, [
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

				return interventionNode;
			});

		// Wait for all interventionPromises to resolve
		const interventionNodes = await Promise.all(interventionPromises);

		// 4. Add simulate nodes for each model config and intervention and attach them to the model config and intervention nodes
		// each intervention node will be connected to a simulate node along with each model config node
		modelConfigNodes.forEach((modelConfigNode) => {
			// For each model config node, we want to connect it to a simulate node, even if interventions aren't present
			const simulateNode = wf.addNode(
				SimulateCiemssOp,
				{ x: 0, y: 0 },
				{
					size: OperatorNodeSize.medium
				}
			);

			wf.updateNode(simulateNode, {
				state: {
					chartSettings: simulateChartSettings
				}
			});

			wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, simulateNode.id, simulateNode.inputs[0].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);

			interventionNodes.forEach((interventionNode, index) => {
				// If this is the first intervention node, connect it to the model config node and the already created simulate node
				if (index === 0) {
					wf.addEdge(interventionNode.id, interventionNode.outputs[0].id, simulateNode.id, simulateNode.inputs[1].id, [
						{ x: 0, y: 0 },
						{ x: 0, y: 0 }
					]);
				} else {
					const additionalSimNode = wf.addNode(
						SimulateCiemssOp,
						{ x: 0, y: 0 },
						{
							size: OperatorNodeSize.medium
						}
					);

					wf.updateNode(additionalSimNode, {
						state: {
							chartSettings: simulateChartSettings
						}
					});

					wf.addEdge(
						modelConfigNode.id,
						modelConfigNode.outputs[0].id,
						additionalSimNode.id,
						additionalSimNode.inputs[0].id,
						[
							{ x: 0, y: 0 },
							{ x: 0, y: 0 }
						]
					);
					wf.addEdge(
						interventionNode.id,
						interventionNode.outputs[0].id,
						additionalSimNode.id,
						additionalSimNode.inputs[1].id,
						[
							{ x: 0, y: 0 },
							{ x: 0, y: 0 }
						]
					);
					wf.addEdge(
						additionalSimNode.id,
						additionalSimNode.outputs[0].id,
						compareDatasetNode.id,
						compareDatasetNode.inputs[compareDatasetIndex].id,
						[
							{ x: 0, y: 0 },
							{ x: 0, y: 0 }
						]
					);
					compareDatasetIndex++;
				}
			});

			wf.addEdge(
				simulateNode.id,
				simulateNode.outputs[0].id,
				compareDatasetNode.id,
				compareDatasetNode.inputs[compareDatasetIndex].id,
				[
					{ x: 0, y: 0 },
					{ x: 0, y: 0 }
				]
			);
			compareDatasetIndex++;
		});

		// 4. Run layout
		wf.runDagreLayout();

		return wf.dump();
	}
}
