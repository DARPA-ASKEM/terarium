import { DataseriesConfig, RunType } from '@/types/SimulateConfig';
import { CsvAsset, TimeSpan } from '@/types/Types';
import { logger } from '@/utils/logger';

export const getTimespan = (
	dataset?: CsvAsset,
	mapping?: { [key: string]: string }[]
): TimeSpan => {
	let start = 0;
	let end = 90;
	// If we have the min/max timestamp available from the csv asset use it
	if (dataset) {
		let tVar = 'timestamp';
		if (mapping) {
			// if there's a mapping for timestamp, then the model variable is guaranteed to be 'timestamp'
			const tMap = mapping.find((m) => m.modelVariable === 'timestamp');
			if (tMap) {
				tVar = tMap.datasetVariable;
			}
		}
		let tIndex = dataset.headers.indexOf(tVar);
		// if the timestamp column is not found, default to 0 as this is what is assumed to be the default
		// timestamp column in the pyciemss backend
		tIndex = tIndex === -1 ? 0 : tIndex;

		start = dataset.stats?.[tIndex].minValue!;
		end = dataset.stats?.[tIndex].maxValue!;
	}
	return { start, end };
};

export const getGraphDataFromDatasetCSV = (
	dataset: CsvAsset,
	columnVar: string,
	mapping?: { [key: string]: string }[],
	runType?: RunType
): DataseriesConfig | null => {
	// Julia's output has a (t) at the end of the variable name, so we need to remove it
	let v = runType === RunType.Julia ? columnVar.slice(0, -3) : columnVar;
	let tVar = 'timestamp';

	// get the dataset variable from the mapping of model variable to dataset variable if there's a mapping
	if (mapping) {
		if (!(mapping.length === 1 && Object.values(mapping[0]).some((val) => !val))) {
			const varMap = mapping.find((m) => m.modelVariable === columnVar);
			if (varMap) {
				v = varMap.datasetVariable;
			}

			// if there's a mapping for timestamp, then the model variable is guaranteed to be 'timestamp'
			const tMap = mapping.find((m) => m.modelVariable === 'timestamp');
			if (tMap) {
				tVar = tMap.datasetVariable;
			}
		}
	}

	// get  the index of the timestamp column
	let tIndex = dataset.headers.indexOf(tVar);
	// if the timestamp column is not found, default to 0 as this is what is assumed to be the default
	// timestamp column in the pyciemss backend
	tIndex = tIndex === -1 ? 0 : tIndex;

	// get the index of the variable column
	let colIdx: number = -1;
	for (let i = 0; i < dataset.headers.length; i++) {
		if (dataset.headers[i].trim() === v.trim()) {
			colIdx = i;
			break;
		}
	}
	if (colIdx === -1) {
		return null;
	}

	const graphData: DataseriesConfig = {
		// ignore the first row, it's the header
		data: dataset.csv.slice(1).map((datum: string[]) => ({
			x: +datum[tIndex],
			y: +datum[colIdx]
		})),
		label: `${columnVar} - dataset`,
		fill: false,
		borderColor: '#000000',
		borderDash: [3, 3]
	};

	return graphData;
};

/**
 * Update an operator to use a selected OperatorOutput
 * The current state will be saved as a new operator output if it did not exist prior.
 * This will replace the current state of the operator by the selected output.
 */
function selectOutput(operator: WorkflowNode<S>, operatorOutputId: OperatorOutput<S>["id"]) {

	// Check if the current state existed previously in the outputs
	let current = operator.outputs.find(output => output.id === operator.active);
	if (!current) {
		// the current state was never saved in the outputs prior
		current = {
			id: crypto.randomUUID(),
			isSelected: false,
		};
		operator.outputs.push(current);
	}

	// Update the current state within the outputs
	current.state = operator.state;
	current.operatorStatus = operator.status;
	current.timestamp = new Date();

	// Update the Operator state with the selected one
	const selected = operator.outputs.find(output => output.id === operatorOutputId);
	if (selected) {
		operator.state = selected.state;
		operator.status = selected.operatorStatus;
		operator.active = selected.id;
	} else {
		logger.warn(`Operator Output Id ${operatorOutputId} does not exist within ${operator.displayName} Operator ${operator.id}.`);
	}
}

export default { selectOutput }
