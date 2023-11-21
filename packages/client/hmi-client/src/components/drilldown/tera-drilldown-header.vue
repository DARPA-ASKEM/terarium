<template>
	<header>
		<div class="title-row">
			<div class="title-container">
				<h5><slot /></h5>
				<i v-if="props.tooltip" v-tooltip="tooltip" class="pi pi-info-circle" />
			</div>
			<Button icon="pi pi-times" text rounded aria-label="Close" @click="emit('close')" />
		</div>
		<div class="actions-row">
			<div>
				<TabView
					v-if="props.views.length > 1"
					:active-index="props.activeIndex"
					@tab-change="onTabChange"
				>
					<TabPanel v-for="(view, index) in props.views" :key="index" :header="view" />
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
	gap: 16px;
	background-color: var(--surface-highlight);
	padding-top: 20px;
	padding-left: 16px;
	padding-right: 16px;
	height: 108px;
}
header .title-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 0.5rem;
}

header .actions-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	/* padding-left: 1rem;
    padding-right: 1rem; */
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

header .actions-row:deep(.p-tabview .p-tabview-panels) {
	padding: 0;
}

.actions-row > a {
	height: 3rem;
	display: flex;
	align-items: center;
}

:deep(.p-tabview-header:not(.p-highlight) .p-tabview-nav-link) {
	background: var(--tab-backgroundcolor-unselected);
}
</style>
