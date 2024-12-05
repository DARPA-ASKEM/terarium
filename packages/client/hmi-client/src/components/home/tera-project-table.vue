<template>
	<!--The pt wrapper styling enables the table header to stick with the project search bar-->
	<DataTable
		:value="projects"
		:rows="numberOfRows"
		:rows-per-page-options="[10, 20, 30, 40, 50]"
		:pt="{ wrapper: { style: { overflow: 'none' } } }"
		:key="projectTableKey"
		data-key="id"
		paginator
		v-model:expandedRows="dataExpandedRows"
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
				</template>
				<template v-else-if="col.field === 'description'">
					<tera-show-more-text :text="data.description" :lines="1" />
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
		<template #expansion="{ data }">
			<div v-for="asset in data.assets" :key="asset.assetId" class="flex align-items-center gap-4">
				<tera-asset-button
					v-if="asset.assetType != AssetType.Project"
					:asset="asset"
					@click="emit('open-asset', data.id, asset.assetId, asset.assetType)"
				/>
				<p class="font-semibold">{{ capitalize(asset.embeddingType) }}</p>
				<tera-show-more-text :text="asset.embeddingContent" :lines="3" />
			</div>
		</template>
	</DataTable>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { v4 as uuidv4 } from 'uuid';
import { capitalize } from '@/utils/text';
import { formatDdMmmYyyy } from '@/utils/date';
import Column from 'primevue/column';
import DataTable, { DataTableExpandedRows } from 'primevue/datatable';
import TeraAssetButton from '@/components/asset/tera-asset-button.vue';
import TeraAssetIcon from '@/components/widgets/tera-asset-icon.vue';
import TeraProjectMenu from '@/components/home/tera-project-menu.vue';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import { AssetType } from '@/types/Types';
import type { ProjectWithKnnData } from '@/types/Project';
import { isEmpty } from 'lodash';

const props = defineProps<{
	projects: ProjectWithKnnData[];
	selectedColumns: { field: string; header: string }[];
	searchQuery: string;
}>();

const emit = defineEmits(['open-project', 'open-asset']);

const numberOfRows = ref(20);
const projectTableKey = ref(uuidv4());

const dataExpandedRows: DataTableExpandedRows = props.projects.reduce((acc, project) => {
	acc[project.id!] = !isEmpty(project.assets);
	return acc;
}, {});

const columnWidthMap = {
	name: '25%',
	description: '25%',
	userName: '15%',
	stats: '15%',
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

watch(
	() => props.projects,
	() => {
		projectTableKey.value = uuidv4();
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
	top: 104px;
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

/* Adjust padding for non-empty expansion rows */
:deep(.p-datatable-tbody > tr.p-datatable-row-expansion > :not(td:empty)) {
	padding: var(--gap-2) var(--gap-1);
}

/* Remove padding for empty expansion rows */
:deep(.p-datatable-tbody > tr.p-datatable-row-expansion > td:empty) {
	border: none;
	padding: 0;
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
