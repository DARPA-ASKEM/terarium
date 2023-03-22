<script setup lang="ts">
/**
 * A container to render a component in a tabbed view
 * @prop {Tab[]} tabs - array of tab data
 * @prop {Object} icon - optional - an icon to display next to the name of each tab
 * @prop {number} activeTabIndex - tab to make active
 *
 * @typedef {Object} Tab
 * @property {string} tabName - name to display in tab header
 *
 */
import { Tab } from '@/types/common';
import Button from 'primevue/button';
import Chip from 'primevue/chip';

const props = defineProps<{
	tabs: Tab[];
	icon?: Object;
	activeTabIndex: number;
}>();

const emit = defineEmits(['select-tab', 'close-tab']);

function calcTabWidthPercentage() {
	return props.tabs.length <= 5 ? 20 : 100 / props.tabs.length;
}
</script>

<template>
	<!-- This div is so that child tabs can be positioned absolutely relative to the div -->
	<nav>
		<template v-for="(tab, index) in tabs" :key="index">
			<header :style="`width: ${calcTabWidthPercentage()}%`">
				<div class="tab" @click="emit('select-tab', tab)" :active="activeTabIndex === index">
					<Chip v-if="tab.assetType !== 'overview'" :label="tab.assetType" />
					<span class="name">
						{{ tab.assetName }}
					</span>
					<span>
						<Button
							icon="pi pi-times"
							class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
							@click.stop="emit('close-tab', index)"
						/>
					</span>
				</div>
			</header>
		</template>
	</nav>
</template>

<style scoped>
nav {
	margin: 0.25rem;
	margin-bottom: 0;
	display: flex;
	overflow: auto;
	position: relative;
	width: calc(100% - 0.5rem);
}

@keyframes show-tab {
	0% {
		width: 0%;
	}

	100% {
		width: 100%;
	}
}

header {
	position: relative;
	z-index: 1;
}

.tab {
	display: inline-flex;
	cursor: pointer;
	padding: 0.25rem 0.5rem 0.25rem 0.5rem;
	border-top-left-radius: 0.5rem;
	border-top-right-radius: 0.5rem;
	background-color: transparent;
	width: 100%;
	position: relative;
	justify-content: space-between;
	animation: show-tab 0.15s ease forwards;
	align-items: center;
}

.tab[active='true'] {
	background-color: white;
	border-bottom: 2px solid var(--primary-color);
}

.tab:hover[active='false'] {
	background-color: var(--surface-secondary);
}

span {
	display: inline-flex;
	align-items: center;
}

.name {
	overflow: hidden;
	white-space: nowrap;
	text-overflow: ellipsis;
	font-size: var(--font-caption);
	display: inline-block;
	margin-bottom: 1px;
}

.icon {
	margin-right: 0.5rem;
}

.p-chip {
	padding: 0 0.5rem;
	margin-right: 0.25rem;
	border-radius: 0.5rem;
	text-transform: uppercase;
}
</style>
