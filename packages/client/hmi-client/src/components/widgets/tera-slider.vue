<template>
	<aside
		:class="`slider ${isOpen ? 'open' : 'closed'} ${direction}`"
		:style="{ width: isOpen ? contentWidth : tabWidth }"
	>
		<div class="slider-content-container" :style="{ width: contentWidth }">
			<section class="slider-content" :style="sidePanelContentStyle">
				<slot name="content" />
			</section>
			<footer>
				<slot name="footerButtons" />
			</footer>
		</div>
		<section class="slider-tab" :style="sidePanelTabStyle">
			<slot name="tab"></slot>
		</section>
	</aside>
</template>

<script setup lang="ts">
import { computed, getCurrentInstance } from 'vue';

const props = defineProps({
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
	}
});

const thisSlider = getCurrentInstance();

const directionMap = {
	left: {
		content: () => 'margin-left: -100%; margin-right: 100%;',
		tab: () => 'margin-left: auto;'
	},
	right: {
		content: () => `margin-left: ${props.tabWidth};`,
		tab: () => 'margin-right: auto;'
	}
};

const sidePanelContentStyle = computed(() => {
	let style: string = props.isOpen ? '' : directionMap[props.direction].content();
	style += thisSlider?.slots.footerButtons ? 'height: calc(100% - 5rem);' : '';
	return style;
});

const sidePanelTabStyle = computed(
	() => `width: ${props.tabWidth}; ${directionMap[props.direction].tab()}`
);
</script>

<style scoped>
.slider {
	z-index: 1;
}

.slider,
.slider-content,
.slider-tab {
	transition: all 0.2s ease-out;
}

.slider.left.closed .slider-tab,
.slider.left.open .slider-content {
	border-right: 1px solid var(--surface-border-light);
}

.slider.right.closed .slider-tab,
.slider.right.open .slider-content {
	border-left: 1px solid var(--surface-border-light);
}

.slider-content-container {
	position: absolute;
	height: 100%;
}

.slider-tab {
	position: relative;
	height: 100%;
	background-color: var(--surface-section);
	z-index: 1;
}

.slider.open .slider-tab,
.slider.closed .slider-content {
	visibility: hidden;
	opacity: 0;
}

.slider-content {
	background-color: var(--surface-section);
	position: relative;
	width: 100%;
	height: 100%;
	overflow-y: auto;
}

footer:empty {
	display: none;
}

footer {
	position: relative;
	border-top: 1px solid var(--surface-border-light);
	background-color: var(--surface-section);
	height: 5rem;
	width: 100%;
	display: flex;
	align-items: center;
}
</style>
