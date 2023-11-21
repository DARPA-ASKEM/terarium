<template>
	<header>
		<div class="actions">
			<div>
				<h5><slot /> <i v-tooltip="'Test tooltip.'" class="pi pi-info-circle" /></h5>
			</div>
			<Button icon="pi pi-times" text rounded aria-label="Close" @click="emit('close')" />
		</div>
		<div class="tabs">
			<TabView
				v-if="props.views.length > 1"
				:active-index="props.activeIndex"
				@tab-change="onTabChange"
			>
				<TabPanel v-for="(view, index) in props.views" :key="index" :header="view" />
			</TabView>

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
}
header .actions {
	display: flex;
	justify-content: space-between;
	align-items: center;
	gap: 0.5rem;
}

header .tabs {
	display: flex;
	align-items: center;
	justify-content: space-between;
	/* padding-left: 1rem;
    padding-right: 1rem; */
}

.p-button.p-button-icon-only.p-button-rounded {
	height: 24px;
	width: 24px;
}
.p-button:deep(.p-button-icon) {
	font-size: 16px;
	color: var(--text-color-primary);
}

header .tabs:deep(.p-tabview .p-tabview-panels) {
	padding: 0;
}
</style>
