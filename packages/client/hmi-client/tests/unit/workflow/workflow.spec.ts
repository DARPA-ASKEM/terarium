import { Workflow, WorkflowPort, Operation, WorkflowNode, WorkflowEdge } from '@/workflow/workflow';
import { describe, expect, it } from 'vitest';

const addOperation: Operation = {
	name: 'add',
	description: 'add two numbers',
	inputs: [{ type: 'number' }, { type: 'number' }],
	outputs: [{ type: 'number' }],

	// This is the brain of the operation, it will make changes, API-calls, that type of stuff
	// and returns an outputs to the node
	action: (v: WorkflowPort[]) => {
		if (v.length && v[0].type === 'number') {
			return [{ type: 'number', value: v[0].value + v[1].value }];
		}
		return [{ type: null, value: null }];
	}
};

const workflow: Workflow = {
	id: '0',
	name: 'test',
	description: 'test',
	transform: { x: 0, y: 0, k: 1 },

	nodes: [],
	edges: []
};

const addEdge = (
	wf: Workflow,
	source: string,
	sourcePort: number,
	target: string,
	targetPort: number
) => {
	const d = wf.nodes.find((n) => n.id === source)?.outputs[sourcePort];
	if (d) {
		const targetNode = wf.nodes.find((n) => n.id === target);
		if (targetNode) {
			targetNode.inputs[targetPort] = d;
		}
	}

	const key = `${source}:${sourcePort}-${target}:${targetPort}`;
	const edge: WorkflowEdge = {
		id: key,
		workflowId: '0',
		points: [],

		source,
		sourcePort,
		target,
		targetPort
	};
	wf.edges.push(edge);
};

const operationLib = new Map<string, Operation>();
operationLib.set('add', addOperation);

const runNode = (node: WorkflowNode): void => {
	const opType = node.operationType;
	const operation = operationLib.get(opType);

	if (!operation) return;
	node.outputs = operation.action(node.inputs);
};

const plusNode = (id: string) =>
	({
		id,
		workflowId: '0',
		operationType: 'add',
		inputs: [],
		outputs: [],
		x: 0,
		y: 0,
		width: 0,
		height: 0
	} as WorkflowNode);

describe('basic tests to make sure it all works', () => {
	it('simple workflow test (1 + 2) + (3 + 4)', () => {
		// Make a workflow we are going to do (1 + 2) + (3 + 4)
		// X:add(1,2) \
		//             ---> Z:add(X, Y)
		// Y:add(3,4) /

		const X = plusNode('X');
		const Y = plusNode('Y');
		const Z = plusNode('Z');
		workflow.nodes.push(X);
		workflow.nodes.push(Y);
		workflow.nodes.push(Z);

		// Pretend we are linking connections
		X.inputs.push({ type: 'number', value: 1 });
		X.inputs.push({ type: 'number', value: 2 });

		Y.inputs.push({ type: 'number', value: 3 });
		Y.inputs.push({ type: 'number', value: 4 });

		runNode(X); // should be 3
		runNode(Y); // should be 7

		addEdge(workflow, 'X', 0, 'Z', 0);
		addEdge(workflow, 'Y', 0, 'Z', 1);
		runNode(Z); // should be 10

		// Run
		expect(Z.outputs.length).to.eq(1);
		expect(Z.outputs[0].value).to.eq(10);
	});
});
