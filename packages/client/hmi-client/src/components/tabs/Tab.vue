<script setup lang="ts">
import { computed } from 'vue';
import IconClose from '@carbon/icons-vue/es/close/16';

const props = defineProps({
	name: {
		type: String,
		required: true,
		default: 'New tab'
	},
	index: {
		type: Number,
		required: true,
		default: 0
	},
	isActive: {
		type: Boolean,
		default: false
	}
});

defineEmits(['clickTabHeader', 'clickTabClose']);

const headerStyle = computed(() => `left: ${10 * props.index}%;}`);
</script>

<template>
	<section class="outer-container">
		<header :style="headerStyle">
			<div @click="$emit('clickTabHeader', index)" :class="{ active: isActive }">
				<span>
					<slot name="tabIcon"></slot>
				</span>
				<span>{{ name }}</span>
				<span>
					<IconClose class="close" @click.stop="$emit('clickTabClose', index)" />
				</span>
			</div>
		</header>
		<section class="content" :class="{ active: isActive }">
			<slot></slot>
		</section>
	</section>
</template>

<style scoped>
header {
	width: 10%;
	position: relative;
	z-index: 1;
}

div {
	display: inline-flex;
	padding: 0.5rem;
	border-top-left-radius: 0.5rem;
	border-top-right-radius: 0.5rem;
	background-color: transparent;
	width: 100%;
	position: relative;
	justify-content: space-between;
}

div.active {
	background-color: white;
}

div:hover:not(.active) {
	background-color: var(--un-color-body-surface-secondary);
}

section {
	display: flex;
	flex-direction: column;
}

.outer-container {
	margin: 0.5rem;
	position: absolute;
}

.content {
	background-color: white;
	visibility: hidden;
}

.content.active {
	visibility: visible;
}

span {
	display: inline-flex;
	align-items: center;
}

.close {
	border-radius: 8px;
}

.close:hover {
	background-color: var(--un-color-black-20);
}
</style>
