import { Workflow, WorkflowPort, Operation, WorkflowNode, WorkflowPortStatus } from '@/types/workflow';
import * as workflowService from '@/services/workflow';

import { describe, expect, it } from 'vitest';
import _ from 'lodash';

const addOperation: Operation = {
	name: 'add',
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

const testOp: Operation = {
	name: 'test' as any,
	displayName: 'test',
	description: 'test',
	inputs: [{ type: 'number' }],
	outputs: [{ type: 'number' }],
	isRunnable: true
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

describe('Workflow basics', () => {
	it('new workflow', () => {
		const wf = new workflowService.WorkflowWrapper();
		expect(wf.getEdges().length).toEqual(0);
		expect(wf.getNodes().length).toEqual(0);
	});
});

describe('basic tests to make sure it all works', () => {
	it('simple workflow test (1 + 2) + (3 + 4)', () => {
		// Make a workflow we are going to do (1 + 2) + (3 + 4)
		// X:add(1,2) \
		//             ---> Z:add(X, Y)
		// Y:add(3,4) /

		const wf = new workflowService.WorkflowWrapper();
		const X = wf.addNode(addOperation, { x: 0, y: 0 }, {});
		const Y = wf.addNode(addOperation, { x: 0, y: 0 }, {});
		const Z = wf.addNode(addOperation, { x: 0, y: 0 }, {});

		X.inputs[0].value = [1];
		X.inputs[1].value = [2];

		Y.inputs[0].value = [3];
		Y.inputs[1].value = [4];

		runNode(X); // should be 3
		runNode(Y); // should be 7

		wf.addEdge(X.id, X.outputs[0].id, Z.id, Z.inputs[0].id, []);
		wf.addEdge(Y.id, Y.outputs[0].id, Z.id, Z.inputs[1].id, []);

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
	const wf = new workflowService.WorkflowWrapper();
	const n1 = wf.addNode(testOp, { x: 0, y: 0 }, {});
	const n2 = wf.addNode(testOp, { x: 300, y: 0 }, {});
	const n3 = wf.addNode(testOp, { x: 600, y: 0 }, {});
	const n4 = wf.addNode(testOp, { x: 900, y: -200 }, {});
	const n5 = wf.addNode(testOp, { x: 900, y: 200 }, {});

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

	wf.addEdge(n1.id, 'n1o', n2.id, 'n2i', []);
	wf.addEdge(n2.id, 'n2o', n3.id, 'n3i', []);
	wf.addEdge(n3.id, 'n3o', n4.id, 'n4i', []);
	wf.addEdge(n3.id, 'n3o', n5.id, 'n5i', []);

	it('bootstrapped workflow programmatically', () => {
		expect(wf.getNodes().length).to.eq(5);
		expect(wf.getEdges().length).to.eq(4);
		expect(sanityCheck(wf.dump())).to.eq(true);
	});

	it('duplicate linear flow', () => {
		const testWf = _.cloneDeep(wf);
		testWf.branchWorkflow(n1.id);
		expect(testWf.getNodes().length).to.eq(10);
		expect(testWf.getEdges().length).to.eq(8);
		expect(sanityCheck(testWf.dump())).to.eq(true);
	});

	it('duplicate tail operator', () => {
		const testWf = _.cloneDeep(wf);
		testWf.branchWorkflow(n5.id);
		expect(testWf.getNodes().length).to.eq(6);
		expect(testWf.getEdges().length).to.eq(5);
		expect(sanityCheck(testWf.dump())).to.eq(true);
	});

	it('duplicate at fork', () => {
		const testWf = _.cloneDeep(wf);
		testWf.branchWorkflow(n3.id);
		expect(testWf.getNodes().length).to.eq(8);
		expect(testWf.getEdges().length).to.eq(7);
		expect(testWf.getEdges().filter((edge) => edge.source === n2.id).length).to.eq(2);
		expect(sanityCheck(testWf.dump())).to.eq(true);
	});

	it('bad duplication', () => {
		const testWf = _.cloneDeep(wf);
		testWf.branchWorkflow('does not exist');
		expect(testWf.getNodes().length).to.eq(5);
		expect(testWf.getEdges().length).to.eq(4);
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
	const wf = new workflowService.WorkflowWrapper();
	const n1 = wf.addNode(testOp, { x: 0, y: 800 }, {});
	const n2 = wf.addNode(testOp, { x: 0, y: 0 }, {});
	const n3 = wf.addNode(testOp, { x: 300, y: 400 }, {});
	const n4 = wf.addNode(testOp, { x: 600, y: 40 }, {});
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

	wf.addEdge(n1.id, 'n1o', n3.id, 'n3i_1', []);
	wf.addEdge(n2.id, 'n2o', n3.id, 'n3i_2', []);
	wf.addEdge(n3.id, 'n3o', n4.id, 'n4i', []);

	it('bootstrapped workflow programmatically', () => {
		expect(wf.getNodes().length).to.eq(4);
		expect(wf.getEdges().length).to.eq(3);
		expect(sanityCheck(wf.dump())).to.eq(true);
	});

	it('duplicate at fork', () => {
		const testWf = _.cloneDeep(wf);
		testWf.branchWorkflow(n3.id);
		expect(testWf.getNodes().length).to.eq(6);
		expect(testWf.getEdges().length).to.eq(6);
		expect(testWf.getEdges().filter((edge) => edge.source === n1.id).length).to.eq(2);
		expect(testWf.getEdges().filter((edge) => edge.source === n2.id).length).to.eq(2);
		expect(sanityCheck(testWf.dump())).to.eq(true);
	});
});

describe('workflow operator with multiple output types', () => {
	const commonFields = {
		name: 'test' as any,
		displayName: 'test',
		description: 'test',
		isRunnable: true
	};

	const multiOutputOp: Operation = {
		...commonFields,
		inputs: [],
		outputs: [{ type: 'datasetId|modelId' }]
	};

	const datasetOp: Operation = {
		...commonFields,
		inputs: [{ type: 'datasetId' }],
		outputs: []
	};

	const modelConfigOp: Operation = {
		...commonFields,
		inputs: [{ type: 'modelId' }],
		outputs: []
	};

	const edgeCaseOp: Operation = {
		...commonFields,
		inputs: [{ type: 'modelId|datasetId|number' }],
		outputs: []
	};

	const wf = new workflowService.WorkflowWrapper();

	const multiOutputNode = wf.addNode(multiOutputOp, { x: 0, y: 0 }, {});
	const datasetNode = wf.addNode(datasetOp, { x: 0, y: 0 }, {});
	const modelNode = wf.addNode(modelConfigOp, { x: 0, y: 0 }, {});
	const testNode = wf.addNode(testOp, { x: 0, y: 0 }, {});
	const edgeCaseNode = wf.addNode(edgeCaseOp, { x: 0, y: 0 }, {});
	multiOutputNode.outputs[0].value = [
		{
			datasetId: 'dataset xyz',
			modelId: 'model abc'
		}
	];

	it('dataset|model => dataset', () => {
		wf.addEdge(multiOutputNode.id, multiOutputNode.outputs[0].id, datasetNode.id, datasetNode.inputs[0].id, []);

		expect(datasetNode.inputs[0].value).toMatchObject(['dataset xyz']);
		expect(wf.getEdges().length).eq(1);
		wf.removeEdge(wf.getEdges()[0].id);
	});

	it('dataset|model => model', () => {
		wf.addEdge(multiOutputNode.id, multiOutputNode.outputs[0].id, modelNode.id, modelNode.inputs[0].id, []);

		expect(modelNode.inputs[0].value).toMatchObject(['model abc']);
		expect(wf.getEdges().length).eq(1);
		wf.removeEdge(wf.getEdges()[0].id);
	});

	it('dataset|model => test', () => {
		wf.addEdge(multiOutputNode.id, multiOutputNode.outputs[0].id, testNode.id, testNode.inputs[0].id, []);

		expect(testNode.inputs[0].value).toBeNull();
		expect(wf.getEdges().length).eq(0);
	});

	it('edge case many to many', () => {
		wf.addEdge(multiOutputNode.id, multiOutputNode.outputs[0].id, edgeCaseNode.id, edgeCaseNode.inputs[0].id, []);
		expect(edgeCaseNode.inputs[0].value).toBeNull();
		expect(wf.getEdges().length).eq(0);
	});
});
