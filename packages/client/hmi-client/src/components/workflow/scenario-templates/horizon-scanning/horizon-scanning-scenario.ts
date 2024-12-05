import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';
import { operation as ModelOp } from '@/components/workflow/ops/model/mod';
import { operation as ModelConfigOp } from '@/components/workflow/ops/model-config/mod';
import { operation as SimulateCiemssOp } from '@/components/workflow/ops/simulate-ciemss/mod';
// import { operation as TransformDatasetOp } from '@/components/workflow/ops/dataset-transformer/mod';
import { operation as InterventionOp } from '@/components/workflow/ops/intervention-policy/mod';
import { OperatorNodeSize } from '@/services/workflow';
import { createModelConfiguration, getModelConfigurationById, getParameter } from '@/services/model-configurations';
import _ from 'lodash';
// import { ChartSetting, ChartSettingType } from '@/types/common';
// import { updateChartSettingsBySelectedVariables } from '@/services/chart-settings';
import { AssetType, ParameterSemantic } from '@/types/Types';
import { DistributionType } from '@/services/distribution';
import { calculateUncertaintyRange } from '@/utils/math';
import { getInterventionPolicyById } from '@/services/intervention-policy';
import { useProjects } from '@/composables/project';

export interface HorizonScanningParameter {
	id: string;
	low: number;
	high: number;
}
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

	setCalibrateSpec(ids: string[]) {
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
		let high = 0;
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

		wf.updateNode(modelNode, {
			state: {
				modelId: this.modelSpec.id
			},
			output: {
				value: [this.modelSpec.id]
			}
		});

		const modelConfig = await getModelConfigurationById(this.modelConfigSpec.id);

		const modelConfigNodes = (
			await Promise.all(
				this.parameters.map(async (parameter) => {
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
				})
			)
		)
			.flat()
			.filter((node) => node !== undefined);

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

			// Collect all promises from the modelConfigNodes map
			const simulatePromises = modelConfigNodes.map(async (modelConfigNode) => {
				const simulateNode = wf.addNode(
					SimulateCiemssOp,
					{ x: 0, y: 0 },
					{
						size: OperatorNodeSize.medium
					}
				);

				wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, simulateNode.id, simulateNode.inputs[0].id, [
					{ x: 0, y: 0 },
					{ x: 0, y: 0 }
				]);

				wf.addEdge(interventionNode.id, interventionNode.outputs[0].id, simulateNode.id, simulateNode.inputs[1].id, [
					{ x: 0, y: 0 },
					{ x: 0, y: 0 }
				]);
			});

			// Wait for all simulatePromises to resolve
			await Promise.all(simulatePromises);
		});

		// Wait for all interventionPromises to resolve
		await Promise.all(interventionPromises);

		// 4. Run layout
		wf.runDagreLayout();

		return wf.dump();
	}
}
