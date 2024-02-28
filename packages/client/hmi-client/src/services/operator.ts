import { WorkflowNode } from '@/types/workflow';
import { isEmpty } from 'lodash';

/**
 * Get the active output state
 */
function getActiveOutputState<S>(node: WorkflowNode<S>): Partial<S> | null {
	if (!!node.active && !isEmpty(node.outputs)) {
		const activeOutput = node.outputs.find((output) => output.id === node.active);
		if (activeOutput?.state) return activeOutput.state;
	}
	return null;
}

export default {
	getActiveOutputState
};
