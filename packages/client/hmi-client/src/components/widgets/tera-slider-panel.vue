<template>
	<tera-slider :content-width="contentWidth" :tab-width="tabWidth" :direction="direction" :is-open="isOpen">
		<template v-slot:content>
			<aside @scroll="onScroll">
				<header :class="{ shadow: isScrolled }">
					<Button
						:icon="`pi ${directionMap[direction].iconOpen}`"
						@click="emit('update:isOpen', false)"
						text
						rounded
						size="large"
					/>
					<h4>{{ header }}</h4>
				</header>
				<slot name="content" />
			</aside>
			<slot name="overlay" />
		</template>
		<template v-slot:tab>
			<header :class="`tab ${direction}`">
				<Button
					:icon="`pi ${directionMap[direction].iconClosed}`"
					@click="emit('update:isOpen', true)"
					text
					rounded
					size="large"
				/>
				<h5>{{ header }}</h5>
				<Badge v-if="indicatorValue" :value="indicatorValue" />
			</header>
			<slot name="tab" />
		</template>
		<template v-if="$slots.footerButtons" v-slot:footerButtons>
			<slot name="footerButtons" />
		</template>
	</tera-slider>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Badge from 'primevue/badge';
import Button from 'primevue/button';
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
header {
	position: sticky;
	top: 0;
	z-index: 3;
	display: flex;
	align-items: center;
	flex-direction: row-reverse;
	justify-content: space-between;
	padding: var(--gap-2);
	padding-left: var(--gap);
	gap: var(--gap);
	background-color: rgba(255, 255, 255, 0.8);
	backdrop-filter: blur(3px);
	&.shadow {
		box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 0.1);
	}
}

aside {
	height: 100%;
	overflow-y: auto;
}

.p-button.p-button-icon-only.p-button-rounded {
	height: 2.5rem;
}

.tab {
	flex-direction: column;
	padding: var(--gap-2);
}

h5 {
	writing-mode: vertical-lr;
}

.p-badge {
	background-color: var(--surface-200);
	color: var(--text-color-primary);
	font-size: 1rem;
	min-width: 2rem;
	height: 2rem;
	line-height: 2rem;
	font-weight: var(--font-weight);
}
</style>
