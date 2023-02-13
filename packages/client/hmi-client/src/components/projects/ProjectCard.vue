<script setup lang="ts">
import { Project, ProjectAssetTypes } from '@/types/Project';
import Card from 'primevue/card';
import Button from 'primevue/button';
import { formatDdMmmYyyy } from '@/utils/date';

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
		<template #content>
			<div class="project-description">{{ project.description }}</div>
		</template>
		<template #footer>
			<div class="project-footer">
				<span>Last updated {{ formatDdMmmYyyy(project.timestamp) }}</span>
				<Button icon="pi pi-ellipsis-v" class="p-button-rounded p-button-secondary" />
			</div>
		</template>
	</Card>
</template>

<style scoped>
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
	height: 190px;
	background-color: var(--surface-ground);
	border-radius: 1rem;
	margin-bottom: 1rem;
	transition: opacity 0.3s ease, height 0.3s ease;
}

.project-img {
	position: relative;
}

.p-card:hover .project-img {
	opacity: 0;
	height: 17px;
}

.project-title {
	display: inline-block;
	height: 77px;
	width: 248px;
	overflow: hidden;
}

.project-description {
	display: inline-block;
	overflow: hidden;
	opacity: 0;
	height: 17px;
	width: 248px;
	transition: opacity 0.3s ease, height 0.3s ease;
	color: var(--text-color-secondary);
}

.p-card:hover .project-description {
	opacity: 100;
	height: 190px;
}

.project-footer {
	align-items: baseline;
	display: flex;
	justify-content: space-between;
	color: var(--text-color-secondary);
}

.p-card {
	width: 280px;
}
</style>
