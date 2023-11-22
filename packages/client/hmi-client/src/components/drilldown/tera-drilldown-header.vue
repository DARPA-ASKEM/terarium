<template>
	<header>
		<div class="title-row">
			<div class="title-container">
				<h4><slot /></h4>
				<i v-if="props.tooltip" v-tooltip="tooltip" class="pi pi-info-circle" />
			</div>
			<Button
				class="close-mask"
				icon="pi pi-times"
				text
				rounded
				aria-label="Close"
				@click="emit('close')"
			/>
		</div>
		<div class="tabs-row">
			<div>
				<TabView v-if="views.length > 1" :active-index="activeIndex" @tab-change="onTabChange">
					<TabPanel v-for="(view, index) in views" :key="index" :header="view" />
				</TabView>
			</div>
			<a target="_blank">Documentation</a>
		</div>
	</header>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import TabView, { TabViewChangeEvent } from 'primevue/tabview';
import TabPanel from 'primevue/tabpanel';

const props = defineProps<{
	activeIndex: number;
	views: string[];
	tooltip?: string;
}>();
const emit = defineEmits(['close', 'tab-change']);

const onTabChange = (event: TabViewChangeEvent) => {
	emit('tab-change', event);
};
</script>

<style scoped>
header {
	display: flex;
	flex-direction: column;
	gap: 1rem;
	background-color: var(--surface-highlight);
	padding-top: 1.25rem;
	padding-left: 1rem;
	padding-right: 1rem;
	height: var(--drilldown-header-height);
}

header > * {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.title-container {
	display: flex;
	align-items: center;
	gap: 1rem;
}

.title-container > i {
	color: var(--text-color-secondary);
}

.p-button.p-button-icon-only.p-button-rounded {
	height: 24px;
	width: 24px;
}
.p-button:deep(.p-button-icon) {
	font-size: 16px;
	color: var(--text-color-primary);
}

header .tabs-row:deep(.p-tabview .p-tabview-panels) {
	padding: 0;
}

.tabs-row > a {
	height: 3rem;
	display: flex;
	align-items: center;
	padding-right: 1.5rem;
	color: var(--primary-color);
}

:deep(.p-tabview-header:not(.p-highlight) .p-tabview-nav-link) {
	background: var(--tab-backgroundcolor-unselected);
}

:deep(.p-tabview .p-tabview-nav li .p-tabview-nav-link:focus) {
	background-color: var(--surface-section);
}

.close-mask {
	position: relative;
	overflow: visible;
}

.close-mask.p-button.p-button-icon-only:deep() {
	padding: 1rem;
}
.close-mask:before {
	content: '';
	position: absolute;
	top: -100px;
	right: -100px;
	bottom: -12px;
	left: -12px;
}
</style>
