<template>
	<div class="header p-buttonset">
		<Button
			label="Wizard"
			severity="secondary"
			icon="pi pi-sign-in"
			size="small"
			:active="activeTab === FunmanTabs.wizard"
			@click="activeTab = FunmanTabs.wizard"
		/>
		<Button
			label="Notebook"
			severity="secondary"
			icon="pi pi-sign-out"
			size="small"
			:active="activeTab === FunmanTabs.notebook"
			@click="activeTab = FunmanTabs.notebook"
		/>
	</div>
	<div v-if="activeTab === FunmanTabs.wizard" class="container">
		<div class="left-side">
			<h4>Set validation parameters <i class="pi pi-info-circle" /></h4>
			<p>The calidator will use these parameters to execute the sanity checks.</p>
		</div>
		<div class="right-side">
			<div v-if="false">
				<h4>Output graph goes here</h4>
			</div>
			<div v-else>
				<!-- TODO -->
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h4>No Output</h4>
			</div>
		</div>
	</div>
	<div v-else class="container">
		<div class="left-side">
			<h4>TODO: Are we demoing notebook?</h4>
			<!-- <Suspense>
				<teraMiraNotebook />
			</Suspense> -->
		</div>
		<div class="right-side">
			<div v-if="false">
				<h4>Output graph goes here</h4>
			</div>
			<div v-else>
				<!-- TODO -->
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h4>No Output</h4>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Button from 'primevue/button';
// import { WorkflowNode } from '@/types/workflow';
// import { FunmanOperationState } from './funman-operation';

// const props = defineProps<{
// 	node: WorkflowNode<FunmanOperationState>;
// }>();

enum FunmanTabs {
	wizard,
	notebook
}
const activeTab = ref(FunmanTabs.wizard);
</script>

<style>
.container {
	display: flex;
	margin-top: 1rem;
}
.left-side {
	width: 45%;
	padding-right: 2.5%;
}
.left-side h1 {
	color: var(--text-color-primary);
	font-family: Inter;
	font-size: 1rem;
	font-style: normal;
	font-weight: 600;
	line-height: 1.5rem; /* 150% */
	letter-spacing: 0.03125rem;
}
.left-side p {
	color: var(--Text-Secondary);
	/* Body Small/Regular */
	font-family: Figtree;
	font-size: 0.875rem;
	font-style: normal;
	font-weight: 400;
	line-height: 1.3125rem; /* 150% */
	letter-spacing: 0.01563rem;
}
.right-side {
	width: 45%;
	padding-left: 2.5%;
}
</style>
