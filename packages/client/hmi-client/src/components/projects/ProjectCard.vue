<script setup lang="ts">
import { IProject, ProjectAssetTypes } from '@/types/Project';
import Card from 'primevue/card';
import Button from 'primevue/button';
import { formatDdMmmYyyy } from '@/utils/date';
import Skeleton from 'primevue/skeleton';

defineProps<{ project?: IProject }>();
</script>

<template>
	<Card v-if="project">
		<template #content>
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
			<div class="project-img">
				<img src="@assets/images/project-card.png" alt="Project image" />
			</div>
			<div class="project-title">{{ project.name }}</div>
			<div class="project-description">{{ project.description }}</div>
			<div class="project-footer">
				<span>Last updated {{ formatDdMmmYyyy(project.timestamp) }}</span>
				<Button icon="pi pi-ellipsis-v" class="p-button-rounded p-button-secondary" />
			</div>
		</template>
	</Card>
	<Card v-else>
		<template #content>
			<header class="project-stats skeleton">
				<Skeleton height="100%" />
				<Skeleton height="100%" />
				<Skeleton height="100%" />
				<Skeleton height="100%" />
			</header>
			<div class="project-img skeleton">
				<Skeleton height="100%" />
			</div>
			<div class="project-description skeleton">
				<Skeleton />
				<Skeleton />
				<Skeleton width="60%" />
			</div>
			<div class="project-footer skeleton">
				<Skeleton />
			</div>
		</template>
	</Card>
</template>

<style scoped>
.p-card {
	width: 17rem;
	height: 20rem;
}

.project-stats {
	display: flex;
	justify-content: space-between;
	font-size: var(--font-caption);
	vertical-align: bottom;
}

.pi {
	vertical-align: bottom;
}

.project-stats.skeleton {
	gap: 1rem;
	height: 17px;
}

.project-stats div {
	color: var(--text-color-secondary);
}

.project-img {
	height: 8.75rem;
	background-color: var(--surface-ground);
	border-radius: 1rem;
	transition: opacity 0.3s ease, height 0.3s ease;
	position: relative;
	margin: 0.5rem 0 0.5rem 0;
}

.project-img img {
	height: 100%;
	width: 100%;
}

.project-img.skeleton {
	background-color: transparent;
}

.p-card:hover .project-img:not(.skeleton) {
	opacity: 0;
	height: 0;
}

.project-title {
	display: inline-block;
	height: 3.75rem;
	overflow: hidden;
	font-weight: var(--font-weight-semibold);
}

.project-title.skeleton {
	height: 100%;
}

.project-description:not(.skeleton) {
	overflow: hidden;
	opacity: 0;
	height: 0;
	transition: opacity 0.3s ease, height 0.3s ease;
	color: var(--text-color-secondary);
}

.p-card:hover .project-description:not(.skeleton) {
	opacity: 100;
	height: 8.75rem;
}

.project-description.skeleton {
	display: flex;
	flex-direction: column;
	row-gap: 0.5rem;
}

.project-footer {
	height: 3rem;
	align-items: center;
	display: flex;
	justify-content: space-between;
	color: var(--text-color-secondary);
	padding-top: 0.5rem;
	font-size: var(--font-caption);
}

.project-footer.skeleton {
	align-items: flex-end;
}

.p-button.p-button-icon-only.p-button-rounded {
	height: 2rem;
	width: 2rem;
}
</style>
