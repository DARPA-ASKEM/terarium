<script setup lang="ts">
import { Project, ProjectAssetTypes } from '@/types/Project';
import Card from 'primevue/card';
import Button from 'primevue/button';

defineProps<{ project: Project }>();
</script>

<template>
	<Card>
		<template #header>
			<header class="project-stats">
				<div title="Contributors"><i class="pi pi-user"></i> 1</div>
				<div title="Models">
					<i class="pi pi-share-alt"></i>
					{{ project?.assets?.[ProjectAssetTypes.MODELS]?.length ?? 0 }}
				</div>
				<div title="Datasets">
					<i class="pi pi-sliders-v"></i>
					{{ project?.assets?.[ProjectAssetTypes.DATASETS]?.length ?? 0 }}
				</div>
				<div title="Papers">
					<i class="pi pi-file"></i>
					{{ project?.assets?.[ProjectAssetTypes.DOCUMENTS]?.length ?? 0 }}
				</div>
			</header>
		</template>
		<template #title>
			<div class="project-img">
				<img src="@assets/images/project-card.png" alt="Project image" />
			</div>
			<div class="project-title">{{ project.name }}</div>
		</template>
		<template #subtitle>
			<div class="project-description">{{ project.description }}</div>
		</template>
		<template #content>
			<div class="project-footer">
				<span>Last modified Dec 20, 2022</span>
				<Button icon="pi pi-ellipsis-v" class="p-button-rounded p-button-secondary" />
			</div>
		</template>
	</Card>
</template>

<style scoped>
.project-footer {
	align-items: baseline;
	display: flex;
	justify-content: space-between;
	color: var(--text-color-secondary);
}

.project-stats {
	display: flex;
	justify-content: space-between;
	padding: 0 1rem 0 1rem;
}

.project-stats div {
	padding-top: 1rem;
	color: var(--text-color-secondary);
}

.project-img {
	width: 248px;
	height: 191px;
	background-color: var(--surface-ground);
	border-radius: 1rem;
	margin-bottom: 1rem;
	transition: opacity 0.3s ease, height 0.3s ease;
}

.p-card:hover .project-img {
	opacity: 0;
	height: 0px;
}

.project-title {
	display: inline-block;
	height: 75px;
	width: 248px;
	overflow: hidden;
}

.project-description {
	opacity: 0;
	height: 0;
	width: 248px;
	transition: opacity 0.3s ease, height 0.3s ease;
}

.p-card:hover .project-description {
	opacity: 100;
	height: 191px;
}

.p-card {
	height: 423px;
	width: 280px;
}
</style>
