<script setup lang="ts">
import { computed } from 'vue';
import IconClose from '@carbon/icons-vue/es/close/16';
// import IconCloseFilled from `@carbon/icons-vue/es/close--filled/16`;
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const props = defineProps({
	name: {
		type: String,
		// required: true,
		default: 'New tab'
	},
	index: {
		type: Number,
		// required: true,
		default: 0
	},
	isActive: {
		type: Boolean,
		default: false
	}
});

const headerStyle = computed(() => `left: ${10 * props.index}%;}`);

function headerClick() {
	console.log('test');
}
</script>

<template>
	<section class="outer-container">
		<header @click="headerClick" :style="headerStyle">
			<div @click="$emit('clickedTabHeader', index)" :class="{ active: isActive }">
				<span>
					<slot name="tabIcon"></slot>
				</span>
				<span>{{ name }}</span>
				<span>
					<IconClose />
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
	background-color: var(--un-color-body-surface-background);
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
</style>
