<template>
	<main :style="nodeStyle" ref="workflowNode">
		<header>
			<tera-operator-header
				:name="node.displayName"
				:status="node.statusCode"
				@open-in-new-window="openDrilldown"
				@remove-node="emit('remove-node', props.node.id)"
			/>
		</header>
		<ul class="inputs">
			<li
				v-for="(input, index) in node.inputs"
				:key="index"
				:class="{ 'port-connected': input.status === WorkflowPortStatus.CONNECTED }"
			>
				<div
					class="input-port-container"
					@mouseenter="(event) => mouseoverPort(event)"
					@mouseleave="emit('port-mouseleave')"
					@click.stop="emit('port-selected', input, WorkflowDirection.FROM_INPUT)"
					@focus="() => {}"
					@focusout="() => {}"
				>
					<div class="input port" />
					<div>
						<!-- if input is empty, show the type. TODO: Create a human readable 'type' to display here -->
						<span v-if="!input.label">{{ input.type }}</span>
						<span
							v-for="(label, labelIdx) in input.label?.split(',') ?? []"
							:key="labelIdx"
							class="input-label"
						>
							{{ label }}
						</span>
					</div>
				</div>
			</li>
		</ul>
		<section>
			<slot name="body" />
			<Button label="Open Drilldown" @click="showNodeDrilldown" severity="secondary" outlined />
		</section>
		<ul class="outputs">
			<li
				v-for="(output, index) in node.outputs"
				:key="index"
				:class="{ 'port-connected': output.status === WorkflowPortStatus.CONNECTED }"
			>
				<div
					class="output-port-container"
					@mouseenter="(event) => mouseoverPort(event)"
					@mouseleave="emit('port-mouseleave')"
					@click.stop="emit('port-selected', output, WorkflowDirection.FROM_OUTPUT)"
					@focus="() => {}"
					@focusout="() => {}"
				>
					<div class="output port" />
					{{ output.label }}
				</div>
			</li>
		</ul>
	</main>
</template>

<script setup lang="ts">
import { Position, WorkflowNode, WorkflowPortStatus, WorkflowDirection } from '@/types/operator';
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import Button from 'primevue/button';
import floatingWindow from '@/utils/floating-window';
import router from '@/router';
import { RouteName } from '@/router/routes';
import TeraOperatorHeader from './operator/tera-operator-header.vue';

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
	'remove-node',
	'drilldown'
]);

const nodeStyle = computed(() => ({
	minWidth: `${props.node.width}px`,
	top: `${props.node.y}px`,
	left: `${props.node.x}px`
}));

const portBaseSize: number = 8;
const workflowNode = ref<HTMLElement>();

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
	emit('drilldown', props.node);
}

function openDrilldown() {
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
	margin-left: 0.5rem;
	margin-right: 0.5rem;
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

.inputs,
.outputs {
	color: var(--text-color-secondary);
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

.port-connected {
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

.port-connected .input-port-container,
.port-connected .output-port-container {
	gap: initial;
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

.input-label::after {
	color: var(--text-color-primary);
	content: ', ';
}

.input-label:last-child::after {
	content: '';
}
</style>
