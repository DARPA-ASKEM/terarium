<template>
	<main :style="nodeStyle" ref="workflowNode">
		<header>
			<h5>{{ node.operationType }} ({{ node.statusCode }})</h5>
			<span>
				<Button
					icon="pi pi-ellipsis-v"
					class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
				/>
				<Button
					@click="showNodeDrilldown"
					icon="pi pi-external-link"
					class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
				/>
			</span>
		</header>
		<ul class="inputs">
			<li v-for="(input, index) in node.inputs" :key="index"
				:class="input.status === WorkflowPortStatus.CONNECTED ? 'port-connected' : ''">
				<div class="port" @click.stop="selectPort(input)" @mouseover="(event) => mouseoverPort(event)"
					@focus="() => { }"></div>
				{{ input.label }}
			</li>
		</ul>
		<section>
			<slot name="body" />
		</section>
		<ul class="outputs">
			<li v-for="(output, index) in node.outputs" :key="index"
				:class="output.status === WorkflowPortStatus.CONNECTED ? 'port-connected' : ''">
				{{ output.label }}
				<div class="port" @click.stop="selectPort(output)" @mouseover="(event) => mouseoverPort(event)"
					@focus="() => { }"></div>
			</li>
		</ul>
	</main>
</template>

<script setup lang="ts">
import { Position, WorkflowNode, WorkflowPort, WorkflowPortStatus } from '@/types/workflow';
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import Button from 'primevue/button';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { isEmpty } from 'lodash';

const props = defineProps<{
	node: WorkflowNode;
}>();

const emit = defineEmits(['dragging', 'port-selected', 'port-mouseover']);

const nodeStyle = computed(() => ({
	minWidth: `${props.node.width}px`,
	minHeight: `${props.node.height}px`,
	top: `${props.node.y}px`,
	left: `${props.node.x}px`
}));

const workflowNode = ref<HTMLElement>();
const openedWorkflowNodeStore = useOpenedWorkflowNodeStore();

let tempX = 0;
let tempY = 0;
let dragStart = false;

const startDrag = (evt: MouseEvent) => {
	tempX = evt.x;
	tempY = evt.y;
	dragStart = true;
};

const drag = (evt: MouseEvent) => {
	if (dragStart === false) return;

	const dx = evt.x - tempX;
	const dy = evt.y - tempY;

	emit('dragging', { x: dx, y: dy });

	tempX = evt.x;
	tempY = evt.y;
};

const stopDrag = (/* evt: MouseEvent */) => {
	tempX = 0;
	tempY = 0;
	dragStart = false;
};

onMounted(() => {
	if (!workflowNode.value) return;

	workflowNode.value.addEventListener('mousedown', startDrag);
	document.addEventListener('mousemove', drag);
	workflowNode.value.addEventListener('mouseup', stopDrag);
});

function selectPort(port: WorkflowPort) {
	emit('port-selected', port);
}

// Pass workflow node to drilldown panel
function showNodeDrilldown() {
	if (!isEmpty(props.node.outputs)) {
		// let assetType, assetId;
		// // openedWorkflowNodeStore.setWorkflowNode(props.node);
		// switch (props.node.operationType) {
		// 	case 'ModelOperation':
		// 		assetType = ProjectAssetTypes.MODELS;
		// 		assetId = props.node.outputs[props.node.outputs.length - 1].value.model.id.toString();
		// 		break;
		// 	case 'Dataset':
		// 		assetType = ProjectAssetTypes.DATASETS;
		// 		assetId = props.node.outputs[0].value.toString();
		// 		break;
		// }
		// console.log(assetType);
		// console.log(assetId);
		// if (assetType && assetId) {
		// 	openedWorkflowNodeStore.asset = { id: assetId, type: assetType }
		}
		openedWorkflowNodeStore.setWorkflowNode(props.node);
	} else alert('Node needs a valid output');
}

function mouseoverPort(event) {
	const el = event.target as HTMLElement;
	const nodePosition: Position = { x: props.node.x, y: props.node.y };
	const totalOffsetX = el.offsetLeft;
	const totalOffsetY = el.offsetTop + el.offsetHeight / 2 + 1;
	const portPosition = { x: nodePosition.x + totalOffsetX, y: nodePosition.y + totalOffsetY };
	emit('port-mouseover', portPosition);
}

onBeforeUnmount(() => {
	if (workflowNode.value) {
		workflowNode.value.removeEventListener('mousedown', startDrag);
		document.removeEventListener('mousemove', drag);
		workflowNode.value.removeEventListener('mouseup', stopDrag);
	}
});
</script>

<style scoped>
main {
	background-color: var(--surface-section);
	outline: 1px solid var(--surface-border);
	border-radius: var(--border-radius);
	position: absolute;
	width: 20rem;
	user-select: none;
}

header {
	display: flex;
	padding: 0.25rem 0.5rem;
	justify-content: space-between;
	align-items: center;
	color: var(--gray-0);
	background-color: var(--primary-color);
	white-space: nowrap;
	border-top-right-radius: var(--border-radius);
	border-top-left-radius: var(--border-radius);
}

header .p-button.p-button-icon-only,
header .p-button.p-button-text:enabled:hover {
	color: var(--gray-0);
}

section {
	margin: 0.5rem;
}

section,
ul {
	display: flex;
	flex-direction: column;
	justify-content: space-evenly;
	gap: 0.5rem;
}

ul {
	margin: 0.5rem 0;
	list-style: none;
	font-size: var(--font-caption);
}

ul li {
	display: flex;
	gap: 0.5rem;
	align-items: center;
}

.port {
	display: inline-block;
	height: 16px;
	width: 8px;
	border: 2px solid var(--surface-border);
	position: relative;
	background: var(--surface-100);
}

.port:hover {
	background: var(--surface-border);
}

.inputs .port {
	border-radius: 0 8px 8px 0;
	border-left: none;
}

.outputs .port {
	border-radius: 8px 0 0 8px;
	border-right: none;
}

.outputs {
	align-items: end;
}

.port-connected {
	background: var(--surface-border);
}
</style>
