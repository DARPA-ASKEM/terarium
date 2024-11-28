<template>
	<!--The pt wrapper styling enables the table header to stick with the project search bar-->
	<DataTable
		ref="projectTableRef"
		:value="projectsWithKnnMatches"
		:rows="numberOfRows"
		:rows-per-page-options="[10, 20, 30, 40, 50]"
		:pt="{ wrapper: { style: { overflow: 'none' } } }"
		data-key="id"
		paginator
		@page="getProjectAssets"
	>
		<Column
			v-for="(col, index) in selectedColumns"
			:field="col.field"
			:header="col.header"
			:sortable="!['stats'].includes(col.field)"
			:key="index"
			:style="{ width: columnWidthMap[col.field] ?? '5%' }"
		>
			<template #body="{ data }">
				<template v-if="col.field === 'name'">
					<span class="flex align-items-center gap-2">
						<a @click.stop="emit('open-project', data.id)">{{ data.name }}</a>
						<tera-project-menu class="project-options" :project="data" />
					</span>
					<ul>
						<li
							v-for="asset in data.projectAssets.slice(0, data.showMore ? data.projectAssets.length : 3)"
							class="flex align-center gap-2"
							:key="asset.id"
						>
							<tera-asset-icon :assetType="asset.assetType" />
							<span v-html="highlight(asset.assetName, searchQuery)" />
						</li>
					</ul>
					<Button
						v-if="data.projectAssets.length > 3"
						class="p-2 mt-2"
						:label="data.showMore ? 'Show less' : 'Show more'"
						text
						size="small"
						@click="data.showMore = !data.showMore"
					/>
				</template>
				<template v-else-if="col.field === 'description'">
					<tera-show-more-text :text="data.description" :lines="1" />
					<p v-if="data.snippet" class="mt-2" v-html="data.snippet" />
				</template>
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
import Button from 'primevue/button';
import TeraProjectMenu from './tera-project-menu.vue';
import TeraAssetIcon from '../widgets/tera-asset-icon.vue';

interface ProjectWithKnnSnippet extends Project {
	snippet?: string;
	showMore?: boolean;
}

const props = defineProps<{
	projects: Project[];
	selectedColumns: { field: string; header: string }[];
	searchQuery: string;
}>();

const emit = defineEmits(['open-project']);

const projectTableRef = ref();
const projectsWithKnnMatches = ref<ProjectWithKnnSnippet[]>([]);
const numberOfRows = ref(20);

let pageState: PageState = { page: 0, rows: numberOfRows.value, first: 0 };
let prevSearchQuery = '';

const columnWidthMap = {
	name: '30%',
	description: '30%',
	userName: '5%',
	stats: '15%',
	createdOn: '5%',
	updatedOn: '5%'
};

function formatStat(data, key) {
	const stat = data?.[key];
	return key === 'contributor-count' ? parseInt(stat ?? '1', 10) : parseInt(stat ?? '0', 10);
}

function formatStatTooltip(stat, displayName) {
	return `${stat} ${displayName}${stat === 1 ? '' : 's'}`;
}

async function getProjectAssets(event: PageState = pageState) {
	pageState = event; // Save the current page state so we still know it when the watch is triggered
	if (isEmpty(props.searchQuery)) return;

	const { rows, first } = event;
	const searchQuery = props.searchQuery.toLowerCase().trim();

	// Just fetch the asset data we are seeing in the current page
	projectsWithKnnMatches.value.slice(first, first + rows).forEach(async (project) => {
		// If assets were fetched before from when we were on that page don't redo it
		if (!isEmpty(project.projectAssets) && prevSearchQuery === searchQuery) return;

		const projectWithAssets = (await ProjectService.get(project.id)) as ProjectWithKnnSnippet | null;
		if (!projectWithAssets) return;

		project.projectAssets = projectWithAssets.projectAssets.filter(
			({ assetName, assetType }) =>
				assetName.toLowerCase().includes(searchQuery) &&
				// These assets dont have names
				assetType !== AssetType.Simulation &&
				assetType !== AssetType.NotebookSession
		);
		project.showMore = false;
		project.snippet = project.description?.toLowerCase().includes(searchQuery)
			? highlight(project.description, searchQuery)
			: undefined;
	});

	prevSearchQuery = searchQuery;
}

watch(
	() => props.projects,
	() => {
		// Reset the page to 0 when a new search is performed
		if (pageState.page !== 0) {
			const firstPageButton = projectTableRef.value.$el.querySelector('.p-paginator-first');
			firstPageButton?.dispatchEvent(new MouseEvent('click'));
		}
		projectsWithKnnMatches.value = props.projects;
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

.p-datatable {
	/* Now the table header sticks along with the project search bar */
	&:deep(thead) {
		top: 105px;
		z-index: 1;
	}
}

:deep(.p-paginator-bottom) {
	position: sticky;
	bottom: 0;
	outline: 1px solid var(--surface-border-light);
}

:deep(.p-paginator) {
	border-radius: 0;
	padding: var(--gap-2) var(--gap-4);
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
	/* max-width: 20vw; */
}

.p-datatable:deep(p) {
	color: var(--text-color-primary);
	/* max-width: 22vw; */
	text-overflow: ellipsis;
	display: block;
	overflow: hidden;
}

.p-datatable:deep(.highlight) {
	font-weight: var(--font-weight-semibold);
}

.p-datatable:deep(.p-datatable-tbody > tr > td),
.p-datatable:deep(.p-datatable-thead > tr > th) {
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	vertical-align: top;
}

.p-datatable:deep(.p-datatable-thead > tr > th) {
	padding: var(--gap-3) var(--gap-5);
	background-color: var(--surface-ground);
}

.p-datatable:deep(.p-datatable-tbody > tr:not(.p-highlight):focus) {
	background-color: transparent;
}

.p-datatable:deep(.p-datatable-tbody > tr > td) {
	padding: var(--gap-3) var(--gap-5);
	color: var(--text-color-secondary);
	overflow: hidden;
}

.p-datatable:deep(.project-options) {
	visibility: hidden;
}
.p-datatable:deep(.p-datatable-tbody > tr:hover .project-options) {
	visibility: visible;
}

.p-datatable:deep(.p-datatable-tbody > tr > td a) {
	color: var(--text-color-primary);
	font-weight: var(--font-weight-semibold);
	font-size: 1.05rem;
	text-overflow: ellipsis;
	display: block;
	overflow: hidden;
	/* max-width: 20vw; */
}

.p-datatable:deep(.p-datatable-tbody > tr > td a:hover) {
	color: var(--primary-color);
	text-decoration: underline;
}
</style>
