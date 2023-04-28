<template>
	<infinite-canvas debug-mode @contextmenu="toggleContextMenu" @save-transform="saveTransform">
		<template #foreground></template>
		<template #data>
			<tera-model-node v-if="models" :models="models" />
			<ContextMenu ref="contextMenu" :model="contextMenuItems" />
			<tera-workflow-node v-for="(node, index) in nodes" :node="node" :key="index" />
		</template>
	</infinite-canvas>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import InfiniteCanvas from '@/components/widgets/tera-infinite-canvas.vue';
import TeraModelNode from '@/components/workflow/tera-model-node.vue';
import { IProject } from '@/types/Project';
import { Operation, WorkflowNode, WorkflowStatus } from '@/types/workflow';
import TeraWorkflowNode from '@/temp/tera-workflow-node.vue';
import ContextMenu from 'primevue/contextmenu';

const props = defineProps<{
	project: IProject;
}>();

const models = computed(() => props.project?.assets?.models);

const nodes = ref<WorkflowNode[]>([]);
const contextMenu = ref();
const newNodePosition = ref<{ x: number; y: number }>({ x: 0, y: 0 });
let canvasTransform = { x: 0, y: 0, k: 1 };

const testOperation: Operation = {
	name: 'Test operation',
	description: 'A test operation',
	inputs: [
		{ type: 'number', label: 'Input one' },
		{ type: 'number', label: 'Input two' },
		{ type: 'number', label: 'Input three' }
	],
	outputs: [
		{ type: 'number', label: 'Output one' },
		{ type: 'number', label: 'Output two' }
	],
	action: () => {},
	isRunnable: true
};

function insertNode(operation: Operation) {
	const newNode: WorkflowNode = {
		id: nodes.value.length.toString(),
		workflowId: '0',
		operationType: operation.name,
		x: newNodePosition.value.x,
		y: newNodePosition.value.y,
		width: 100,
		height: 100,
		inputs: operation.inputs.map((o, i) => ({ id: i.toString(), ...o })),
		outputs: operation.outputs.map((o, i) => ({ id: i.toString(), ...o })),
		statusCode: WorkflowStatus.INVALID
	};
	nodes.value.push(newNode);
}

const contextMenuItems = ref([
	{
		label: 'New operation',
		command: () => {
			insertNode(testOperation);
		}
	}
]);

function toggleContextMenu(event) {
	contextMenu.value.show(event);
	newNodePosition.value = {
		x: (event.offsetX - canvasTransform.x) / canvasTransform.k,
		y: (event.offsetY - canvasTransform.y) / canvasTransform.k
	};
}

function saveTransform(newTransform: { k: number; x: number; y: number }) {
	canvasTransform = newTransform;
}
</script>
