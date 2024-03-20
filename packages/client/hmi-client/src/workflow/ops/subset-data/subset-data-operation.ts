import { Operation, WorkflowOperationTypes } from '@/types/workflow';

export interface SubsetDataOperationState {
	datasetId: string | null;
	fromDate: Date;
	toDate: Date;
	latitudeStart: number;
	latitudeEnd: number;
	longitudeStart: number;
	longitudeEnd: number;
	isSpatialSkipping: boolean;
	spatialSkipping: number | null;
	isTimeSkipping: boolean;
	timeSkipping: number | null;
	notebookSessionId?: string;
}

export const SubsetDataOperation: Operation = {
	name: WorkflowOperationTypes.SUBSET_DATA,
	displayName: 'Subset data',
	description: '',
	inputs: [{ type: 'datasetId', label: 'Dataset' }],
	outputs: [{ type: 'datasetId' }],
	isRunnable: true,
	action: () => {},
	initState: () => {
		const init: SubsetDataOperationState = {
			datasetId: null,
			fromDate: new Date(),
			toDate: new Date(),
			latitudeStart: 0,
			latitudeEnd: 0,
			longitudeStart: 0,
			longitudeEnd: 0,
			isSpatialSkipping: false,
			spatialSkipping: null,
			isTimeSkipping: false,
			timeSkipping: null
		};
		return init;
	}
};
