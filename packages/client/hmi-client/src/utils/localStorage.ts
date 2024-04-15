import { WorkflowTransformations, Transform } from '@/types/workflow';

export function getLocalStorageTransform(id: string): Transform | null {
	const terariumWorkflowTransforms = localStorage.getItem('terariumWorkflowTransforms');
	if (!terariumWorkflowTransforms) {
		return null;
	}

	const workflowTransformations: WorkflowTransformations = JSON.parse(terariumWorkflowTransforms);
	if (!workflowTransformations.workflows[id]) {
		return null;
	}
	return workflowTransformations.workflows[id];
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
