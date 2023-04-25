export interface Operation {
	type: string;
}

export interface Node {
	id: string;
	workflowId: string;
	operation: Operation;
}
