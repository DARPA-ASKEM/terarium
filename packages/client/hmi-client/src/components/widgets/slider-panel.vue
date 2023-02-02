<template>
	<slider
		:content-width="contentWidth"
		:tab-width="tabWidth"
		:direction="direction"
		:is-open="isOpen"
	>
		<template v-slot:content>
			<div class="slider-header content">
				<i
					:class="`slider-header-item pi ${directionMap[direction].iconOpen}`"
					@click="emit('update:isOpen', false)"
				/>
				<h4 class="slider-header-item">{{ header }}</h4>
			</div>
			<slot name="content"></slot>
		</template>
		<template v-slot:tab>
			<div :class="`slider-tab-header ${direction}`">
				<i
					:class="`slider-header-item pi ${directionMap[direction].iconClosed}`"
					@click="emit('update:isOpen', true)"
				/>
				<h4 class="slider-header-item">{{ header }}</h4>
				<Badge v-if="indicatorValue" :value="indicatorValue" class="resources-count" size="large" />
			</div>
			<slot name="tab"></slot>
		</template>
	</slider>
</template>

<script setup lang="ts">
import Badge from 'primevue/badge';
import Slider from './Slider.vue';

defineProps({
	// slider props
	isOpen: {
		type: Boolean,
		default: true
	},
	direction: {
		type: String,
		default: 'left'
	},
	contentWidth: {
		type: String,
		default: '240px'
	},
	tabWidth: {
		type: String,
		default: '3rem'
	},
	// slider-panel props
	header: {
		type: String,
		default: ''
	},
	indicatorValue: {
		type: Number,
		default: 0
	}
});

const emit = defineEmits(['update:isOpen']);

const directionMap = {
	left: {
		iconOpen: 'pi-angle-double-left',
		iconClosed: 'pi-angle-double-right'
	},
	right: {
		iconOpen: 'pi-angle-double-right',
		iconClosed: 'pi-angle-double-left'
	}
};
</script>

<style scoped>
i {
	font-size: 1.25rem;
	cursor: pointer;
}

.slider-header {
	display: flex;
	align-items: center;
}

.slider-header.content {
	flex-direction: row-reverse;
	justify-content: space-between;
	padding: 16px;
}

.slider-header.tab {
	justify-content: center;
}

i.slider-header-item {
	color: var(--text-color-subdued);
}

.slider-tab-header {
	align-items: center;
	display: flex;
	flex-direction: column;
	padding: 1rem;
	gap: 2rem;
}

.slider-tab-header h4 {
	text-align: left;
	transform-origin: top right;
	line-height: 1em;
	margin-bottom: 1rem;
}

.resources-count {
	background-color: var(--surface-200);
	color: var(--text-color-primary);
	margin-top: 4rem;
}

.slider-tab-header h4 {
	writing-mode: vertical-rl;
	text-orientation: mixed;
}
</style>
