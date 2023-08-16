<template>
	<nav :style="{ '--nb-tabs': tabs.length }">
		<header v-for="(tab, index) in tabs" :key="index">
			<div
				class="tab"
				@click="activeTabIndex !== index && emit('select-tab', index)"
				:active="activeTabIndex === index"
				:loading="loadingTabIndex === index"
			>
				<vue-feather
					v-if="typeof getAssetIcon(tab.pageType ?? null) === 'string'"
					:type="getAssetIcon(tab.pageType ?? null)"
					size="1rem"
					stroke="rgb(16, 24, 40)"
				/>
				<component
					v-else
					:is="getAssetIcon(tab.pageType ?? null)"
					class="p-button-icon-left icon"
				/>
				<span class="name">
					{{ getTabName(tab) }}
				</span>
				<Button
					icon="pi pi-times"
					class="p-button-icon-only p-button-text p-button-rounded p-button-icon-only-small"
					@click.stop="emit('close-tab', index)"
				/>
			</div>
			<div :loading="loadingTabIndex === index" @animationiteration="endAnimationIfTabIsLoaded" />
		</header>
	</nav>
</template>

<script setup lang="ts">
/**
 * A container to render a component in a tabbed view
 * @prop {Tab[]} tabs - array of tab data
 * @prop {number} activeTabIndex - tab to make active
 */
import { Tab } from '@/types/common';
import Button from 'primevue/button';
import { getAssetIcon } from '@/services/project';
import { ref, watch } from 'vue';
import useResourceStore from '@/stores/resources';
import { ProjectAssetTypes, ProjectPages } from '@/types/Project';

const props = defineProps<{
	tabs: Tab[];
	activeTabIndex: number | null;
	loadingTabIndex: number | null;
}>();

const resourceStore = useResourceStore();

const emit = defineEmits(['select-tab', 'close-tab']);
const loadingTabIndex = ref();

const getTabName = (tab: Tab) => {
	if (tab.assetName) return tab.assetName;
	if (tab.pageType === ProjectPages.OVERVIEW) return 'Overview';
	if (tab.pageType === ProjectAssetTypes.CODE) return 'New File';
	const assets = resourceStore.activeProjectAssets;

	if (assets) {
		const asset: any = assets[tab.pageType as string].find(
			(assetItem: any) => assetItem.id?.toString() === tab.assetId?.toString()
		);
		// FIXME: remove this check for title/name once the following github issue has been addressed
		// https://github.com/DARPA-ASKEM/data-service/issues/299
		return (tab.pageType === ProjectAssetTypes.DOCUMENTS ? asset?.title : asset?.name) ?? 'n/a';
	}
	return 'n/a';
};

function endAnimationIfTabIsLoaded() {
	if (props.loadingTabIndex === null) {
		loadingTabIndex.value = null;
	}
}

watch(
	() => props.loadingTabIndex,
	() => {
		if (props.loadingTabIndex !== null) {
			loadingTabIndex.value = props.loadingTabIndex;
		}
	},
	{ immediate: true }
);
</script>

<style scoped>
nav {
	display: grid;
	grid-auto-columns: min(calc(100% / var(--nb-tabs, 1)), 20%);
	grid-auto-flow: column;
	padding: 0.2rem 0.2rem 0 0.2rem;
	background-color: var(--surface-ground);
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
	height: 3rem;
}

.tab {
	align-items: center;
	animation: show-tab 0.15s ease forwards;
	background-color: transparent;
	border-color: var(--surface-ground);
	border-bottom-color: var(--surface-border-light);
	border-bottom-width: 2px;
	border-style: solid;
	border-top-left-radius: 0.5rem;
	border-top-right-radius: 0.5rem;
	border-width: 1px;
	cursor: pointer;
	display: flex;
	height: 100%;
	gap: 0.66rem;
	padding: 0.25rem 0.5rem 0.25rem 0.75rem;
	position: relative;
}

.icon {
	fill: var(--text-color-primary);
	overflow: visible;
}

i {
	overflow: visible;
}

.tab {
	border-bottom-width: 2px;
}

.tab:hover {
	border-color: var(--surface-border-light);
}

.tab[active='true'] {
	background-color: var(--surface-0);
}

.tab[active='true'][loading='false'] {
	border-bottom-color: var(--primary-color);
}
.tab[active='false'] {
	background-color: var(--surface-secondary);
}
.tab[active='false']:hover {
	background-color: var(--surface-highlight);
}

.tab:not(:hover) .p-button {
	pointer-events: none;
	display: none;
	visibility: hidden;
}

.tab + div {
	width: calc(100% - 2px);
	position: relative;
	height: 2px;
	top: -2px;
	left: 1px;
}

@keyframes tab-loading {
	0% {
		width: 0%;
		/* opacity: 1; */
	}

	50% {
		width: calc(100% - 2px);
	}

	100% {
		/* opacity: 0; */
	}
}

.tab + div[loading='true'] {
	background-color: var(--primary-color);
	animation: tab-loading 0.6s ease-out infinite forwards;
}

.p-button {
	/* Avoid the highlight to be squished */
	flex-shrink: 0;
}

span {
	display: inline-flex;
	align-items: center;
}

.name {
	display: inline-block;
	font-size: var(--font-caption);
	flex-grow: 1;
	margin-bottom: 1px;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
</style>
