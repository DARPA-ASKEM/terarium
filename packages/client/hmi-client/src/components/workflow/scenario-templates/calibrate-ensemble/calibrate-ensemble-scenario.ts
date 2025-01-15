import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import * as workflowService from '@/services/workflow';
import { operation as ModelOp } from '@/components/workflow/ops/model/mod';
import { operation as ModelConfigOp } from '@/components/workflow/ops/model-config/mod';
import { operation as SimulateOp } from '@/components/workflow/ops/simulate-ciemss/mod';
import { operation as CalibrateOp } from '@/components/workflow/ops/calibrate-ciemss/mod';
import { operation as CalibrateEnsembleOp } from '@/components/workflow/ops/calibrate-ensemble-ciemss/mod';
import { operation as DatasetOp } from '@/components/workflow/ops/dataset/mod';
import { OperatorNodeSize } from '@/services/workflow';
import { v4 as uuidv4 } from 'uuid';
import _ from 'lodash';
import { getModelConfigurationById } from '@/services/model-configurations';
import { CalibrateEnsembleMappingRow } from '../../ops/calibrate-ensemble-ciemss/calibrate-ensemble-ciemss-operation';

export class CalibrateEnsembleScenario extends BaseScenario {
	public static templateId = 'calibrate-ensemble';

	public static templateName = 'Calibrate an ensemble model';

	datasetSpec: { id: string };

	tabSpecs: {
		id: string;
		modelSpec: { id: string };
		modelConfigSpec: { id: string };
		interventionSpecs: { id: string }[];
	}[];

	simulateSpec: { ids: string[] };

	timestampColName: string;

	calibrateMappings: CalibrateEnsembleMappingRow[];

	constructor() {
		super();
		this.datasetSpec = {
			id: ''
		};

		this.tabSpecs = [
			{
				id: uuidv4(),
				modelSpec: {
					id: ''
				},
				modelConfigSpec: {
					id: ''
				},
				interventionSpecs: [
					{
						id: ''
					}
				]
			}
		];

		this.simulateSpec = {
			ids: []
		};

		this.timestampColName = '';
		this.calibrateMappings = [];
		this.workflowName = '';
	}

	setDatasetSpec(id: string) {
		this.datasetSpec.id = id;
		this.timestampColName = '';
	}

	setModelSpec(modelId: string, tabId: string) {
		const tabSpec = this.tabSpecs.find((tab) => tab.id === tabId);
		if (!tabSpec) return;
		tabSpec.modelSpec.id = modelId;
		tabSpec.modelConfigSpec.id = '';
		tabSpec.interventionSpecs = [{ id: '' }];
	}

	setModelConfigSpec(modelConfigId: string, tabId: string) {
		const tabSpec = this.tabSpecs.find((tab) => tab.id === tabId);
		if (!tabSpec) return;
		tabSpec.modelConfigSpec.id = modelConfigId;
	}

	addInterventionSpec(tabId: string) {
		const tabSpec = this.tabSpecs.find((tab) => tab.id === tabId);
		if (!tabSpec) return;
		tabSpec.interventionSpecs.push({ id: '' });
	}

	removeInterventionSpec(tabId: string, index: number) {
		const tabSpec = this.tabSpecs.find((tab) => tab.id === tabId);
		if (!tabSpec) return;
		tabSpec.interventionSpecs.splice(index, 1);
	}

	setInterventionSpec(interventionId: string, tabId: string, index: number) {
		const tabSpec = this.tabSpecs.find((tab) => tab.id === tabId);
		if (!tabSpec) return;
		tabSpec.interventionSpecs[index].id = interventionId;
	}

	setSimulateSpec(ids: string[]) {
		this.simulateSpec.ids = ids;
	}

	setTimeStepColName(name: string) {
		this.timestampColName = name;
	}

	addTabSpec() {
		this.tabSpecs.push({
			id: uuidv4(),
			modelSpec: {
				id: ''
			},
			modelConfigSpec: {
				id: ''
			},
			interventionSpecs: [
				{
					id: ''
				}
			]
		});
	}

	addMappingRow() {
		this.calibrateMappings.push({
			newName: '',
			datasetMapping: '',
			modelConfigurationMappings: {}
		});
	}

	removeMappingRow(index: number) {
		this.calibrateMappings.splice(index, 1);
	}

	toJSON() {
		return {
			templateId: CalibrateEnsembleScenario.templateId,
			workflowName: this.workflowName,
			datasetSpec: this.datasetSpec,
			tabSpec: this.tabSpecs,
			simulateSpec: this.simulateSpec
		};
	}

	isValid(): boolean {
		return (
			!!this.workflowName &&
			!!this.datasetSpec.id &&
			this.tabSpecs.every((tab) => !!tab.modelSpec.id && !!tab.modelConfigSpec.id)
		);
	}

	async createWorkflow() {
		const wf = new workflowService.WorkflowWrapper();
		wf.setWorkflowName(this.workflowName);
		wf.setWorkflowScenario(this.toJSON());

		// add dataset node
		const datasetNode = wf.addNode(
			DatasetOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// add calibrate ensemble node
		const calibrateEnsembleNode = wf.addNode(
			CalibrateEnsembleOp,
			{ x: 0, y: 0 },
			{
				size: OperatorNodeSize.medium
			}
		);

		// attach to calibrate ensemble node
		wf.addEdge(
			datasetNode.id,
			datasetNode.outputs[0].id,
			calibrateEnsembleNode.id,
			calibrateEnsembleNode.inputs[0].id,
			[
				{ x: 0, y: 0 },
				{ x: 0, y: 0 }
			]
		);

		for (let i = 0; i < this.tabSpecs.length - 1; i++) {
			// add inputs
			workflowService.appendInputPort(calibrateEnsembleNode, {
				type: 'modelConfigId',
				label: 'Model configuration'
			});
		}

		// every tab has a model, model config, intervention (optional).  Attach accordingly.

		await Promise.all(
			this.tabSpecs.map(async (tab, index) => {
				const modelNode = wf.addNode(
					ModelOp,
					{ x: 0, y: 0 },
					{
						size: OperatorNodeSize.medium
					}
				);

				const modelConfig = await getModelConfigurationById(tab.modelConfigSpec.id);

				const modelConfigNode = wf.addNode(
					ModelConfigOp,
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

				const calibrateNode = wf.addNode(
					CalibrateOp,
					{ x: 0, y: 0 },
					{
						size: OperatorNodeSize.medium
					}
				);

				// attach to model config node
				wf.addEdge(modelNode.id, modelNode.outputs[0].id, modelConfigNode.id, modelConfigNode.inputs[0].id, [
					{ x: 0, y: 0 },
					{ x: 0, y: 0 }
				]);

				// attach to simulate node
				wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, simulateNode.id, simulateNode.inputs[0].id, [
					{ x: 0, y: 0 },
					{ x: 0, y: 0 }
				]);

				// attach to calibrate node
				wf.addEdge(modelConfigNode.id, modelConfigNode.outputs[0].id, calibrateNode.id, calibrateNode.inputs[0].id, [
					{ x: 0, y: 0 },
					{ x: 0, y: 0 }
				]);

				wf.addEdge(datasetNode.id, datasetNode.outputs[0].id, calibrateNode.id, calibrateNode.inputs[1].id, [
					{ x: 0, y: 0 },
					{ x: 0, y: 0 }
				]);

				wf.updateNode(modelNode, {
					state: {
						modelId: tab.modelSpec.id
					},
					output: {
						value: [tab.modelSpec.id]
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

				// attach to calibrate ensemble node
				wf.addEdge(
					modelConfigNode.id,
					modelConfigNode.outputs[0].id,
					calibrateEnsembleNode.id,
					calibrateEnsembleNode.inputs[index + 1].id,
					[
						{ x: 0, y: 0 },
						{ x: 0, y: 0 }
					]
				);
			})
		);

		wf.updateNode(calibrateEnsembleNode, {
			state: {
				timestampColName: this.timestampColName,
				ensembleMapping: this.calibrateMappings
			}
		});

		wf.updateNode(datasetNode, {
			state: {
				datasetId: this.datasetSpec.id
			}
		});

		wf.runDagreLayout();

		return wf.dump();
	}
}
