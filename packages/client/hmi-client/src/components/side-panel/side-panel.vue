<template>
	<div class="side-panel-container" :class="{ large: isLarge }">
		<div class="tab-column">
			<side-panel-nav
				class="side-panel-nav"
				:tabs="tabs"
				:current-tab-name="currentTabName"
				@set-active="(tabName) => emit('set-active', tabName)"
			/>
			<slot name="below-tabs" />
		</div>
		<transition name="slide-fade">
			<div v-if="isPanelOpen" class="side-panel-body">
				<div class="side-panel-header">
					<h5>{{ currentTabName }}</h5>
				</div>
				<div class="side-panel-content" :class="{ 'has-padding': addPadding }">
					<slot />
				</div>
			</div>
		</transition>
	</div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue';
import SidePanelNav from '@/components/side-panel/side-panel-nav.vue';
import { SidePanelTab } from '@/types/common';

const props = defineProps({
	/**
	 * `tabs` prop structure
	 * {
	 *  name: string,
	 *  icon: string (optional) - only include one of [icon, imgSrc], // USE IBM ICON INSTEAD (WON'T BE A STRING)
	 *  imgSrc: string (optional) - relative to assets folder,
	 *  isGreyscale: boolean (optional),
	 *  badgeCount: number (optional)
	 * }
	 * e.g. {
	 *  name: 'Cube Information',
	 *  icon: useIcon ? 'svg-icon' : null, // USE IBM ICON INSTEAD
	 *  imgSrc: useIcon ? null : 'imageOfCube.png',
	 *  isGreyscale: !useIcon,
	 *  badgeCount: 12
	 * }
	 */
	tabs: {
		type: Array as PropType<SidePanelTab[]>,
		default: () => []
	},
	currentTabName: {
		type: String,
		default: () => ''
	},
	addPadding: {
		type: Boolean,
		default: false
	},
	isLarge: {
		type: Boolean,
		default: false
	}
});

const emit = defineEmits(['set-active']);

const isPanelOpen = computed(() => {
	// eslint-disable-next-line no-plusplus
	for (let i = 0; i < props.tabs.length; i++) {
		if (props.tabs[i].name === props.currentTabName) return true;
	}
	return false;
});
</script>

<style scoped>
.side-panel-container {
	flex-grow: 0;
	flex-shrink: 0;
	min-width: var(--navbar-outer-height);
	position: relative;
	margin-right: 10px;
	filter: drop-shadow(0px 1px 2px rgba(0, 0, 0, 0.12));
}

.slide-fade-enter-from,
.slide-fade-leave-to {
	opacity: 0;
}

.slide-fade-enter-active,
.slide-fade-leave-active {
	transition: opacity 0.1s ease;
}

.side-panel-container.large .side-panel-header,
.side-panel-container.large .side-panel-body,
.side-panel-container.large .side-panel-content {
	width: 350px;
}

.side-panel-container .side-panel-header,
.side-panel-container .side-panel-body,
.side-panel-container .side-panel-content {
	width: 250px;
}

.tab-column {
	position: absolute;
	top: 0;
	right: 0;
	width: var(--navbar-outer-height);
	display: flex;
	flex-direction: column;
}

.side-panel-nav {
	margin-bottom: 10px;
}

.side-panel-header {
	height: var(--navbar-outer-height);
	display: flex;
	align-items: center;
}

.side-panel-header h5 {
	margin: 0;
	margin-left: 8px;
	flex: 1;
	font-size: var(--font-size-medium);
	text-transform: uppercase;
	letter-spacing: 0.066rem;
	color: var(--label-color);
	font-weight: 600;
}

.side-panel-body {
	position: relative;
	/* width of the side-panel-nav */
	margin-right: var(--navbar-outer-height);
	background-color: #ffffff;
	display: flex;
	flex-direction: column;
	height: 100%;
}

.side-panel-content {
	position: relative;
	min-height: 0;
	flex: 1;
	overflow-y: auto;
}

.side-panel-content.has-padding {
	padding: 0 10px;
}
</style>
