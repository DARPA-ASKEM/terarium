<template>
	<div class="side-panel-container" :class="{ large: isLarge }">
		<div class="tab-column">
			<side-panel-nav
				class="side-panel-nav"
				:tabs="tabs"
				:current-tab-name="currentTabName"
				@set-active="(tabName) => $emit('set-active', tabName)"
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

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import SidePanelNav from '@/components/side-panel/side-panel-nav.vue';
import { SidePanelTab } from '@/types/common';

export default defineComponent({
	name: 'SidePanel',
	components: {
		SidePanelNav
	},
	props: {
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
		 *  icon: useIcon ? 'fa fa-fw fa-cube' : null, // USE IBM ICON INSTEAD
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
	},
	emits: ['set-active'],
	computed: {
		isPanelOpen() {
			// eslint-disable-next-line no-plusplus
			for (let i = 0; i < this.tabs.length; i++) {
				if (this.tabs[i].name === this.currentTabName) return true;
			}
			return false;
		}
	}
});
</script>

<style lang="scss" scoped>
@import '@/styles/variables';
$small-width: 250px;
$large-width: 380px;

.side-panel-container {
	flex-grow: 0;
	flex-shrink: 0;
	min-width: $navbar-outer-height;
	position: relative;
	margin-right: 10px;
	filter: drop-shadow(0px 1px 2px rgba(0, 0, 0, 0.12));

	.side-panel-header,
	.side-panel-body,
	.side-panel-content {
		width: $small-width;
	}

	&.large {
		.side-panel-header,
		.side-panel-body,
		.side-panel-content {
			width: $large-width;
		}
	}

	.side-panel-body.slide-fade-enter-from,
	.side-panel-body.slide-fade-leave-to {
		width: 0;

		.side-panel-content,
		.side-panel-header {
			opacity: 0;
		}
	}
}

.tab-column {
	position: absolute;
	top: 0;
	right: 0;
	width: $navbar-outer-height;
	display: flex;
	flex-direction: column;
}

.side-panel-nav {
	margin-bottom: 10px;
}

.side-panel-header {
	height: $navbar-outer-height;
	display: flex;
	align-items: center;

	h5 {
		margin: 0;
		margin-left: 8px;
		flex: 1;
		@include header-secondary;
	}
}

.side-panel-body {
	position: relative;
	margin-right: $navbar-outer-height; // width of the side-panel-nav
	background-color: #ffffff;
	display: flex;
	flex-direction: column;
	height: 100%;
}

.slide-fade-enter-active,
.slide-fade-leave-active {
	transition: all $layout-transition;

	.side-panel-content,
	.side-panel-header {
		transition: opacity 0.1s ease;
	}
}

.side-panel-content {
	position: relative;
	min-height: 0;
	flex: 1;
	overflow-y: auto;

	&.has-padding {
		padding: 0 10px;
	}
}
</style>
