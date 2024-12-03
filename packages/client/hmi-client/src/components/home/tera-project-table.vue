<template>
	<!--The pt wrapper styling enables the table header to stick with the project search bar-->
	<DataTable
		:value="projectsWithKnnMatches"
		:rows="numberOfRows"
		:rows-per-page-options="[10, 20, 30, 40, 50]"
		:pt="{ wrapper: { style: { overflow: 'none' } } }"
		:key="projectTableKey"
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
			:style="{ width: columnWidthMap[col.field] }"
		>
			<template #body="{ data }">
				<template v-if="col.field === 'name'">
					<span class="flex align-items-center gap-2">
						<a @click.stop="emit('open-project', data.id)">{{ data.name }}</a>
						<tera-project-menu class="project-options" :project="data" />
					</span>
					<ul>
						<li v-for="hit in data.hits" class="flex align-center gap-2" :key="hit.id">
							<tera-asset-icon :asset-type="hit.assetType" />
							{{ hit }}
							<!-- <span v-html="highlight(asset.name, searchQuery)" />
							{{ asset.name }}
							{{ asset.description }} -->
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
				<template v-else-if="col.field === 'stats'">
					<div class="stats">
						<span v-tooltip.top="formatStatTooltip(data.metadata?.['contributor-count'] ?? 1, 'contributor')">
							<i class="pi pi-user" />
							{{ data.metadata?.['contributor-count'] ?? 1 }}
						</span>
					</div>
					<div class="stats mt-3">
						<span
							v-for="metadataField in Object.keys(metadataCountToAssetNameMap)"
							:key="metadataField"
							v-tooltip.top="
								formatStatTooltip(data.metadata?.[metadataField] ?? 0, metadataCountToAssetNameMap[metadataField])
							"
						>
							<tera-asset-icon
								:asset-type="metadataCountToAssetNameMap[metadataField]"
								custom-fill="var(--text-color-secondary)"
							/>
							{{ data.metadata?.[metadataField] ?? 0 }}
						</span>
					</div>
				</template>
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
import { isEmpty, cloneDeep } from 'lodash';
import { ref, watch } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import { formatDdMmmYyyy } from '@/utils/date';
import { AssetType } from '@/types/Types';
import type { ProjectWithKnnData } from '@/types/Project';
import type { PageState } from 'primevue/paginator';
// import { highlight } from '@/utils/text';
import Button from 'primevue/button';
import { v4 as uuidv4 } from 'uuid';
import TeraAssetIcon from '@/components/widgets/tera-asset-icon.vue';
import TeraProjectMenu from './tera-project-menu.vue';
// Gets (I think these are the asset types that we want/would typically show up)
// import { getModel } from '@/services/model';
// import { getDataset } from '@/services/dataset';
// import { getDocumentAsset } from '@/services/document-assets';
// import { getWorkflow } from '@/services/workflow';
// import { getModelConfigurationById } from '@/services/model-configurations';

const props = defineProps<{
	projects: ProjectWithKnnData[];
	selectedColumns: { field: string; header: string }[];
	searchQuery: string;
}>();

const emit = defineEmits(['open-project']);

const projectsWithKnnMatches = ref<ProjectWithKnnData[]>([]);
const numberOfRows = ref(20);
const projectTableKey = ref(uuidv4());

let pageState: PageState = { page: 0, rows: numberOfRows.value, first: 0 };
let prevSearchQuery = '';

const columnWidthMap = {
	name: '25%',
	description: '30%',
	userName: '15%',
	stats: '10%',
	createdOn: '10%',
	updatedOn: '10%'
};

const metadataCountToAssetNameMap = {
	'document-count': AssetType.Document,
	'datasets-count': AssetType.Dataset,
	'models-count': AssetType.Model,
	'workflows-count': AssetType.Workflow
};

function formatStatTooltip(amount: number, itemName: string) {
	return `${amount} ${itemName}${amount === 1 ? '' : 's'}`;
}

async function getProjectAssets(event: PageState = pageState) {
	pageState = event; // Save the current page state so we still know it when the watch is triggered
	if (isEmpty(props.searchQuery)) return;

	const { rows, first } = event;
	const searchQuery = props.searchQuery.toLowerCase().trim();

	// Just fetch the asset data we are seeing in the current page
	projectsWithKnnMatches.value.slice(first, first + rows).map(async (project) => {
		// If assets were fetched before from when we were on that page don't redo it
		if (!isEmpty(project.projectAssets) && prevSearchQuery === searchQuery) return;

		// const hit = project?.hits?.[0];

		// if (hit) {
		// 	let relevantAsset: any = null;
		// 	if (hit.assetType === AssetType.Model) {
		// 		relevantAsset = await getModel(hit.assetId);
		// 	} else if (hit.assetType === AssetType.Dataset) {
		// 		relevantAsset = await getDataset(hit.assetId);
		// 	} else if (hit.assetType === AssetType.Document) {
		// 		relevantAsset = await getDocumentAsset(hit.assetId);
		// 	} else if (hit.assetType === AssetType.Workflow) {
		// 		relevantAsset = await getWorkflow(hit.assetId);
		// 	} else if (hit.assetType === AssetType.ModelConfiguration) {
		// 		relevantAsset = await getModelConfigurationById(hit.assetId);
		// 	}
		// 	project.projectAssets.push(relevantAsset);
		// }

		project.showMore = false;
		// project.snippet = project.description?.toLowerCase().includes(searchQuery)
		// 	? highlight(project.description, searchQuery)
		// 	: undefined;
	});
	prevSearchQuery = searchQuery;
	console.log(projectsWithKnnMatches.value);
}

watch(
	() => props.projects,
	() => {
		projectTableKey.value = uuidv4();
		projectsWithKnnMatches.value = cloneDeep(props.projects);
		getProjectAssets();
	},
	{ immediate: true }
);
</script>

<style scoped>
.stats {
	display: flex;
	font-size: var(--font-caption);
	gap: var(--gap-3);

	& span {
		display: flex;
		align-items: center;
		& > * {
			margin-right: var(--gap-1);
		}
	}
}

/* Now the table header sticks along with the project search bar */
:deep(thead) {
	top: 105px;
	z-index: 1;
}

:deep(.p-datatable-tbody > tr > td),
:deep(.p-datatable-thead > tr > th) {
	vertical-align: top;
	padding: var(--gap-3) var(--gap-5);
}

:deep(.p-datatable-thead > tr > th) {
	background-color: var(--surface-ground);
}

:deep(.p-datatable-tbody > tr > td) {
	color: var(--text-color-secondary);
}

:deep(.p-datatable-tbody > tr:not(.p-highlight):focus) {
	background-color: transparent;
}

.p-datatable:deep(.p-datatable-tbody > tr > td a) {
	color: var(--text-color-primary);
	font-weight: var(--font-weight-semibold);
	font-size: 1.05rem;
	display: block;
}
.p-datatable:deep(.p-datatable-tbody > tr > td a:hover) {
	color: var(--primary-color);
	text-decoration: underline;
}

/* Truncate long text for asset list, project name and highlighted description*/
:deep(li > span),
.p-datatable:deep(.p-datatable-tbody > tr > td a),
:deep(p) {
	overflow: hidden;
	white-space: nowrap;
	max-width: 25vw;
	text-overflow: ellipsis;
}

/* Matching asset names and project descriptions */
:deep(ul) {
	margin-top: var(--gap-4);
	color: var(--text-color-primary);
	display: flex;
	flex-direction: column;
	gap: var(--gap-2);
	font-size: var(--font-caption);
}

:deep(p) {
	color: var(--text-color-primary);
}

:deep(.highlight) {
	font-weight: var(--font-weight-semibold);
}

/* Hide/show ellipsis menu on row hover */
:deep(.project-options) {
	visibility: hidden;
}
:deep(tr:hover .project-options) {
	visibility: visible;
}

/* Paginator styles */
:deep(.p-paginator-bottom) {
	position: sticky;
	bottom: 0;
	outline: 1px solid var(--surface-border-light);
}
:deep(.p-paginator) {
	border-radius: 0;
	padding: var(--gap-2) var(--gap-4);
}
</style>
