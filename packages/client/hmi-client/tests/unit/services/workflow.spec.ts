import {
	OperatorStatus,
	Workflow,
	WorkflowPort,
	Operation,
	WorkflowNode,
	WorkflowOperationTypes,
	WorkflowPortStatus
} from '@/types/workflow';
import * as workflowService from '@/services/workflow';

import { describe, expect, it } from 'vitest';
import _ from 'lodash';

const addOperation: Operation = {
	name: WorkflowOperationTypes.ADD,
	displayName: 'Add',
	description: 'add two numbers',
	inputs: [{ type: 'number' }, { type: 'number' }],
	outputs: [{ type: 'number' }],
	isRunnable: true,

	// This is the brain of the operation, it will make changes, API-calls, that type of stuff
	// and returns an outputs to the node
	action: (v: WorkflowPort[]) => {
		if (v.length && v[0].type === 'number' && v[0].value && v[1].value) {
			return [{ id: '0', type: 'number', value: [v[0].value[0] + v[1].value[0]] }];
		}
		return [{ id: '0', type: null, value: null }];
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

const operationLib = new Map<string, Operation>();
operationLib.set('add', addOperation);

const runNode = (node: WorkflowNode<any>): void => {
	const opType = node.operationType;
	const operation = operationLib.get(opType);

	if (!operation) return;
	if (operation.action) {
		node.outputs = operation.action(node.inputs);
	}
};

const plusNode = (id: string) =>
	({
		id,
		workflowId: '0',
		operationType: 'add',
		inputs: operationLib.get('add')?.inputs.map((d, i) => ({ id: `${i}`, type: d.type, value: null })),
		outputs: operationLib.get('add')?.inputs.map((d, i) => ({ id: `${i}`, type: d.type, value: null })),
		x: 0,
		y: 0,
		width: 0,
		height: 0,
		status: OperatorStatus.INVALID
	}) as WorkflowNode<any>;

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
		let temp: WorkflowPort | undefined;
		temp = X.inputs.find((d) => d.id === '0');
		if (temp) {
			temp.value = [1];
		}
		temp = X.inputs.find((d) => d.id === '1');
		if (temp) {
			temp.value = [2];
		}
		temp = Y.inputs.find((d) => d.id === '0');
		if (temp) {
			temp.value = [3];
		}
		temp = Y.inputs.find((d) => d.id === '1');
		if (temp) {
			temp.value = [4];
		}

		runNode(X); // should be 3
		runNode(Y); // should be 7

		workflowService.addEdge(workflow, 'X', '0', 'Z', '0', []);
		workflowService.addEdge(workflow, 'Y', '0', 'Z', '1', []);

		runNode(Z); // should be 10

		// Run
		expect(Z.outputs.length).to.eq(1);
		expect(Z.outputs[0].value?.[0]).to.eq(10);
	});
});

describe('operator state sync/staleness check', () => {
	const operatorState = { vars: ['a', 'b', 'c'], extra: 123 };
	const outputState1 = { vars: ['a', 'b', 'c'], constraintGroups: [{ name: 'abc' }] };
	const outputState2 = { vars: ['a', 'b', 'c'] };
	const outputState3 = { vars: ['a', 'b'] };

	it('states unsynced 1', () => {
		expect(workflowService.isOperatorStateInSync(operatorState, outputState1)).to.eq(false);
	});

	it('states unsynced 2', () => {
		expect(workflowService.isOperatorStateInSync(operatorState, outputState3)).to.eq(false);
	});

	it('states synced', () => {
		expect(workflowService.isOperatorStateInSync(operatorState, outputState2)).to.eq(true);
	});
});

const testOp: Operation = {
	name: 'test' as any,
	displayName: 'test',
	description: 'test',
	inputs: [{ type: 'number' }],
	outputs: [{ type: 'number' }],
	isRunnable: true
};

// Helper
const sanityCheck = (wf: Workflow) => {
	const dupe = new Set<string>();
	for (let i = 0; i < wf.nodes.length; i++) {
		const id = wf.nodes[i].id;
		if (dupe.has(id)) return false;
		dupe.add(id);
	}
	return true;
};

describe('workflow copying branch -< fork', () => {
	/**
	 * Pictorially
	 *                  __ n4
	 *                 /
	 *   n1 -- n2 -- n3
	 *                 \__ n5
	 *
	 * */
	const wf = workflowService.emptyWorkflow('test', 'test');
	workflowService.addNode(wf, testOp, { x: 0, y: 0 }, {});
	workflowService.addNode(wf, testOp, { x: 300, y: 0 }, {});
	workflowService.addNode(wf, testOp, { x: 600, y: 0 }, {});
	workflowService.addNode(wf, testOp, { x: 900, y: -200 }, {});
	workflowService.addNode(wf, testOp, { x: 900, y: 200 }, {});

	const n1 = wf.nodes[0];
	const n2 = wf.nodes[1];
	const n3 = wf.nodes[2];
	const n4 = wf.nodes[3];
	const n5 = wf.nodes[4];
	const NC = WorkflowPortStatus.NOT_CONNECTED;

	n1.outputs = [{ id: 'n1o', type: 'number', value: [1], status: NC, isOptional: false }];
	n2.inputs = [{ id: 'n2i', type: 'number', value: [1], status: NC, isOptional: false }];
	n2.outputs = [{ id: 'n2o', type: 'number', value: [2], status: NC, isOptional: false }];
	n3.inputs = [{ id: 'n3i', type: 'number', value: [2], status: NC, isOptional: false }];
	n3.outputs = [{ id: 'n3o', type: 'number', value: [3], status: NC, isOptional: false }];
	n4.inputs = [{ id: 'n4i', type: 'number', value: [3], status: NC, isOptional: false }];
	n4.outputs = [{ id: 'n4o', type: 'number', value: [4], status: NC, isOptional: false }];
	n5.inputs = [{ id: 'n5i', type: 'number', value: [3], status: NC, isOptional: false }];
	n5.outputs = [{ id: 'n5o', type: 'number', value: [5], status: NC, isOptional: false }];

	workflowService.addEdge(wf, n1.id, 'n1o', n2.id, 'n2i', []);
	workflowService.addEdge(wf, n2.id, 'n2o', n3.id, 'n3i', []);
	workflowService.addEdge(wf, n3.id, 'n3o', n4.id, 'n4i', []);
	workflowService.addEdge(wf, n3.id, 'n3o', n5.id, 'n5i', []);

	it('bootstrapped workflow programmatically', () => {
		expect(wf.nodes.length).to.eq(5);
		expect(wf.edges.length).to.eq(4);
		expect(sanityCheck(wf)).to.eq(true);
	});

	it('duplicate linear flow', () => {
		const testWf = _.cloneDeep(wf);
		workflowService.branchWorkflow(testWf, n1.id);
		expect(testWf.nodes.length).to.eq(10);
		expect(testWf.edges.length).to.eq(8);
		expect(sanityCheck(testWf)).to.eq(true);
	});

	it('duplicate tail operator', () => {
		const testWf = _.cloneDeep(wf);
		workflowService.branchWorkflow(testWf, n5.id);
		expect(testWf.nodes.length).to.eq(6);
		expect(testWf.edges.length).to.eq(5);
		expect(sanityCheck(testWf)).to.eq(true);
	});

	it('duplicate at fork', () => {
		const testWf = _.cloneDeep(wf);
		workflowService.branchWorkflow(testWf, n3.id);
		expect(testWf.nodes.length).to.eq(8);
		expect(testWf.edges.length).to.eq(7);
		expect(testWf.edges.filter((edge) => edge.source === n2.id).length).to.eq(2);
		expect(sanityCheck(testWf)).to.eq(true);
	});

	it('bad duplication', () => {
		const testWf = _.cloneDeep(wf);
		workflowService.branchWorkflow(testWf, 'does not exist');
		expect(testWf.nodes.length).to.eq(5);
		expect(testWf.edges.length).to.eq(4);
	});
});

describe('workflow copying branch >- fork', () => {
	/**
	 * Pictorially
	 *
	 *  n1 _
	 *      \
	 *        -- n3 -- n4
	 *  n2 _/
	 *
	 * */
	const wf = workflowService.emptyWorkflow('test', 'test');
	workflowService.addNode(wf, testOp, { x: 0, y: 800 }, {});
	workflowService.addNode(wf, testOp, { x: 0, y: 0 }, {});
	workflowService.addNode(wf, testOp, { x: 300, y: 400 }, {});
	workflowService.addNode(wf, testOp, { x: 600, y: 40 }, {});

	const n1 = wf.nodes[0];
	const n2 = wf.nodes[1];
	const n3 = wf.nodes[2];
	const n4 = wf.nodes[3];
	const NC = WorkflowPortStatus.NOT_CONNECTED;

	n1.outputs = [{ id: 'n1o', type: 'number', value: [1], status: NC, isOptional: false }];
	n2.outputs = [{ id: 'n2o', type: 'number', value: [2], status: NC, isOptional: false }];
	n3.inputs = [
		{
			id: 'n3i_1',
			type: 'number',
			value: [1],
			status: NC,
			isOptional: false
		},
		{
			id: 'n3i_2',
			type: 'number',
			value: [2],
			status: NC,
			isOptional: false
		}
	];
	n3.outputs = [{ id: 'n3o', type: 'number', value: [3], status: NC, isOptional: false }];
	n4.inputs = [{ id: 'n4i', type: 'number', value: [3], status: NC, isOptional: false }];
	n4.outputs = [{ id: 'n4o', type: 'number', value: [4], status: NC, isOptional: false }];

	workflowService.addEdge(wf, n1.id, 'n1o', n3.id, 'n3i_1', []);
	workflowService.addEdge(wf, n2.id, 'n2o', n3.id, 'n3i_2', []);
	workflowService.addEdge(wf, n3.id, 'n3o', n4.id, 'n4i', []);

	it('bootstrapped workflow programmatically', () => {
		expect(wf.nodes.length).to.eq(4);
		expect(wf.edges.length).to.eq(3);
		expect(sanityCheck(wf)).to.eq(true);
	});

	it('duplicate at fork', () => {
		const testWf = _.cloneDeep(wf);
		workflowService.branchWorkflow(testWf, n3.id);
		expect(testWf.nodes.length).to.eq(6);
		expect(testWf.edges.length).to.eq(6);
		expect(testWf.edges.filter((edge) => edge.source === n1.id).length).to.eq(2);
		expect(testWf.edges.filter((edge) => edge.source === n2.id).length).to.eq(2);
		expect(sanityCheck(wf)).to.eq(true);
	});
});

describe('workflow operator with multiple output types', () => {
	const multiOutputOp: Operation = {
		name: 'test' as any,
		displayName: 'test',
		description: 'test',
		inputs: [],
		outputs: [{ type: 'datasetId|modelId' }],
		isRunnable: true
	};

	const datasetOp: Operation = {
		name: 'test' as any,
		displayName: 'test',
		description: 'test',
		inputs: [{ type: 'datasetId' }],
		outputs: [],
		isRunnable: true
	};

	const modelConfigOp: Operation = {
		name: 'test' as any,
		displayName: 'test',
		description: 'test',
		inputs: [{ type: 'modelId' }],
		outputs: [],
		isRunnable: true
	};

	const wf = workflowService.emptyWorkflow('test', 'test');
	workflowService.addNode(wf, multiOutputOp, { x: 0, y: 0 }, {});
	workflowService.addNode(wf, datasetOp, { x: 0, y: 0 }, {});
	workflowService.addNode(wf, modelConfigOp, { x: 0, y: 0 }, {});
	workflowService.addNode(wf, testOp, { x: 0, y: 0 }, {});

	const multiOutputNode = wf.nodes[0];
	multiOutputNode.outputs[0].value = [
		{
			datasetId: 'dataset xyz',
			modelId: 'model abc'
		}
	];

	const datasetNode = wf.nodes[1];
	const modelNode = wf.nodes[2];
	const testNode = wf.nodes[3];

	it('dataset/model => dataset', () => {
		workflowService.addEdge(
			wf,
			multiOutputNode.id,
			multiOutputNode.outputs[0].id,
			datasetNode.id,
			datasetNode.inputs[0].id,
			[]
		);

		expect(datasetNode.inputs[0].value).toMatchObject(['dataset xyz']);
		expect(wf.edges.length).eq(1);
		workflowService.removeEdge(wf, wf.edges[0].id);
	});

	it('dataset/model => model', () => {
		workflowService.addEdge(
			wf,
			multiOutputNode.id,
			multiOutputNode.outputs[0].id,
			modelNode.id,
			modelNode.inputs[0].id,
			[]
		);

		expect(modelNode.inputs[0].value).toMatchObject(['model abc']);
		expect(wf.edges.length).eq(1);
		workflowService.removeEdge(wf, wf.edges[0].id);
	});

	it('dataset/model => test', () => {
		workflowService.addEdge(
			wf,
			multiOutputNode.id,
			multiOutputNode.outputs[0].id,
			testNode.id,
			testNode.inputs[0].id,
			[]
		);

		expect(testNode.inputs[0].value).toBeNull();
		expect(wf.edges.length).eq(0);
	});
});
