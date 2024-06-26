<template>
	<DataTable
		:value="projects"
		dataKey="id"
		:rowsPerPageOptions="[10, 20, 50]"
		scrollable
		scrollHeight="45rem"
	>
		<Column
			v-for="(col, index) in selectedColumns"
			:field="col.field"
			:header="col.header"
			:sortable="col.field !== 'stats'"
			:key="index"
			:style="`width: ${getColumnWidth(col.field)}%`"
		>
			<template #body="{ data }">
				<template v-if="col.field === 'name'">
					<a class="project-title-link" @click.stop="emit('open-project', data.id)">
						{{ data.name }}
					</a>
				</template>
				<tera-show-more-text
					v-else-if="col.field === 'description'"
					:text="data.description"
					:lines="1"
				/>
				<template v-if="col.field === 'userName'">
					{{ data.userName ?? '--' }}
				</template>
				<div v-else-if="col.field === 'stats'" class="stats">
					<span class="mr-1"><i class="pi pi-user mr-1" />1</span>
					<span class="mr-1"
						><i class="pi pi-file mr-1" /> {{ data.metadata?.['publications-count'] }}</span
					>
					<span class="mr-1">
						<dataset-icon fill="var(--text-color-secondary)" class="mr-1" />
						{{ data.metadata?.['datasets-count'] }}
					</span>
					<span><i class="pi pi-share-alt mr-1" /> {{ data.metadata?.['models-count'] }}</span>
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
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import { formatDdMmmYyyy } from '@/utils/date';
import DatasetIcon from '@/assets/svg/icons/dataset.svg?component';
import { Project } from '@/types/Types';
import TeraProjectMenu from './tera-project-menu.vue';

defineProps<{
	projects: Project[];
	selectedColumns: { field: string; header: string }[];
}>();

const emit = defineEmits(['open-project']);

function getColumnWidth(columnField: string) {
	switch (columnField) {
		case 'description':
			return 40;
		case 'name':
			return 40;
		default:
			return 100;
	}
}
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
	width: 2rem;
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
	padding: 1rem 0.5rem;
	background-color: var(--surface-ground);
}

.p-datatable:deep(.p-datatable-tbody > tr:not(.p-highlight):focus) {
	background-color: transparent;
}

.p-datatable:deep(.p-datatable-tbody > tr > td) {
	color: var(--text-color-secondary);
	padding: 0.5rem;
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
	cursor: pointer;
}

.p-datatable:deep(.p-datatable-tbody > tr > td > a:hover) {
	color: var(--primary-color);
	text-decoration: underline;
}
</style>
