<template>
	<tera-slider
		:content-width="contentWidth"
		:tab-width="tabWidth"
		:direction="direction"
		:is-open="isOpen"
	>
		<template v-slot:content>
			<header class="slider-header content">
				<i
					:class="`slider-header-item pi ${directionMap[direction].iconOpen}`"
					@click="emit('update:isOpen', false)"
				/>
				<slot name="header"></slot>
				<div class="column">
					<h4 class="slider-header-item">{{ header }}</h4>
					<slot name="subHeader"></slot>
				</div>
			</header>
			<slot name="content"></slot>
		</template>
		<template v-slot:tab>
			<div :class="`slider-tab-header ${direction}`">
				<i
					:class="`slider-header-item pi ${directionMap[direction].iconClosed}`"
					@click="emit('update:isOpen', true)"
				/>
				<h5 class="slider-header-item">{{ header }}</h5>
				<Badge v-if="indicatorValue" :value="indicatorValue" class="selected-resources-count" />
			</div>
			<slot name="tab"></slot>
		</template>
	</tera-slider>
</template>

<script setup lang="ts">
import Badge from 'primevue/badge';
import TeraSlider from './tera-slider.vue';

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
		default: '40px'
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
	align-items: start;
}

.column {
	display: flex;
	flex-direction: column;
}

.slider-header.content {
	flex-direction: row-reverse;
	justify-content: space-between;
	padding: 1rem;
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
	line-height: 1em;
	margin-bottom: 1rem;
}

.slider-tab-header h5 {
	transform: rotate(180deg);
	writing-mode: vertical-lr;
}

.selected-resources-count {
	background-color: var(--surface-200);
	color: var(--text-color-primary);
	font-size: 1rem;
	min-width: 2rem;
	height: 2rem;
	line-height: 2rem;
	font-weight: var(--font-weight);
}
</style>
