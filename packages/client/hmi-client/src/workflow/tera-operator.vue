<template>
	<main
		:style="nodeStyle"
		ref="operator"
		@mouseenter="interactionStatus = addHover(interactionStatus)"
		@mouseleave="interactionStatus = removeHover(interactionStatus)"
		@focus="() => {}"
		@focusout="() => {}"
	>
		<tera-operator-header
			:name="node.displayName"
			:status="node.status"
			:interaction-status="interactionStatus"
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
import { addHover, removeHover, addDrag, removeDrag, isDrag } from '@/services/operator-bitmask';
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import floatingWindow from '@/utils/floating-window';
import router from '@/router';
import { RouteName } from '@/router/routes';
import TeraOperatorHeader from './operator/tera-operator-header.vue';
import TeraOperatorInputs from './operator/tera-operator-inputs.vue';
import TeraOperatorOutputs from './operator/tera-operator-outputs.vue';

enum PortDirection {
	Input,
	Output
}

const props = defineProps<{
	node: WorkflowNode<any>;
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

const interactionStatus = ref(0); // States will be added to it thorugh bitmasking
let tempX = 0;
let tempY = 0;

const startDrag = (evt: MouseEvent) => {
	tempX = evt.x;
	tempY = evt.y;
	interactionStatus.value = addDrag(interactionStatus.value);
};

const drag = (evt: MouseEvent) => {
	if (!isDrag(interactionStatus.value)) return;

	const dx = evt.x - tempX;
	const dy = evt.y - tempY;

	emit('dragging', { x: dx, y: dy });

	tempX = evt.x;
	tempY = evt.y;
};

const stopDrag = (/* evt: MouseEvent */) => {
	tempX = 0;
	tempY = 0;
	interactionStatus.value = removeDrag(interactionStatus.value);
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

	&:hover {
		box-shadow: var(--overlayMenuShadowHover);
		z-index: 2;
	}

	& > .content {
		margin: 0.5rem;
	}

	& > ul, 
	& > .content, 
	& > .content:deep(> *)  /* Assumes that the child put in the slot will be wrapped in its own parent tag */ {
		display: flex;
		flex-direction: column;
		justify-content: space-evenly;
		gap: 0.5rem;
	}

	/* Shared styles between tera-operator-inputs and tera-operator-outputs */
	& > ul {
		margin: 0.5rem 0;
		list-style: none;
		font-size: var(--font-caption);
		color: var(--text-color-secondary);

		&:empty {
			display: none;
		}

		/* Can't nest css within the deep selector */
		&:deep(> li) {
			display: flex;
			flex-direction: column;
			gap: 0.25rem;
			width: fit-content;
			cursor: pointer;
		}
		&:deep(> li:hover) {
			background-color: var(--surface-highlight);
		}
		&:deep(li:hover .port) {
			/* Not sure what color was intended */
			background-color: var(--primary-color);
			background-color: var(--surface-border);
		}
		&:deep(> li > section) {
			display: flex;
			align-items: center;
			height: calc(var(--port-base-size) * 2);
			gap: 0.25rem;
		}

		&:deep(.p-button.p-button-sm) {
			font-size: var(--font-caption);
			min-width: fit-content;
			padding: 0 0.25rem;
		}

		&:deep(.unlink) {
			display: none;
		}

		&:deep(.port-connected:hover .unlink) {
			display: block;
		}

		&:deep(.port-connected) {
			color: var(--text-color-primary);
		}

		&:deep(.port-container) {
			width: calc(var(--port-base-size) * 2);
		}

		&:deep(.port) {
			display: inline-block;
			background-color: var(--surface-100);
			position: relative;
			width: var(--port-base-size);
			height: calc(var(--port-base-size) * 2);
		}

		&:deep(.port-connected .port) {
			width: calc(var(--port-base-size) * 2);
			border: 2px solid var(--primary-color);
			border-radius: var(--port-base-size);
			background-color: var(--primary-color);
		}
		&:deep(.port-connected:hover .port) {
			background-color: var(--primary-color);
		}
	}
}
</style>
