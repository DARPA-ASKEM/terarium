<template>
	<div :class="!customPosition && position">
		<slot />
		<div class="tooltip-content" :style="customPositionStyle">
			<slot name="tooltip-content" />
		</div>
	</div>
</template>

<script setup lang="ts">
import { PropType, computed } from 'vue';

const props = defineProps({
	position: {
		type: String as PropType<'top' | 'left' | 'right' | 'bottom'>,
		default: 'right'
	},
	// Custom configurations, useful if you want similar behavior to a context menu
	// See it used like this in tera-model-diagram.vue
	customPosition: {
		type: Object as PropType<{ x: number; y: number }> | null,
		default: null
	},
	showTooltip: {
		type: Boolean,
		default: true
	}
});

const customPositionStyle = computed(() => {
	if (props.customPosition) {
		return {
			top: `${props.customPosition.y}px`,
			left: `${props.customPosition.x}px`,
			display: props.showTooltip ? 'block' : 'none'
		};
	}
	return {};
});
</script>

<style scoped>
div {
	position: relative;
	z-index: 1;

	& > .tooltip-content {
		display: none;
		position: absolute;
		background-color: var(--surface-section);
		padding: var(--gap-small);
		border-radius: var(--border-radius);
		box-shadow: var(--box-shadow);
		outline: 1px solid red;
	}

	&:hover > .tooltip-content {
		display: block;
	}

	&.top > .tooltip-content {
		bottom: 100%;
		left: 50%;
		transform: translateX(-50%);
	}

	&.left > .tooltip-content {
		right: 100%;
		top: 50%;
		transform: translateY(-50%);
	}

	&.right > .tooltip-content {
		left: 100%;
		top: 50%;
		transform: translateY(-50%);
	}

	&.bottom > .tooltip-content {
		top: 100%;
		left: 50%;
		transform: translateX(-50%);
	}
}
</style>
