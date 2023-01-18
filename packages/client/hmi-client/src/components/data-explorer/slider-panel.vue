<template>
	<slider
		:content-width="contentWidth"
		:tab-width="tabWidth"
		:direction="direction"
		:is-open="isOpen"
	>
		<template v-slot:content>
			<div class="slider-header content">
				<IconChevronLeft24 class="slider-header-item" @click="emit('update:isOpen', false)" />
				<h4 class="slider-header-item">{{ header }}</h4>
			</div>
			<slot name="content"></slot>
		</template>
		<template v-slot:tab>
			<IconChevronRight24 class="slider-header-item" @click="emit('update:isOpen', true)" />
			<h5 class="slider-header-item">{{ header }}</h5>
			<slot name="tab"></slot>
		</template>
	</slider>
</template>

<script setup lang="ts">
import IconChevronLeft24 from '@carbon/icons-vue/es/chevron--left/24';
import IconChevronRight24 from '@carbon/icons-vue/es/chevron--right/24';

import Slider from '../Slider.vue';

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
		default: '25%'
	},
	tabWidth: {
		type: String,
		default: '50px'
	},

	// slider-panel props
	header: {
		type: String,
		default: ''
	}
});

const emit = defineEmits(['update:isOpen']);

// const directionMap = {
// 	left: {
// 		content: 'margin-left: -100%; margin-right: 100%;',
// 		tab: 'margin-left: auto;'
// 	},
// 	right: {
// 		content: 'margin-left: 100%; margin-right: -100%;',
// 		tab: 'margin-right: auto;'
// 	}
// };
</script>

<style scoped>
.slider-header {
	display: flex;
	align-items: center;
}
.slider-header.content {
	flex-direction: row-reverse;
	justify-content: space-between;
}
.slider-header.tab {
	justify-content: center;
}
.slider-header-item {
	font-weight: bold;
	margin: 12px;
}

.slider-tab h5 {
	transform: rotate(90deg);
}
</style>
