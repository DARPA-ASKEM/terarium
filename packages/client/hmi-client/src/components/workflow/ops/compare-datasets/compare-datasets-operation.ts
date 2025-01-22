import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';
import { ChartSetting } from '@/types/common';

const DOCUMENTATION_URL = 'https://documentation.terarium.ai/datasets/compare-datasets/';

export enum TimepointOption {
	LAST = 'last',
	FIRST = 'first'
}
export enum RankOption {
	MINIMUM = 'minimum',
	MAXIMUM = 'maximum'
}

export enum PlotValue {
	VALUE = 'value',
	PERCENTAGE = 'percent change',
	DIFFERENCE = 'difference'
}

export enum CompareValue {
	IMPACT = 'impact',
	RANK = 'rank'
}

export const blankCriteriaOfInterest = {
	name: 'Criteria of interest',
	selectedConfigurationId: null,
	selectedVariable: null,
	rank: RankOption.MINIMUM,
	timepoint: TimepointOption.LAST
};
export interface CriteriaOfInterestCard {
	name: string;
	selectedConfigurationId: string | null;
	selectedVariable: string | null;
	rank: RankOption;
	timepoint: TimepointOption;
}
export interface CompareDatasetsState extends BaseState {
	criteriaOfInterestCards: CriteriaOfInterestCard[];
	selectedPlotType: PlotValue;
	selectedCompareOption: CompareValue;
	selectedBaselineDatasetId: string | null;
	chartSettings: ChartSetting[] | null;
}

export const CompareDatasetsOperation: Operation = {
	name: WorkflowOperationTypes.COMPARE_DATASETS,
	displayName: 'Compare datasets',
	description: 'Compare datasets, or simulation results',
	documentationUrl: DOCUMENTATION_URL,
	inputs: [
		{ type: 'datasetId', label: 'Dataset or Simulation result' },
		{ type: 'datasetId', label: 'Dataset or Simulation result' }
	],
	outputs: [{ type: 'datasetId', label: 'Dataset' }],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: CompareDatasetsState = {
			criteriaOfInterestCards: [blankCriteriaOfInterest],
			selectedPlotType: PlotValue.PERCENTAGE,
			selectedCompareOption: CompareValue.IMPACT,
			selectedBaselineDatasetId: null,
			chartSettings: null
		};
		return init;
	}
};
