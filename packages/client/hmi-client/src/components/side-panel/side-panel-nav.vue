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
				<component :is="toRaw(tab.icon)" />
				<span v-if="tab.badgeCount !== undefined && tab.badgeCount > 0" class="counter-badge">
					{{ tab.badgeCount }}
				</span>
			</button>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { computed, PropType, toRaw } from 'vue';
import { SidePanelTab } from '@/types/common';

const props = defineProps({
	tabs: {
		type: Array as PropType<SidePanelTab[]>,
		default: () => []
	},
	currentTabName: {
		type: String,
		default: () => ''
	}
});

const emit = defineEmits(['set-active']);

const allTabsAreClosed = computed(
	() => props.tabs.find((tab) => tab.name === props.currentTabName) === undefined
);

const toggleActive = (tabName: string) => {
	// If the tab is currently selected, pass '' to signify it should be
	//  unselected. Otherwise, pass the tab's name to select it
	emit('set-active', tabName === props.currentTabName ? '' : tabName);
};

const getImgUrl = (imgSrc: string) => {
	// @ts-ignore: Unreachable code error
	const assetFolder = require.context('@/assets/');
	return assetFolder(`./${imgSrc}`);
};
</script>

<style scoped>
.side-panel-nav-container {
	margin: 0;
	padding: 0;
}

/* If no tab is open, all tabs should take the full square width */
.side-panel-nav-container.all-tabs-closed li:not(:hover) {
	transform: translateX(0);
}

li {
	position: relative;
	display: block;
	border-top-right-radius: 3px;
	border-bottom-right-radius: 3px;
	color: rgba(0, 0, 0, 0.61);
	margin-bottom: 5px;
	background: var(--background-light-1-faded);
	transform: translateX(-25%);
	transition: transform 0.1s ease;
}

/* Add a white rectangle to the left of each tab to show during the hover animation */
li::before {
	content: '';
	display: block;
	position: absolute;
	width: 5%;
	height: 100%;
	top: 0;
	z-index: -1;
	/* // Overlap the tab slightly to cover tiny gaps during animation */
	right: calc(100% - 1px);
	background: var(--background-light-1);
}

li button {
	width: var(--navbar-outer-height);
	height: var(--navbar-outer-height);
	background-color: transparent;
	border-radius: 0;
	border: none;
	color: black;
}

li button .counter-badge {
	display: inline-block;
	padding: 3px 7px;
	font-size: var(--font-size-small);
	font-weight: bold;
	color: white;
	line-height: 1;
	vertical-align: middle;
	background-color: #545353;
	border-radius: 10px;
	position: absolute;
	left: calc(var(--navbar-outer-height) / 2);
	bottom: calc(var(--navbar-outer-height) / 2);
	top: auto;
}

li img {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	width: 40%;
	height: 40%;
}

li:not(.active):hover {
	background-color: var(--background-light-1);
	color: #000;
}

li.active {
	transform: translateX(0);
	background-color: var(--background-light-1);
	color: var(--un-color-accent-dark);
}

li:hover {
	transform: translateX(5px);
}
</style>
