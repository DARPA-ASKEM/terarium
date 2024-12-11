import { WorkflowOperationTypes } from '@/types/workflow';
import type { Operation, BaseState } from '@/types/workflow';
import { ChartSetting } from '@/types/common';

export enum TimepointOption {
	LAST = 'last',
	FIRST = 'first'
}
export enum RankOption {
	MINIMUM = 'minimum',
	MAXIMUM = 'maximum'
}

export enum PlotValue {
	TRAJECTORY = 'trajectory',
	PERCENTAGE = 'percentage',
	VALUE = 'value'
}

export enum CompareValue {
	IMPACT = 'impact',
	RANK = 'rank'
}

export const blankCriteriaOfInterest = {
	name: 'Criteria of interest',
	configurations: [],
	selectedConfiguration: null,
	variables: [],
	selectedVariable: null,
	rank: RankOption.MINIMUM,
	timepoint: TimepointOption.LAST
};
export interface CriteriaOfInterestCard {
	name: string;
	configurations: string[];
	selectedConfiguration: string | null;
	variables: string[];
	selectedVariable: string | null;
	rank: RankOption;
	timepoint: TimepointOption;
}
export interface CompareDatasetsState extends BaseState {
	criteriaOfInterestCards: CriteriaOfInterestCard[];
	selectedPlotValue: PlotValue;
	selectedCompareOption: CompareValue;
	selectedDataset: string | null;
	chartSettings: ChartSetting[] | null;
}

export const CompareDatasetsOperation: Operation = {
	name: WorkflowOperationTypes.COMPARE_DATASETS,
	displayName: 'Compare datasets',
	description: 'Compare datasets, or simulation results',
	documentationUrl: '',
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
			selectedPlotValue: PlotValue.PERCENTAGE,
			selectedCompareOption: CompareValue.IMPACT,
			selectedDataset: null,
			chartSettings: null
		};
		return init;
	}
};
