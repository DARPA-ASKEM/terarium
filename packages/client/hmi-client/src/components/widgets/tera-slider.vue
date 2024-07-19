<template>
	<aside
		:class="`slider ${isOpen ? 'open' : 'closed'} ${direction}`"
		:style="{ width: isOpen ? contentWidth : tabWidth }"
	>
		<div class="slider-content-container" :style="{ width: isOpen ? contentWidth : 0 }">
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
		tab: () => 'margin-left: auto;'
	},
	right: {
		tab: () => 'margin-right: auto;'
	}
};

const sidePanelContentStyle = computed(() => (thisSlider?.slots.footerButtons ? 'height: calc(100% - 5rem);' : ''));

const sidePanelTabStyle = computed(() => `width: ${props.tabWidth}; ${directionMap[props.direction].tab()}`);
</script>

<style scoped>
aside {
	position: relative;
}
.slider,
.slider-content,
.slider-tab,
footer {
	transition: all 0.2s ease-out;
}

.slider {
	outline: 1px solid var(--surface-border-light);
	z-index: 0;
}

.slider-content-container {
	position: absolute;
	height: 100%;
	transition: width 0.2s ease-out;
}

.slider-tab {
	position: relative;
	height: 100%;
	background-color: var(--surface-section);
	z-index: 1;
}

.slider.open .slider-tab,
.slider.closed .slider-content,
.slider.closed footer {
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
	overflow: hidden;
	border-top: 1px solid var(--surface-border-light);
	box-shadow: 0px -4px 8px -7px #b8b8b8;
	background-color: var(--surface-section);
	height: 5rem;
	width: 100%;
	display: flex;
	align-items: center;
	justify-content: space-evenly;
}
</style>
