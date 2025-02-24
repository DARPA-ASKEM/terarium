import { EnsembleModelConfigs } from '@/types/Types';
import { WorkflowNode } from '@/types/workflow';
import _ from 'lodash';
import { computed, Ref } from 'vue';
import { DataArray } from '@/utils/stats';
import { ChartData } from '@/composables/useCharts';
import { processAndSortSamplesByTimepoint } from '@/services/models/simulation-service';
import { v4 as uuidv4 } from 'uuid';
import { getActiveOutput } from '../../util';
import {
	SimulateEnsembleCiemssOperationState,
	SimulateEnsembleMappingRow,
	SimulateEnsembleWeights
} from './simulate-ensemble-ciemss-operation';

export function formatSimulateModelConfigurations(
	rows: SimulateEnsembleMappingRow[],
	weights: SimulateEnsembleWeights
): EnsembleModelConfigs[] {
	const ensembleModelConfigMap: { [key: string]: EnsembleModelConfigs } = {};
	// 1. map the weights to the ensemble model configs
	Object.entries(weights).forEach(([key, value]) => {
		// return if there is no weight
		if (!value) return;

		const ensembleModelConfig: EnsembleModelConfigs = {
			id: key,
			solutionMappings: {},
			weight: value
		};

		ensembleModelConfigMap[key] = ensembleModelConfig;
	});

	// 2. format the solution mappings
	rows.forEach((row) => {
		Object.entries(row.modelConfigurationMappings).forEach(([key, value]) => {
			if (!ensembleModelConfigMap[key]) return;
			ensembleModelConfigMap[key].solutionMappings[row.newName] = value;
		});
	});

	return [...Object.values(ensembleModelConfigMap)];
}

export function getChartEnsembleMapping(
	node: WorkflowNode<SimulateEnsembleCiemssOperationState>,
	stateToModelConfigMap: { [key: string]: string[] }
) {
	const wfOutputState = getActiveOutput(node)?.state;
	const mapping = _.clone(wfOutputState?.mapping ?? []);

	// For every State Variable that has not been mapped in the ensembleMapping
	// We will fill in here so the user can still see these if they want
	Object.keys(stateToModelConfigMap).forEach((state) => {
		if (!mapping.find((map) => map.newName === state)) {
			const modelConfigurationsMap = {};
			stateToModelConfigMap[state].forEach((id) => {
				modelConfigurationsMap[id] = state;
			});
			mapping.push({
				id: uuidv4(),
				newName: state,
				modelConfigurationMappings: modelConfigurationsMap
			});
		}
	});
	return mapping;
}

export function usePreparedChartInputs(
	props: {
		node: WorkflowNode<SimulateEnsembleCiemssOperationState>;
	},
	runResult: Ref<{ [runId: string]: DataArray }>,
	runResultSummary: Ref<{ [runId: string]: DataArray }>,
	pyciemssMap: Ref<Record<string, string>>
) {
	const selectedRunId = computed(() => props.node.outputs.find((o) => o.id === props.node.active)?.value?.[0]);

	return computed(() => {
		if (!selectedRunId.value || _.isEmpty(pyciemssMap.value)) return null;

		const result = runResult.value[selectedRunId.value];
		const resultSummary = runResultSummary.value[selectedRunId.value];

		// Process data for uncertainty intervals chart mode
		const resultGroupByTimepoint = processAndSortSamplesByTimepoint(result);

		const reverseMap: Record<string, string> = {};
		Object.keys(pyciemssMap.value).forEach((key) => {
			reverseMap[pyciemssMap.value[key]] = key;
			reverseMap[`${pyciemssMap.value[key]}_mean`] = key;
		});
		return {
			result,
			resultSummary,
			translationMap: reverseMap,
			pyciemssMap: pyciemssMap.value,
			resultGroupByTimepoint
		} as ChartData;
	});
}

// Build chart data by adding variable translation map to the given output data
export function buildChartData(
	outputData: {
		result: DataArray;
		resultSummary: DataArray;
		pyciemssMap: Record<string, string>;
	} | null,
	mappings: SimulateEnsembleMappingRow[]
) {
	if (!outputData) return null;
	const pyciemssMap = outputData.pyciemssMap;
	const translationMap = {};
	Object.keys(outputData.pyciemssMap).forEach((key) => {
		// pyciemssMap keys are formatted as either '{modelConfigId}/{displayVariableName}' for model variables or '{displayVariableName}' for ensemble variables
		const tokens = key.split('/');
		const varName = tokens.length > 1 ? tokens[1] : 'Ensemble';
		translationMap[`${pyciemssMap[key]}`] = `${varName}`;
		translationMap[`${pyciemssMap[key]}_mean`] = `${varName}`;
	});
	// Add translation map for dataset variables
	mappings.forEach((mapObj) => {
		translationMap[mapObj.newName] = 'Observations';
	});
	console.log({ ...outputData, translationMap });
	return { ...outputData, translationMap };
}
