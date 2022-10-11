<template>
	<ul
		class="side-panel-nav-container"
		:class="{ 'all-tabs-closed': allTabsAreClosed }"
		role="tablist"
	>
		<li v-for="(tab, idx) in tabs" :key="idx" :class="{ active: tab.name === currentTabName }">
			<button
				type="button"
				role="tab"
				:class="{ 'is-greyscale': tab.isGreyscale }"
				@click="toggleActive(tab.name)"
			>
				<img
					v-if="tab.imgSrc !== undefined && tab.imgSrc !== null"
					:src="getImgUrl(tab.imgSrc)"
					alt=""
				/>
				<i v-else :class="tab.icon" />
				<span v-if="tab.badgeCount !== undefined && tab.badgeCount > 0" class="counter-badge">
					{{ tab.badgeCount }}
				</span>
			</button>
		</li>
	</ul>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue';
import { SidePanelTab } from '@/types/common';

export default defineComponent({
	name: 'SidePanelNav',
	props: {
		tabs: {
			type: Array as PropType<SidePanelTab[]>,
			default: () => []
		},
		currentTabName: {
			type: String,
			default: () => ''
		}
	},
	emits: ['set-active'],
	computed: {
		allTabsAreClosed(): boolean {
			return this.tabs.find((tab) => tab.name === this.currentTabName) === undefined;
		}
	},
	methods: {
		toggleActive(tabName: string) {
			// If the tab is currently selected, pass '' to signify it should be
			//  unselected. Otherwise, pass the tab's name to select it
			this.$emit('set-active', tabName === this.currentTabName ? '' : tabName);
		},
		getImgUrl(imgSrc: string) {
			// @ts-ignore: Unreachable code error
			const assetFolder = require.context('@/assets/');
			return assetFolder(`./${imgSrc}`);
		}
	}
});
</script>

<style lang="scss" scoped>
@import '../../styles/variables';

.side-panel-nav-container {
	margin: 0;
	padding: 0;

	// If no tab is open, all tabs should take
	//  the full square width
	&.all-tabs-closed {
		li:not(:hover) {
			transform: translateX(0);
		}
	}
}

li {
	position: relative;
	display: block;
	border-top-right-radius: 3px;
	border-bottom-right-radius: 3px;
	color: rgba(0, 0, 0, 0.61);
	margin-bottom: 5px;
	background: $background-light-1-faded;
	transform: translateX(-25%);
	transition: transform 0.1s ease;

	// Add a white rectangle to the left of each
	//  tab to show during the hover animation
	&::before {
		content: '';
		display: block;
		position: absolute;
		width: 50%;
		height: 100%;
		top: 0;
		z-index: -1;
		// Overlap the tab slightly to cover tiny
		// gaps during animation
		right: calc(100% - 1px);
		background: $background-light-1;
	}

	button {
		width: $navbar-outer-height;
		height: $navbar-outer-height;
		background-color: transparent;
		border-radius: 0;
		border: none;
		color: black;

		&.is-greyscale {
			img,
			i {
				filter: grayscale(100%);
			}
		}

		.counter-badge {
			display: inline-block;
			padding: 3px 7px;
			font-size: $font-size-small;
			font-weight: bold;
			color: white;
			line-height: 1;
			vertical-align: middle;
			background-color: #545353;
			border-radius: 10px;
			position: absolute;
			left: calc($navbar-outer-height / 2);
			bottom: calc($navbar-outer-height / 2);
			top: auto;
		}
	}

	img {
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
		width: 40%;
		height: 40%;
	}

	&:not(.active):hover {
		background-color: $background-light-1;
		color: #000;
	}

	&.active {
		transform: translateX(0);
		background-color: $background-light-1;
		color: $selected-dark;
	}

	&:hover {
		transform: translateX(5px);
	}
}
</style>
