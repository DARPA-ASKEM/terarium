import { WorkflowTransformations } from '@/types/workflow';

export function getLocalStorageTransform(id: string): object | null {
	const terariumWorkflowTransforms = localStorage.getItem('terariumWorkflowTransforms');
	if (!terariumWorkflowTransforms) {
		return null;
	}

	const workflowTransformations: WorkflowTransformations = JSON.parse(terariumWorkflowTransforms);
	let canvasTransform;
	if (workflowTransformations.workflows[id]) {
		canvasTransform = workflowTransformations.workflows[id];
	}
	return canvasTransform;
}

export function setLocalStorageTransform(id: string, canvasTransform) {
	const terariumWorkflowTransforms = localStorage.getItem('terariumWorkflowTransforms');
	if (!terariumWorkflowTransforms) {
		const transformation: WorkflowTransformations = { workflows: { [id]: canvasTransform } };
		localStorage.setItem('terariumWorkflowTransforms', JSON.stringify(transformation));
		return;
	}
	const workflowTransformations = JSON.parse(terariumWorkflowTransforms);
	workflowTransformations.workflows[id] = canvasTransform;
	localStorage.setItem('terariumWorkflowTransforms', JSON.stringify(workflowTransformations));
}
