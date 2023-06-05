<template>
	<main :style="nodeStyle" ref="workflowNode">
		<header>
			<h5 class="truncate">{{ node.operationType }} ({{ node.statusCode }})</h5>
			<span>
				<Button
					icon="pi pi-sign-in"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="showNodeDrilldown"
				/>
				<!-- 3-dot options menu -->
				<Button
					icon="pi pi-ellipsis-v"
					class="p-button-icon-only p-button-text p-button-rounded"
					@click="toggleNodeMenu"
				/>
				<Menu ref="nodeMenu" :model="nodeMenuItems" :popup="true" />
			</span>
		</header>
		<ul class="inputs">
			<li
				v-for="(input, index) in node.inputs"
				:key="index"
				:class="{ 'port-connected': input.status === WorkflowPortStatus.CONNECTED }"
			>
				<div
					class="input-port-container"
					@mouseover="(event) => mouseoverPort(event)"
					@mouseleave="emit('port-mouseleave')"
					@click.stop="emit('port-selected', input, WorkflowDirection.FROM_INPUT)"
					@focus="() => {}"
					@focusout="() => {}"
				>
					<div class="input port" />
					{{ input.label }}
				</div>
			</li>
		</ul>
		<section>
			<slot name="body" />
		</section>
		<ul class="outputs">
			<li
				v-for="(output, index) in node.outputs"
				:key="index"
				:class="{ 'port-connected': output.status === WorkflowPortStatus.CONNECTED }"
			>
				<div
					class="output-port-container"
					@mouseover="(event) => mouseoverPort(event)"
					@mouseleave="emit('port-mouseleave')"
					@click.stop="emit('port-selected', output, WorkflowDirection.FROM_OUTPUT)"
					@focus="() => {}"
					@focusout="() => {}"
					:active="openedWorkflowNodeStore.selectedOutputIndex === index"
					@click="openedWorkflowNodeStore.selectedOutputIndex = index"
				>
					<div class="output port" />
					{{ output.label }}
				</div>
			</li>
		</ul>
	</main>
</template>

<script setup lang="ts">
import { Position, WorkflowNode, WorkflowPortStatus, WorkflowDirection } from '@/types/workflow';
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import Button from 'primevue/button';
import { useOpenedWorkflowNodeStore } from '@/stores/opened-workflow-node';
import { isEmpty } from 'lodash';
import { ProjectAssetTypes } from '@/types/Project';
import Menu from 'primevue/menu';

const props = defineProps<{
	node: WorkflowNode;
	canDrag: boolean;
}>();

const emit = defineEmits([
	'dragging',
	'port-selected',
	'port-mouseover',
	'port-mouseleave',
	'remove-node'
]);

const nodeStyle = computed(() => ({
	minWidth: `${props.node.width}px`,
	top: `${props.node.y}px`,
	left: `${props.node.x}px`
}));

const portBaseSize: number = 8;
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

	if (!props.canDrag) {
		stopDrag();
	}
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

function showNodeDrilldown() {
	let pageType;
	let assetId;
	switch (props.node.operationType) {
		case 'SimulateOperation':
			pageType = ProjectAssetTypes.SIMULATIONS;
			assetId = props.node.id;
			openedWorkflowNodeStore.setDrilldown(assetId, pageType, props.node);
			break;
		default:
			break;
	}
	if (!isEmpty(props.node.outputs)) {
		switch (props.node.operationType) {
			case 'ModelOperation':
				pageType = ProjectAssetTypes.MODELS;
				assetId = props.node.outputs[props.node.outputs.length - 1].value?.[0].model.id.toString();
				break;
			case 'Dataset':
				pageType = ProjectAssetTypes.DATASETS;
				assetId = props.node.outputs[0].value?.[0].toString();
				break;
			default:
				break;
		}
		openedWorkflowNodeStore.setDrilldown(assetId, pageType, props.node);
	}
}

function mouseoverPort(event) {
	const el = event.target as HTMLElement;
	const portElement = (el.firstChild as HTMLElement) ?? el;
	const portDirection = portElement.className.split(' ')[0];
	const nodePosition: Position = { x: props.node.x, y: props.node.y };
	const totalOffsetX = portElement.offsetLeft + (portDirection === 'input' ? 0 : portBaseSize);
	const totalOffsetY = portElement.offsetTop + portElement.offsetHeight / 2;
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

function removeNode() {
	console.log(props.node);
	emit('remove-node', props.node.id);
}
function bringToFront() {
	// TODO: bring to front
	// maybe there can be a z-index variable in the parent component
	// and we can just increment it here, and add a z-index style to the node
	// console.log('bring to front');
}

/*
 * User Menu
 */
const nodeMenu = ref();
const nodeMenuItems = ref([
	{ icon: 'pi pi-clone', label: 'Bring to front', command: bringToFront },
	{ icon: 'pi pi-trash', label: 'Remove', command: removeNode }
]);
const toggleNodeMenu = (event) => {
	nodeMenu.value.toggle(event);
};
</script>

<style scoped>
main {
	background-color: var(--surface-section);
	outline: 1px solid var(--surface-border);
	border-radius: var(--border-radius);
	position: absolute;
	width: 20rem;
	user-select: none;
	box-shadow: var(--overlayMenuShadow);
}

main:hover {
	box-shadow: var(--overlayMenuShadowHover);
	z-index: 2;
}

main:hover > header {
	background-color: var(--node-header-hover);
}

header {
	display: flex;
	padding: 0.25rem 0.25rem 0.25rem 1rem;
	justify-content: space-between;
	align-items: center;
	color: var(--gray-0);
	background-color: var(--node-header);
	white-space: nowrap;
	border-top-right-radius: var(--border-radius);
	border-top-left-radius: var(--border-radius);
}
header:hover {
	background-color: var(--node-header-hover);
	cursor: move;
}

.truncate {
	overflow: hidden;
	text-overflow: ellipsis;
}

header .p-button.p-button-icon-only,
header .p-button.p-button-text:enabled:hover {
	color: var(--gray-0);
	width: 1.5rem;
	margin-right: 0.25rem;
}

header .p-button.p-button-text:enabled:hover {
	color: var(--surface-highlight);
}

section {
	margin-left: 1rem;
	margin-right: 1rem;
}

section,
ul {
	display: flex;
	flex-direction: column;
	justify-content: space-evenly;
}

ul {
	margin: 0.25rem 0;
	list-style: none;
	font-size: var(--font-caption);
}

ul li {
	display: flex;
	gap: 0.5rem;
	align-items: center;
}

.input-port-container {
	display: flex;
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	padding-right: 0.5rem;
	gap: 0.5rem;
}

.output-port-container {
	display: flex;
	gap: 0.5rem;
	margin-left: 0.5rem;
	flex-direction: row-reverse;
	padding-left: 0.75rem;
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	border-radius: var(--border-radius) 0 0 var(--border-radius);
}

.output-port-container:hover {
	cursor: pointer;
	background-color: var(--surface-highlight);
}

.output-port-container[active='false'] {
	color: var(--text-color-secondary);
}
.output-port-container[active='true'] {
	color: var(--text-color-primary);
}

.port {
	display: inline-block;
	border: 2px solid var(--surface-border);
	background: var(--surface-100);
	position: relative;
	width: var(--port-base-size);
	height: calc(var(--port-base-size) * 2);
}

.port-connected .input.port,
.port-connected .output.port {
	width: calc(var(--port-base-size) * 2);
	height: calc(var(--port-base-size) * 2);
	border: 2px solid var(--primary-color);
	border-radius: var(--port-base-size);
}

.port-connected .input.port {
	left: calc(-1 * var(--port-base-size));
}

.port-connected .output.port {
	left: var(--port-base-size);
}

.port:hover {
	background: var(--primary-color);
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
	background: var(--surface-0);
}

.port-connected .port {
	background-color: var(--primary-color);
}

.inputs > li:hover .port,
.outputs > li:hover .port {
	background: var(--surface-border);
}

.inputs > .port-connected:hover .port,
.outputs > .port-connected:hover .port {
	background: var(--primary-color);
}
</style>
