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
				></i>
				<h4 class="slider-header-item">{{ header }}</h4>
			</div>
			<slot name="content"></slot>
		</template>
		<template v-slot:tab>
			<i
				:class="`slider-header-item pi ${directionMap[direction].iconClosed}`"
				@click="emit('update:isOpen', true)"
			></i>
			<h5 class="slider-header-item">{{ header }}</h5>
			<slot name="tab"></slot>
		</template>
	</slider>
</template>

<script setup lang="ts">
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
	font-size: 1.5rem;
	cursor: pointer;
}

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
