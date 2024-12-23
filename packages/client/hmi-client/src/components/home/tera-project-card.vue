<template>
	<Card v-if="project">
		<template #content>
			<header>
				<div
					class="flex align-items-center gap-1"
					v-tooltip.top="`${stats?.contributors} contributor${stats?.contributors === 1 ? '' : 's'}`"
				>
					<i class="pi pi-user" /> {{ stats?.contributors }}
				</div>
				<div
					class="flex align-items-center gap-1"
					v-tooltip.top="`${stats?.papers} paper${stats?.papers === 1 ? '' : 's'}`"
				>
					<i class="pi pi-file" /> {{ stats?.papers }}
				</div>
				<div
					class="flex align-items-center gap-1"
					v-tooltip.top="`${stats?.datasets} dataset${stats?.datasets === 1 ? '' : 's'}`"
				>
					<dataset-icon fill="var(--text-color-secondary)" /> {{ stats?.datasets }}
				</div>
				<div
					class="flex align-items-center gap-1"
					v-tooltip.top="`${stats?.models} model${stats?.models === 1 ? '' : 's'}`"
				>
					<i class="pi pi-share-alt" /> {{ stats?.models }}
				</div>
				<div
					class="flex align-items-center gap-1"
					v-tooltip.top="`${stats?.workflows} workflow${stats?.workflows === 1 ? '' : 's'}`"
				>
					<vue-feather
						class="p-button-icon-left"
						type="git-merge"
						size="1.25rem"
						stroke="var(--text-color-secondary)"
					/>
					{{ stats?.workflows }}
				</div>
			</header>
			<div class="img">
				<img :src="thumbnail" alt="Artistic representation of the Project statistics" />
			</div>
			<section>
				<div class="title" ref="titleRef">
					<template v-if="isCopying">Copy of</template>
					{{ project.name }}
				</div>
				<ProgressBar v-if="isCopying" mode="indeterminate" />
				<section class="details">
					<div>
						<div>{{ project?.userName ?? '——' }}</div>
					</div>
					<div class="description">
						{{ project.description }}
					</div>
				</section>
			</section>
		</template>
		<template #footer>
			<template v-if="!isCopying">
				<span>Last updated {{ formatDdMmmYyyy(project.updatedOn) }}</span>
				<tera-project-menu
					:project="project"
					@copied-project="(copiedProject) => emit('copied-project', copiedProject)"
				/>
			</template>
			<template v-else-if="isCopying">Copying</template>
		</template>
	</Card>
	<Card v-else>
		<template #content>
			<header class="skeleton">
				<Skeleton height="100%" />
				<Skeleton height="100%" />
				<Skeleton height="100%" />
				<Skeleton height="100%" />
			</header>
			<div class="img skeleton">
				<Skeleton height="100%" />
			</div>
			<div class="description skeleton">
				<Skeleton />
				<Skeleton />
				<Skeleton width="60%" />
			</div>
		</template>
		<template #footer>
			<Skeleton />
		</template>
	</Card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import Card from 'primevue/card';
import Skeleton from 'primevue/skeleton';
import { formatDdMmmYyyy } from '@/utils/date';
import DatasetIcon from '@/assets/svg/icons/dataset.svg?component';
import { Project } from '@/types/Types';
import DefaultThumbnail from '@/assets/images/project-thumbnails/default.png';
import getImage from '@/assets/utils';
import ProgressBar from 'primevue/progressbar';
import TeraProjectMenu from './tera-project-menu.vue';

const props = defineProps<{
	project?: Project;
	isCopying?: boolean;
}>();
const emit = defineEmits(['copied-project']);

const titleRef = ref();
const descriptionLines = computed(() => {
	const titleHeight = titleRef.value?.clientHeight;
	if (titleHeight === 17) return 9;
	if (titleHeight === 34) return 8;
	return 7;
});

const stats = computed(() => {
	const metadata = props.project?.metadata;
	if (!props.project || !metadata) return null;

	return {
		contributors: parseInt(metadata['contributor-count'] ?? '1', 10),
		papers: parseInt(metadata['document-count'] ?? '0', 10),
		datasets: parseInt(metadata['datasets-count'] ?? '0', 10),
		models: parseInt(metadata['models-count'] ?? '0', 10),
		workflows: parseInt(metadata['workflows-count'] ?? '0', 10)
	};
});

const thumbnail = computed(
	() => getImage(`project-thumbnails/${props.project?.thumbnail ?? 'default'}.png`) ?? DefaultThumbnail
);
</script>

<style scoped>
.p-card {
	height: 20rem;
}

.p-card:deep(.p-card-body) {
	height: 100%;
	overflow: hidden;
	justify-content: space-between;
	display: flex;
	flex-direction: column;
	position: relative;
}

header {
	color: var(--text-color-secondary);
	display: flex;
	justify-content: space-between;
	font-size: var(--font-caption);
	vertical-align: bottom;
}

header span {
	display: flex;
	gap: 0.1rem;
	align-items: center;
}

header.skeleton {
	gap: 1rem;
	height: 17px;
}

section {
	display: flex;
	gap: 0.5rem;
	flex-direction: column;
}

.pi {
	vertical-align: bottom;
}

.img {
	height: 8.75rem;
	background-color: var(--surface-ground);
	border-radius: var(--border-radius-big);
	transition:
		opacity 0.3s ease,
		height 0.3s ease;
	position: relative;
	margin: 0.5rem 0 0.5rem 0;
}

.img img {
	height: 100%;
	width: 100%;
	border-radius: var(--border-radius-big);
}

.img.skeleton {
	background-color: transparent;
}

.p-card:hover .img:not(.skeleton) {
	opacity: 0;
	height: 0;
}

.title,
.description {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	text-overflow: ellipsis;
	overflow: hidden;
}

.title {
	font-weight: var(--font-weight-semibold);
	-webkit-line-clamp: 3;
}

.description {
	-webkit-line-clamp: v-bind('descriptionLines');
}

.p-card-footer.skeleton,
.title.skeleton {
	height: 100%;
}

.details:not(.skeleton) {
	overflow: hidden;
	& .description {
		opacity: 0;
		height: 0;
		transition:
			opacity 0.5s ease,
			height 0.3s ease;
		color: var(--text-color-secondary);
		font-size: var(--font-caption);
	}
}

.p-card:hover .details:not(.skeleton) {
	& .description {
		opacity: 100;
		height: auto;
	}
}

.author {
	color: var(--text-color-primary);
	font-weight: var(--font-weight-semibold);
}

.description.skeleton {
	display: flex;
	flex-direction: column;
	row-gap: 0.5rem;
}

.p-card:deep(.p-card-footer) {
	align-items: center;
	padding: 0 0 0.5rem 0;
	height: 3rem;
	background-color: rgba(255, 255, 255, 0.85);
	display: flex;
	justify-content: space-between;
	color: var(--text-color-secondary);
	font-size: var(--font-caption);
	position: absolute;
	bottom: 0;
	width: calc(100% - 2rem);
}
.p-card .p-card-footer .p-button-icon-only {
	visibility: hidden;
}

.p-card:hover .p-card-footer .p-button-icon-only {
	visibility: visible;
}

.p-card-footer.skeleton {
	align-items: flex-end;
}

.p-dialog em {
	font-weight: var(--font-weight-semibold);
}

.project-menu-item {
	display: flex;
	gap: 0.5rem;
	padding: 0.5rem 1rem 0.5rem 1rem;
	color: var(--text-color-secondary);
}

.project-menu-item:hover {
	background-color: var(--surface-highlight);
}
</style>
