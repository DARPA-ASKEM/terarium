<template>
	<main
		ref="operator"
		@mouseenter="interactionStatus = addHover(interactionStatus)"
		@mouseleave="interactionStatus = removeHover(interactionStatus)"
		@mousedown="interactionStatus = addDrag(interactionStatus)"
		@mouseup="interactionStatus = removeDrag(interactionStatus)"
		@focus="() => {}"
		@focusout="() => {}"
	>
		<tera-operator-header
			:name="node.displayName"
			:status="node.status"
			:interaction-status="interactionStatus"
			@open-in-new-window="openInNewWindow"
			@remove-operator="emit('remove-operator', props.node.id)"
			@duplicate-branch="emit('duplicate-branch')"
			@bring-to-front="bringToFront"
		/>
		<tera-operator-inputs
			:inputs="node.inputs"
			@port-mouseover="(event) => mouseoverPort(event, PortDirection.Input)"
			@port-mouseleave="emit('port-mouseleave')"
			@port-selected="
				(input: WorkflowPort, direction: WorkflowDirection) =>
					emit('port-selected', input, direction)
			"
			@remove-edges="(portId: string) => emit('remove-edges', portId)"
		/>
		<section class="content">
			<slot name="body" />
		</section>
		<tera-operator-outputs
			:outputs="node.outputs"
			@port-mouseover="(event) => mouseoverPort(event, PortDirection.Output)"
			@port-mouseleave="emit('port-mouseleave')"
			@port-selected="
				(input: WorkflowPort, direction: WorkflowDirection) =>
					emit('port-selected', input, direction)
			"
			@remove-edges="(portId: string) => emit('remove-edges', portId)"
		/>
	</main>
</template>

<script setup lang="ts">
import type { WorkflowNode, WorkflowPort } from '@/types/workflow';
import { WorkflowDirection } from '@/types/workflow';
import type { Position } from '@/types/common';
import { addHover, removeHover, addDrag, removeDrag } from '@/services/operator-bitmask';
import { ref, onMounted, onBeforeUnmount } from 'vue';
import floatingWindow from '@/utils/floating-window';
import router from '@/router';
import { RouteName } from '@/router/routes';
import TeraOperatorHeader from '@/components/operator/tera-operator-header.vue';
import TeraOperatorInputs from '@/components/operator/tera-operator-inputs.vue';
import TeraOperatorOutputs from '@/components/operator/tera-operator-outputs.vue';

const props = defineProps<{
	node: WorkflowNode<any>;
}>();

const emit = defineEmits([
	'port-selected',
	'port-mouseover',
	'port-mouseleave',
	'remove-operator',
	'remove-edges',
	'resize',
	'duplicate-branch'
]);

enum PortDirection {
	Input,
	Output
}

const operator = ref<HTMLElement>();
const interactionStatus = ref(0); // States will be added to it thorugh bitmasking

let resizeObserver: ResizeObserver | null = null;

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

function mouseoverPort(event: MouseEvent, portType: PortDirection) {
	const el = event.target as HTMLElement;
	const portElement = (el.querySelector('.port') as HTMLElement) ?? el;
	const nodePosition: Position = { x: props.node.x, y: props.node.y };
	const totalOffsetY = portElement.offsetTop + portElement.offsetHeight / 2;
	const w = portType === PortDirection.Input ? 0 : props.node.width;
	const portPosition = {
		x: nodePosition.x + w + portElement.offsetWidth * 0.5,
		y: nodePosition.y + totalOffsetY
	};
	emit('port-mouseover', portPosition);
}

function resizeHandler() {
	emit('resize', props.node);
}

onMounted(() => {
	if (operator.value) {
		resizeObserver = new ResizeObserver(resizeHandler);
		resizeObserver.observe(operator.value);
	}
});

onBeforeUnmount(() => {
	if (operator.value && resizeObserver) {
		resizeObserver.disconnect();
		resizeObserver = null;
	}
});
</script>

<style scoped>
main {
	background-color: var(--surface-section);
	outline: 1px solid var(--surface-border);
	border-radius: var(--border-radius-medium);
	box-shadow: var(--overlayMenuShadow);
	min-width: 15rem;
	transition: box-shadow 80ms ease;

	&:hover {
		box-shadow: var(--overlayMenuShadowHover);
		z-index: 2;
	}

	& > .content {
		padding: 0.5rem;
	}

	&>ul,
	&>.content,
	&>.content:deep(> *)

	/* Assumes that the child put in the slot will be wrapped in its own parent tag */ {
		display: flex;
		flex-direction: column;
		justify-content: space-evenly;
		gap: 0.5rem;
	}

	/* Shared styles between tera-operator-inputs and tera-operator-outputs */
	& > ul {
		padding: 0.5rem 0;
		list-style: none;
		font-size: var(--font-caption);
		color: var(--text-color-subdued);

		&:empty {
			display: none;
		}

		/* Can't nest css within the deep selector */
		&:deep(> li) {
			display: flex;
			flex-direction: column;
			gap: 0.25rem;
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
			color: var(--text-color-primary);
			padding: 0.25rem 0.5rem;
			background-color: var(--surface-0);
			border: solid 1px var(--surface-border);
		}

		&:deep(.port-connected) {
			color: var(--text-color-primary);
		}

		&:deep(.port-container) {
			width: calc(var(--port-base-size) * 1.25);
		}

		&:deep(.port) {
			display: inline-block;
			background-color: var(--surface-100);
			position: relative;
			width: var(--port-base-size);
			height: calc(var(--port-base-size) * 2);
			top: 2px;
		}

		&:deep(.port-connected .port) {
			position: relative;
			width: calc(var(--port-base-size) * 2);
			border: 2px solid var(--surface-border);
			border-radius: var(--port-base-size);
			background-color: var(--surface-100);
		}

		&:deep(.port-connected .port)::after {
			content: '';
			position: absolute;
			/* This is crucial for positioning inside the parent */
			top: 50%;
			left: 50%;
			transform: translate(-50%, -50%);
			/* Centers the pseudo-element */
			width: 10px;
			/* Size of the circle */
			height: 10px;
			/* Size of the circle */
			border-radius: 50%;
			/* Makes it round */
			background-color: var(--text-color-subdued);
			/* Circle color */
		}

		&:deep(.port-connected:hover .port) {
			background-color: var(--text-color-subdued);
		}
	}
}
</style>
