<template>
	<tera-slider :content-width="contentWidth" :tab-width="tabWidth" :direction="direction" :is-open="isOpen">
		<template v-slot:content>
			<aside class="panel-container" @scroll="onScroll">
				<header :class="{ shadow: isScrolled }">
					<i
						:class="`slider-header-item pi ${directionMap[direction].iconOpen}`"
						@click="emit('update:isOpen', false)"
					/>
					<h4>{{ header }}</h4>
				</header>
				<slot name="content"></slot>
			</aside>
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
		<template v-if="$slots.footerButtons" v-slot:footerButtons>
			<slot name="footerButtons"></slot>
		</template>
	</tera-slider>
</template>

<script setup lang="ts">
import { ref } from 'vue';
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

/* This is for adding a subtle shadow to the header when the panel is scrolled */
const isScrolled = ref(false);
const onScroll = (event: Event) => {
	// Check if the panel is scrolled beyond a certain point
	isScrolled.value = (event.target as HTMLElement).scrollTop > 0;
};
</script>

<style scoped>
.panel-container {
	height: 100%;
	overflow-y: auto;
}
i {
	font-size: 1.25rem;
	cursor: pointer;
}

header {
	display: flex;
	align-items: start;
	flex-direction: row-reverse;
	justify-content: space-between;
	padding: 1rem;
	z-index: 3;
	position: sticky;
	top: 0;
	background-color: rgba(255, 255, 255, 0.8);
	backdrop-filter: blur(3px);
	&.shadow {
		box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.1);
	}
}

section {
	display: flex;
	flex-direction: column;
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
	transform: rotate(0deg);
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
