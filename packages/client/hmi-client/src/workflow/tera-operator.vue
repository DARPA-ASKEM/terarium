<template>
	<main :style="nodeStyle" ref="operator">
		<tera-operator-header
			:name="node.displayName"
			:status="node.status"
			@open-in-new-window="openInNewWindow"
			@remove-operator="emit('remove-operator', props.node.id)"
			@bring-to-front="bringToFront"
		/>
		<tera-operator-inputs
			:inputs="node.inputs"
			@port-mouseover="(event) => mouseoverPort(event, PortDirection.Input)"
			@port-mouseleave="emit('port-mouseleave')"
			@port-selected="(input: WorkflowPort, direction: WorkflowDirection) => emit('port-selected', input, direction)"
			@remove-edges="(portId: string) => emit('remove-edges', portId)"
		/>
		<section class="content">
			<slot name="body" />
			<Button label="Open Drilldown" @click="emit('drilldown')" severity="secondary" outlined />
		</section>
		<tera-operator-outputs
			:outputs="node.outputs"
			@port-mouseover="(event) => mouseoverPort(event, PortDirection.Output)"
			@port-mouseleave="emit('port-mouseleave')"
			@port-selected="(input: WorkflowPort, direction: WorkflowDirection) => emit('port-selected', input, direction)"
			@remove-edges="(portId: string) => emit('remove-edges', portId)"
		/>
	</main>
</template>

<script setup lang="ts">
import { Position, WorkflowNode, WorkflowDirection, WorkflowPort } from '@/types/workflow';
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import floatingWindow from '@/utils/floating-window';
import router from '@/router';
import { RouteName } from '@/router/routes';
import Button from 'primevue/button';
import TeraOperatorHeader from './operator/tera-operator-header.vue';
import TeraOperatorInputs from './operator/tera-operator-inputs.vue';
import TeraOperatorOutputs from './operator/tera-operator-outputs.vue';

enum PortDirection {
	Input,
	Output
}

const props = defineProps<{
	node: WorkflowNode<any>;
	canDrag: boolean;
	isActive: boolean;
}>();

const emit = defineEmits([
	'dragging',
	'port-selected',
	'port-mouseover',
	'port-mouseleave',
	'remove-operator',
	'remove-edges',
	'drilldown'
]);

const nodeStyle = computed(() => ({
	minWidth: `${props.node.width}px`,
	top: `${props.node.y}px`,
	left: `${props.node.x}px`
}));

const portBaseSize: number = 8;
const operator = ref<HTMLElement>();

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
	if (!operator.value) return;

	operator.value.addEventListener('mousedown', startDrag);
	document.addEventListener('mousemove', drag);
	operator.value.addEventListener('mouseup', stopDrag);
});

function bringToFront() {
	// TODO: bring to front
	// maybe there can be a z-index variable in the parent component
	// and we can just increment it here, and add a z-index style to the node
	// console.log('bring to front');
}

function openInNewWindow() {
	const url = router.resolve({
		name: RouteName.WorkflowNode,
		params: { nodeId: props.node.id, workflowId: props.node.workflowId }
	}).href;
	floatingWindow.open(url);
}

function mouseoverPort(event: MouseEvent, portDirection: PortDirection) {
	const el = event.target as HTMLElement;
	const portElement = (el.querySelector('.port') as HTMLElement) ?? el;
	const nodePosition: Position = { x: props.node.x, y: props.node.y };
	const totalOffsetX =
		portElement.offsetLeft + (portDirection === PortDirection.Input ? 0 : portBaseSize);
	const totalOffsetY = portElement.offsetTop + portElement.offsetHeight / 2;
	const portPosition = { x: nodePosition.x + totalOffsetX, y: nodePosition.y + totalOffsetY };
	emit('port-mouseover', portPosition);
}

onBeforeUnmount(() => {
	if (operator.value) {
		operator.value.removeEventListener('mousedown', startDrag);
		document.removeEventListener('mousemove', drag);
		operator.value.removeEventListener('mouseup', stopDrag);
	}
});
</script>

<style scoped>
main {
	background-color: var(--surface-section);
	outline: 1px solid var(--surface-border);
	border-radius: var(--border-radius-medium);
	position: absolute;
	width: 15rem;
	user-select: none;
	box-shadow: var(--overlayMenuShadow);
}

main:hover {
	box-shadow: var(--overlayMenuShadowHover);
	z-index: 2;
}

main > .content {
	display: flex;
	flex-direction: column;
	justify-content: space-evenly;
	margin: 0 0.5rem;
	gap: 0.5rem;
}

.content:deep(> *),
main > ul {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin: 0.5rem 0;
}

/* Inputs/outputs */
ul {
	list-style: none;
	font-size: var(--font-caption);
	color: var(--text-color-secondary);
}

ul:empty {
	display: none;
}

:deep(ul .p-button.p-button-sm) {
	font-size: var(--font-caption);
	min-width: fit-content;
	padding: 0 0.25rem;
}

:deep(.unlink) {
	display: none;
}

:deep(.port-connected:hover .unlink) {
	display: block;
}

:deep(li) {
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
	width: fit-content;
	cursor: pointer;
}

:deep(li > section) {
	display: flex;
	align-items: center;
	height: calc(var(--port-base-size) * 2);
	gap: 0.25rem;
}

:deep(li:hover) {
	background-color: var(--surface-highlight);
}

:deep(li:hover .port) {
	/* Not sure what color was intended */
	background-color: var(--primary-color);
	background-color: var(--surface-border);
}

:deep(.port-connected) {
	color: var(--text-color-primary);
}

:deep(.port-container) {
	width: calc(var(--port-base-size) * 2);
}

:deep(.port) {
	display: inline-block;
	background-color: var(--surface-100);
	position: relative;
	width: var(--port-base-size);
	height: calc(var(--port-base-size) * 2);
}

:deep(.port-connected .port) {
	width: calc(var(--port-base-size) * 2);
	border: 2px solid var(--primary-color);
	border-radius: var(--port-base-size);
	background-color: var(--primary-color);
}
:deep(.port-connected:hover .port) {
	background-color: var(--primary-color);
}
</style>
