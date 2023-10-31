<template>
	<div class="header p-buttonset">
		<Button
			label="Wizard"
			severity="secondary"
			icon="pi pi-sign-in"
			size="small"
			:active="activeTab === Tabs.wizard"
			@click="activeTab = Tabs.wizard"
		/>
		<Button
			label="Notebook"
			severity="secondary"
			icon="pi pi-sign-out"
			size="small"
			:active="activeTab === Tabs.notebook"
			@click="activeTab = Tabs.notebook"
		/>
	</div>
	<div v-if="activeTab === Tabs.wizard" class="container">
		<div class="left-side">
			<h4>Stratify Model <i class="pi pi-info-circle" /></h4>
			<p>The model will be stratified with the following settings.</p>
		</div>
		<div class="right-side">
			<tera-model-diagram
				v-if="model"
				ref="teraModelDiagramRef"
				:model="model"
				:is-editable="false"
			/>
			<div v-else>
				<!-- TODO -->
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h4>No Model Provided</h4>
			</div>
		</div>
	</div>
	<div v-else class="container">
		<div class="left-side">
			<Suspense>
				<teraMiraNotebook />
			</Suspense>
		</div>
		<div class="right-side">
			<tera-model-diagram
				v-if="model"
				ref="teraModelDiagramRef"
				:model="model"
				:is-editable="false"
			/>
			<div v-else>
				<!-- TODO -->
				<img src="@assets/svg/plants.svg" alt="" draggable="false" />
				<h4>No Model Provided</h4>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import Button from 'primevue/button';

enum Tabs {
	wizard,
	notebook
}
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
