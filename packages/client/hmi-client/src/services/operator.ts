import { WorkflowNode, WorkflowOutput } from '@/types/workflow';
import { isEmpty } from 'lodash';

/**
 * Get the active output
 */
function getActiveOutput<S>(node: WorkflowNode<S>): WorkflowOutput<S> | null {
	if (!!node.active && !isEmpty(node.outputs)) {
		const activeOutput = node.outputs.find((output) => output.id === node.active);
		if (activeOutput) {
			return activeOutput;
		}
	}
	return null;
}

export default {
	getActiveOutput
};
