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
			@port-mouseover="(event) => mouseoverPort(event)"
			@port-mouseleave="emit('port-mouseleave')"
			@port-selected="(input: WorkflowPort, direction: WorkflowDirection) => emit('port-selected', input, direction)"
			@remove-edge="(portId: string) => emit('remove-edge', portId)"
		/>
		<section>
			<slot name="body" />
			<Button label="Open Drilldown" @click="openDrilldown" severity="secondary" outlined />
			<tera-operator-actions :action-buttons="actionButtons" />
		</section>
		<ul class="outputs">
			<li
				v-for="(output, index) in node.outputs"
				:key="index"
				:class="{ 'port-connected': output.status === WorkflowPortStatus.CONNECTED }"
				@mouseenter="(event) => mouseoverPort(event)"
				@mouseleave="emit('port-mouseleave')"
				@click.stop="emit('port-selected', output, WorkflowDirection.FROM_OUTPUT)"
				@focus="() => {}"
				@focusout="() => {}"
			>
				<div class="port" />
				{{ output.label }}
			</li>
		</ul>
	</main>
</template>

<script setup lang="ts">
import {
	Position,
	WorkflowNode,
	WorkflowPortStatus,
	WorkflowDirection,
	WorkflowPort,
	OperatorActionButton
} from '@/types/workflow';
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import Button from 'primevue/button';
import floatingWindow from '@/utils/floating-window';
import router from '@/router';
import { RouteName } from '@/router/routes';
import TeraOperatorHeader from './operator/tera-operator-header.vue';
import TeraOperatorInputs from './operator/tera-operator-inputs.vue';
import TeraOperatorActions from './operator/tera-operator-actions.vue';

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
	'remove-edge',
	'drilldown'
]);

const actionButtons = computed<OperatorActionButton[]>(() => [
	{ label: 'Open drilldown', isPrimary: false, icon: '', action: openDrilldown() }
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

function openDrilldown() {
	emit('drilldown', props.node);
}

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
	width: 20rem;
	user-select: none;
	box-shadow: var(--overlayMenuShadow);
}

main:hover {
	box-shadow: var(--overlayMenuShadowHover);
	z-index: 2;
}

section {
	display: flex;
	flex-direction: column;
	justify-content: space-evenly;
	margin: 0 0.5rem;
}

/* Inputs/outputs */
ul {
	display: flex;
	flex-direction: column;
	justify-content: space-evenly;
	margin: 0.6rem 0;
	gap: 0.6rem;
	list-style: none;
	font-size: var(--font-caption);
	color: var(--text-color-secondary);
}

.outputs {
	align-items: end;
}

.outputs li {
	flex-direction: row-reverse;
	padding-left: 0.75rem;
	border-radius: var(--border-radius) 0 0 var(--border-radius);
}

.outputs .port {
	border-radius: var(--port-base-size) 0 0 var(--port-base-size);
	border: 2px solid var(--surface-border);
	border-right: none;
}

.inputs .port-connected .port,
.outputs .port-connected .port {
	border-radius: var(--port-base-size);
}

.outputs .port-connected .port {
	left: var(--port-base-size);
}

:deep(li) {
	display: flex;
	width: fit-content;
	align-items: center;
	cursor: pointer;
	height: calc(var(--port-base-size) * 2);
	gap: 0.25rem;
}

:deep(li:hover) {
	background-color: var(--surface-highlight);
}

:deep(li:hover > .port) {
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
