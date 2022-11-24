<template>
	<transition :name="transitionName">
		<div v-if="isOpen === null || isOpen" class="drilldown-panel-container">
			<div class="panel-header">
				<h5>{{ paneTitle }}</h5>
			</div>
			<div class="panel-content" id="panel-content-container">
				<slot name="content" />
			</div>
		</div>
	</transition>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue';

export interface DrilldownPanelTab {
	id: string;
	name: string;
	icon?: string;
}

const props = defineProps({
	// If isOpen isn't passed, no close button
	//  is shown.
	isOpen: {
		type: Boolean,
		default: null
	},
	tabs: {
		type: Array as PropType<DrilldownPanelTab[]>,
		default: () => []
	},
	activeTabId: {
		type: String as PropType<String | null>,
		default: null
	},
	hasTransition: {
		type: Boolean,
		default: true
	},
	hideClose: {
		type: Boolean,
		default: false
	}
});

const transitionName = computed(() => (props.hasTransition ? 'slide-fade' : ''));
const paneTitle = computed(() => {
	const activeTab = props.tabs.find((tab) => tab.id === props.activeTabId);
	return activeTab === undefined ? '[Panel Title]' : activeTab.name;
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables';

$drilldown-width: 20vw;

.drilldown-panel-container {
	height: 100vh;
	width: $drilldown-width;
	flex-shrink: 0;
	position: relative;
	display: flex;
	flex-direction: column;
	border-left: 2px solid $background-light-3;
	border-top-left-radius: 5px;
	&.slide-fade-enter-from,
	&.slide-fade-leave-to {
		width: 0;

		.panel-header,
		.panel-content,
		.tab-bar {
			opacity: 0;
		}
	}

	:deep(.tab-bar li.active) {
		background: $background-light-1;
		z-index: -1;
	}

	.panel-header {
		height: $navbar-outer-height;
		display: flex;
		align-items: center;
		padding: 0 10px;

		h5 {
			margin: 0;
			flex: 1;
			@include header-secondary;
		}
	}

	:deep(.pane-summary) {
		padding-bottom: 5px;
		font-size: $font-size-large;
		font-weight: 600;

		.icon {
			// Spacing for summaries that include arrow icons (e.g. the Evidence pane)
			margin: 0 4px;
		}
	}

	:deep(.pane-loading-message) {
		padding: 10px;

		.pane-loading-icon ~ span {
			margin-left: 4px;
		}
	}
}

.slide-fade-enter-active,
.slide-fade-leave-active {
	transition: all $layout-transition;
	.panel-content,
	.panel-header,
	.tab-bar {
		transition: opacity 0.1s ease;
	}
}

.panel-header,
.panel-body,
.panel-content {
	width: $drilldown-width;
	background-color: $background-light-1;
}

.navigation-button {
	font-size: $font-size-large;
	width: 32px;
	height: 32px;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 3px;
	color: #c1c1de;
	cursor: pointer;

	&:hover {
		color: $text-color-dark;
	}
}

.close-button {
	position: relative;
	top: auto;
	right: auto;
}

.back-button {
	font-size: 24px;
	margin-right: 8px;
}

.panel-content {
	height: 0;
	flex: 1;
	overflow-y: auto;
	padding: 0 10px;
}

.overlay-pane {
	position: absolute;
	width: 25vw;
	left: 0;
	top: 0;
	bottom: 0;
	background: $background-light-1;
	opacity: 0;
	transition: transform $layout-transition, opacity $layout-transition;
	transform: translateX(100%);
	display: flex;
	flex-direction: column;

	&.open {
		opacity: 1;
		transform: translateX(0);
	}
}
</style>
