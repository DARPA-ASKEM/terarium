<template>
	<DataTable
		:value="projects"
		dataKey="id"
		:rowsPerPageOptions="[5, 10, 20]"
		scrollable
		scrollHeight="45rem"
		paginator
		:rows="numberOfRows"
		@page="getProjectAssets"
	>
		<Column
			v-for="(col, index) in selectedColumns"
			:field="col.field"
			:header="col.header"
			:sortable="!['stats', 'score'].includes(col.field)"
			:key="index"
			:style="`width: ${getColumnWidth(col.field)}%`"
		>
			<template #body="{ data }">
				<template v-if="col.field === 'score'">
					{{ Math.round((data.metadata?.score ?? 0) * 100) + '%' }}
				</template>
				<template v-if="col.field === 'name'">
					<a @click.stop="emit('open-project', data.id)">{{ data.name }}</a>
					<ul>
						<li
							v-for="(asset, index) in projectsWithKnnMatches.find(({ id }) => id === data.id)?.projectAssets"
							class="flex align-center gap-2"
							:key="index"
						>
							<tera-asset-icon :assetType="asset.assetType" />
							<span v-html="highlight(asset.assetName, searchQuery)" />
						</li>
					</ul>
				</template>
				<tera-show-more-text v-else-if="col.field === 'description'" :text="data.description" :lines="1" />
				<template v-if="col.field === 'userName'">
					{{ data.userName ?? '--' }}
				</template>
				<div v-else-if="col.field === 'stats'" class="stats">
					<span
						class="mr-1"
						v-tooltip.top="formatStatTooltip(formatStat(data.metadata, 'contributor-count'), 'contributor')"
					>
						<i class="pi pi-user mr-1" />
						{{ formatStat(data.metadata, 'contributor-count') }}
					</span>
					<span class="mr-1" v-tooltip.top="formatStatTooltip(formatStat(data.metadata, 'document-count'), 'paper')">
						<i class="pi pi-file mr-1" />
						{{ formatStat(data.metadata, 'document-count') }}
					</span>
					<span class="mr-1" v-tooltip.top="formatStatTooltip(formatStat(data.metadata, 'datasets-count'), 'dataset')">
						<dataset-icon fill="var(--text-color-secondary)" class="mr-1" />
						{{ formatStat(data.metadata, 'datasets-count') }}
					</span>
					<span v-tooltip.top="formatStatTooltip(formatStat(data.metadata, 'models-count'), 'model')">
						<i class="pi pi-share-alt mr-1" />
						{{ formatStat(data.metadata, 'models-count') }}
					</span>
					<span v-tooltip.top="formatStatTooltip(formatStat(data.metadata, 'workflows-count'), 'workflow')">
						<vue-feather
							class="p-button-icon-left"
							type="git-merge"
							size="1.25rem"
							stroke="var(--text-color-secondary)"
						/>
						{{ formatStat(data.metadata, 'workflows-count') }}
					</span>
				</div>
				<template v-else-if="col.field === 'createdOn'">
					{{ data.createdOn ? formatDdMmmYyyy(data.createdOn) : '--' }}
				</template>
				<template v-else-if="col.field === 'updatedOn'">
					{{ data.updatedOn ? formatDdMmmYyyy(data.updatedOn) : '--' }}
				</template>
			</template>
		</Column>
		<Column style="width: 0">
			<template #body="{ data }">
				<tera-project-menu :project="data" />
			</template>
		</Column>
	</DataTable>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import { formatDdMmmYyyy } from '@/utils/date';
import DatasetIcon from '@/assets/svg/icons/dataset.svg?component';
import { AssetType, Project } from '@/types/Types';
import type { PageState } from 'primevue/paginator';
import * as ProjectService from '@/services/project';
import { highlight } from '@/utils/text';
import TeraProjectMenu from './tera-project-menu.vue';
import TeraAssetIcon from '../widgets/tera-asset-icon.vue';

const props = defineProps<{
	projects: Project[];
	selectedColumns: { field: string; header: string }[];
	searchQuery: string;
}>();

const emit = defineEmits(['open-project']);

const projectsWithKnnMatches = ref<Project[]>([]);
const numberOfRows = ref(5);
let pageState: PageState = { page: 0, rows: numberOfRows.value, first: 0 };

function formatStat(data, key) {
	const stat = data?.[key];
	return key === 'contributor-count' ? parseInt(stat ?? '1', 10) : parseInt(stat ?? '0', 10);
}

function formatStatTooltip(stat, displayName) {
	return `${stat} ${displayName}${stat === 1 ? '' : 's'}`;
}

function getColumnWidth(columnField: string) {
	switch (columnField) {
		case 'description':
			return 40;
		case 'name':
			return 40;
		case 'score':
			return 5;
		default:
			return 100;
	}
}

async function getProjectAssets(event: PageState = pageState) {
	pageState = event;

	if (isEmpty(props.searchQuery)) {
		projectsWithKnnMatches.value = [];
		return;
	}

	const { page, rows } = event;

	projectsWithKnnMatches.value = (
		await Promise.all(
			props.projects.slice(page * rows, (page + 1) * rows).map(async ({ id }) => {
				const project = await ProjectService.get(id);
				if (!project) return null;
				project.projectAssets = project.projectAssets.filter(
					(asset) =>
						asset.assetName.toLowerCase().includes(props.searchQuery.toLowerCase().trim()) ||
						asset.assetType === AssetType.Simulation // Simulations don't have names
				);
				return project;
			})
		)
	).filter(Boolean) as Project[];

	console.log(props.searchQuery.toLowerCase().trim());
}

watch(
	() => props.searchQuery,
	() => {
		getProjectAssets();
	},
	{ immediate: true }
);
</script>

<style scoped>
.stats {
	display: flex;
	width: fit-content;
	gap: 0.5rem;
	font-size: var(--font-caption);
	vertical-align: bottom;
}

.stats span {
	display: flex;
	gap: 0.1rem;
	align-items: center;
	width: 2.4rem;
}

.p-datatable:deep(ul) {
	margin-top: var(--gap-4);
	color: var(--text-color-primary);
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	font-size: var(--font-caption);
}

.p-datatable:deep(li > span) {
	text-overflow: ellipsis;
	display: block;
	overflow: hidden;
	width: 14rem;
}

.p-datatable:deep(.highlight) {
	font-weight: var(--font-weight-semibold);
}

.p-datatable {
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
}

.p-datatable:deep(.p-datatable-tbody > tr > td),
.p-datatable:deep(.p-datatable-thead > tr > th) {
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	vertical-align: top;
}

.p-datatable:deep(.p-datatable-thead > tr > th) {
	padding-left: var(--gap-5);
	background-color: var(--surface-ground);
}

.p-datatable:deep(.p-datatable-tbody > tr:not(.p-highlight):focus) {
	background-color: transparent;
}

.p-datatable:deep(.p-datatable-tbody > tr > td) {
	padding-left: var(--gap-5);
	color: var(--text-color-secondary);
	max-width: 32rem;
}

.p-datatable:deep(.p-datatable-tbody > tr > td:not(:last-child)) {
	padding-top: 1rem;
}

.p-datatable:deep(.p-datatable-tbody > tr .project-options) {
	visibility: hidden;
	float: right;
}

.p-datatable:deep(.p-datatable-tbody > tr:hover .project-options) {
	visibility: visible;
}
.p-datatable:deep(.p-datatable-tbody > tr > td > a) {
	color: var(--text-color-primary);
	font-weight: var(--font-weight-semibold);
	text-overflow: ellipsis;
	display: block;
	overflow: hidden;
	width: 15rem;
}

.p-datatable:deep(.p-datatable-tbody > tr > td > a:hover) {
	color: var(--primary-color);
	text-decoration: underline;
}
</style>
