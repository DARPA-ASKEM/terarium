import { WorkflowNode, WorkflowPort } from '@/types/workflow';
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

/**
 * Get the active output value
 */
function getActiveOutputValue<S>(node: WorkflowNode<S>): WorkflowPort['value'] {
	if (!!node.active && !isEmpty(node.outputs)) {
		const activeOutput = node.outputs.find((output) => output.id === node.active);
		if (activeOutput?.value) return activeOutput.value;
	}
	return null;
}

export default {
	getActiveOutputState,
	getActiveOutputValue
};
