import _ from 'lodash';
import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';
import { operation as ModelOp } from '@/components/workflow/ops/model/mod';
import { operation as ModelConfigOp } from '@/components/workflow/ops/model-config/mod';
import { operation as SimulateCiemssOp } from '@/components/workflow/ops/simulate-ciemss/mod';
import { operation as CompareDatasetOp } from '@/components/workflow/ops/compare-datasets/mod';
import { operation as InterventionOp } from '@/components/workflow/ops/intervention-policy/mod';
import { OperatorNodeSize } from '@/services/workflow';
import {
	createModelConfiguration,
	getModelConfigurationById,
	setParameterDistribution
} from '@/services/model-configurations';
import { ChartSetting, ChartSettingType } from '@/types/common';
import { updateChartSettingsBySelectedVariables } from '@/services/chart-settings';
import { AssetType, ParameterSemantic } from '@/types/Types';
import { getInterventionPolicyById } from '@/services/intervention-policy';
import { useProjects } from '@/composables/project';
import { formatUniformDistribution } from '../scenario-template-utils';

/*
 * Value of information scenario

	Configure the model with parameter distributions that reflect all the sources of uncertainty,
	then simulate into the near future with different intervention policies.

  Users can input a model, a model configuration, a set of interventions,
  and a set of parameters. For each parameter, a min and max value will be specified,
  resulting in the creation of new model configurations for each parameter with that
	parameter set to a uniform.

	Example:
	2 interventions, 2 parameters
  Model Node
  |
  +-- Model Config Node (Uniform distrubiton for Param 1)
  |     |
  |     +-- Simulate Node (Intervention 1)
  |
  +-- Model Config Node (Uniform distrubiton for Param 2)
  |     |
  |     +-- Simulate Node (Intervention 1)
  |
  +-- Model Config Node (Uniform distrubiton for Param 1)
  |     |
  |     +-- Simulate Node (Intervention 2)
  |
  +-- Model Config Node (Uniform distrubiton for Param 2)
        |
        +-- Simulate Node (Intervention 2)
 */
export class ValueOfInformationScenario extends BaseScenario {
	public static templateId = 'value-of-information-scenario';

	public static templateName = 'Value of information';

	modelSpec: { id: string };

	modelConfigSpec: { id: string };

	interventionSpecs: { id: string }[];

	simulateSpecs: { ids: string[] };

	parameters: (ParameterSemantic | null)[];

	constructor() {
		super();
		this.workflowName = '';
		this.modelSpec = {
			id: ''
		};
		this.modelConfigSpec = {
			id: ''
		};
		this.simulateSpecs = {
			ids: []
		};
		this.interventionSpecs = [{ id: '' }];
		this.parameters = [null];
	}

	setModelSpec(id: string) {
		this.modelSpec.id = id;
		this.modelConfigSpec.id = '';
		this.interventionSpecs = [{ id: '' }];
		this.simulateSpecs.ids = [];
		this.parameters = [null];
	}

	setModelConfigSpec(id: string) {
		this.modelConfigSpec.id = id;
	}

	setSimulateSpec(ids: string[]) {
		this.simulateSpecs.ids = ids;
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
		formatUniformDistribution(parameter);
		this.parameters[index] = parameter;
	}

	toJSON() {
		return {
			templateId: ValueOfInformationScenario.templateId,
			workflowName: this.workflowName,
			modelSpec: this.modelSpec,
			modelConfigSpec: this.modelConfigSpec,
			simulateSpecs: this.simulateSpecs,
			interventionSpecs: this.interventionSpecs
		};
	}

	isValid(): boolean {
		return (
			!!this.workflowName &&
			!!this.modelSpec.id &&
			!!this.modelConfigSpec.id &&
			!this.interventionSpecs.some((interventionSpec) => !interventionSpec.id) &&
			!this.parameters.some((parameter) => !parameter) &&
			!_.isEmpty(this.simulateSpecs.ids)
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

		// add input ports for each simulation to the dataset transformer, this will be a matrix of intervention x parameter
		for (let i = 0; i < this.interventionSpecs.length * this.parameters.length; i++) {
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
			this.simulateSpecs.ids
		);

		// 2. create model config nodes for each paramter and attach them to the model node
		const modelConfigPromises = this.parameters.flatMap(async (parameter) => {
			if (!parameter) return null;
			const clonedModelConfig = _.cloneDeep(modelConfig);

			const modelConfigNode = wf.addNode(
				ModelConfigOp,
				{ x: 0, y: 0 },
				{
					size: OperatorNodeSize.medium
				}
			);

			wf.addEdge(modelNode.id, modelNode.outputs[0].id, modelConfigNode.id, modelConfigNode.inputs[0].id, [
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]);

			setParameterDistribution(clonedModelConfig, parameter.referenceId, parameter.distribution);

			clonedModelConfig.name = `${modelConfig.name}_${parameter.referenceId}`;
			const newModelConfig = await createModelConfiguration(clonedModelConfig);
			await useProjects().addAsset(
				AssetType.ModelConfiguration,
				newModelConfig.id,
				useProjects().activeProject.value?.id
			);

			wf.updateNode(modelConfigNode, {
				state: {
					transientModelConfig: newModelConfig
				},
				output: {
					value: [newModelConfig.id],
					state: _.omit(modelConfigNode.state, ['transientModelConfig'])
				}
			});

			return modelConfigNode;
		});

		// Wait for all modelConfigPromises to resolve and filter non null responses
		const modelConfigNodes = (await Promise.all(modelConfigPromises)).filter((node) => !!node);

		// 3. Add intervention nodes for each intervention and attach them to the model node
		const interventionPromises = this.interventionSpecs.map(async (interventionSpec) => {
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

			// each intervention node will be connected to a simulate node along with each model config node
			modelConfigNodes.forEach((modelConfigNode) => {
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

				wf.addEdge(interventionNode.id, interventionNode.outputs[0].id, simulateNode.id, simulateNode.inputs[1].id, [
					{ x: 0, y: 0 },
					{ x: 0, y: 0 }
				]);

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
		});

		// Wait for all interventionPromises to resolve
		await Promise.all(interventionPromises);

		// 4. Run layout
		wf.runDagreLayout();

		return wf.dump();
	}
}
