import { WorkflowNode, WorkflowOperationTypes } from '@/types/workflow';

const VIRIDIS_14 = [
	'#440154',
	'#481c6e',
	'#453581',
	'#3d4d8a',
	'#34618d',
	'#2b748e',
	'#24878e',
	'#1f998a',
	'#25ac82',
	'#40bd72',
	'#67cc5c',
	'#98d83e',
	'#cde11d',
	'#fde725'
];

export const getInputLabelColor = (edgeIdx: number, node: WorkflowNode<any>) => {
	const numRuns = node.inputs[0].value?.length ?? 0;
	return numRuns > 1 && node.operationType === WorkflowOperationTypes.SIMULATE_JULIA
		? VIRIDIS_14[Math.floor((edgeIdx / numRuns) * VIRIDIS_14.length)]
		: 'inherit';
};

export const getVariableColorByRunIdx = (edgeIdx: number, numOfEdges: number) =>
	numOfEdges > 1 ? VIRIDIS_14[Math.floor((edgeIdx / numOfEdges) * VIRIDIS_14.length)] : '#1B8073';
