<template>
	<tera-asset
		v-if="dataset"
		:name="dataset?.name"
		:is-editable="isEditable"
		:stretch-content="datasetView === DatasetView.DATA"
		@close-preview="emit('close-preview')"
	>
		<template #edit-buttons>
			<span class="p-buttonset">
				<Button
					class="p-button-secondary p-button-sm"
					label="Description"
					icon="pi pi-list"
					@click="datasetView = DatasetView.DESCRIPTION"
					:active="datasetView === DatasetView.DESCRIPTION"
				/>
				<Button
					class="p-button-secondary p-button-sm"
					label="Data"
					icon="pi pi-file"
					@click="datasetView = DatasetView.DATA"
					:active="datasetView === DatasetView.DATA"
				/>
			</span>
		</template>
		<template v-if="datasetView === DatasetView.DESCRIPTION">
			<div class="container">
				<Message class="inline-message" icon="none"
					>This page describes the dataset. Use the content switcher above to see the data table and
					transformation tools.</Message
				>
			</div>
			<section class="metadata data-row">
				<section>
					<header>Rows</header>
					<section>{{ csvContent?.length || '-' }}</section>
				</section>
				<section>
					<header>Columns</header>
					<section>{{ rawContent?.stats?.length || '-' }}</section>
					<header>Metadata</header>
					<section>{{ dataset?.metadata || '-' }}</section>
				</section>
				<section>
					<header>Date uploaded</header>
					<section>
						{{ new Date(dataset?.timestamp as Date).toLocaleString('eu-ZA') || '-' }}
					</section>
				</section>
				<section>
					<header>Uploaded by</header>
					<section>{{ dataset?.username || '-' }}</section>
				</section>
			</section>
			<section class="metadata data-row">
				<section>
					<header>Source Name</header>
					<section>{{ dataset?.source || '-' }}</section>
				</section>
				<section>
					<header>Source URL</header>
					<section>
						<a v-if="dataset?.url" :href="dataset?.url">{{ dataset?.url || '-' }}</a>
						<span v-else>-</span>
					</section>
				</section>
			</section>
			<RelatedPublications />
			<Accordion :multiple="true" :activeIndex="showAccordion">
				<AccordionTab>
					<template #header>
						<header id="Description">Description</header>
					</template>
					<p v-html="dataset?.description" />
				</AccordionTab>
				<AccordionTab v-if="(annotations?.length || 0) > 0">
					<template #header>
						<header id="Annotations">
							Annotations
							<span class="artifact-amount"> ({{ annotations?.length || 0 }}) </span>
						</header>
					</template>
					<section v-if="annotations">
						<header class="annotation-subheader">Annotations</header>
						<section class="annotation-group">
							<section
								v-for="name in annotations.map((annotation) => annotation['name'])"
								:key="name[0]"
								class="annotation-row data-row"
							>
								<section>
									<header>Name</header>
									<section>{{ name }}</section>
								</section>
								<section>
									<header>Description</header>
									<section>{{ annotations[name[0]] }}</section>
								</section>
							</section>
						</section>
					</section>
				</AccordionTab>
			</Accordion>
			<Accordion :multiple="true" :activeIndex="[0, 1]">
				<!-- 	<AccordionTab>
					<template #header>
						<header id="Description">Description</header>
					</template>
					<p v-html="dataset?.description" />
				</AccordionTab>
				<AccordionTab v-if="(annotations?.length || 0) > 0">
					<template #header>
						<header id="Annotations">
							Annotations
							<span class="artifact-amount"> ({{ annotations?.length || 0 }}) </span>
						</header>
					</template>
					<section v-if="annotations">
						<header class="annotation-subheader">Annotations</header>
						<section class="annotation-group">
							<section
								v-for="name in annotations.map((annotation) => annotation['name'])"
								:key="name[0]"
								class="annotation-row data-row"
							>
								<section>
									<header>Name</header>
									<section>{{ name }}</section>
								</section>
								<section>
									<header>Description</header>
									<section>{{ annotations[name[0]] }}</section>
								</section>
							</section>
						</section>
					</section>
					<section v-if="annotations?.date">
						<header class="annotation-subheader">Temporal annotations</header>
							<section
								v-for="annotation in annotations?.date"
								:key="annotation.name"
								class="metadata data-row"
							>
								<section>
									<header>Name</header>
									<section>{{ annotation.name }}</section>
								</section>
								<section>
									<header>Description</header>
									<section>{{ annotation.description }}</section>
								</section>
								<section>
									<header>Time format</header>
									<section>{{ annotation.timeFormat }}</section>
								</section>
							</section>
					</section>
				</AccordionTab>
				-->

				<AccordionTab v-if="(annotations?.feature?.length || 0) > 0">
					<template #header>
						<header id="Variables">
							Variables<span class="artifact-amount">({{ annotations?.feature?.length }})</span>
						</header>
					</template>
					<DataTable :value="annotations?.feature">
						<Column field="name" header="Name"></Column>
						<Column field="featureType" header="Type"></Column>
						<Column field="description" header="Definition"></Column>
						<Column field="units" header="Units"></Column>
						<Column field="concept" header="Concept"></Column>
					</DataTable>
				</AccordionTab>
			</Accordion>
		</template>
		<Accordion v-else-if="DatasetView.DATA" :activeIndex="0">
			<AccordionTab>
				<template #header>
					Data preview<span class="artifact-amount">({{ csvContent?.length }} rows)</span>
				</template>
				<tera-dataset-datatable :raw-content="rawContent" />
			</AccordionTab>
		</Accordion>
	</tera-asset>
</template>
<script setup lang="ts">
import { downloadRawFile, getDataset } from '@/services/dataset';
import { computed, ref, watch, onUpdated, Ref } from 'vue';
import Accordion from 'primevue/accordion';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import Message from 'primevue/message';
import * as textUtil from '@/utils/text';
import { isString } from 'lodash';
import { CsvAsset, Dataset } from '@/types/Types';
import teraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import RelatedPublications from '../widgets/tera-related-publications.vue';

enum DatasetView {
	DESCRIPTION = 'description',
	DATA = 'data'
}

const props = defineProps<{
	assetId: string;
	isEditable: boolean;
	highlight?: string;
}>();

const emit = defineEmits(['close-preview', 'asset-loaded']);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

const dataset: Ref<Dataset | null> = ref(null);
const rawContent: Ref<CsvAsset | null> = ref(null);
const datasetView = ref(DatasetView.DESCRIPTION);

const csvContent = computed(() => rawContent.value?.csv);

/*
// apparently this isn't used?
const datasetContent = computed(() => [
	{ key: 'Description', value: dataset.value?.description },
	{
		key: 'Annotations',
		value: [...(annotations.value ?? [])]
	}
]);
*/

onUpdated(() => {
	if (dataset.value) {
		emit('asset-loaded');
	}
});

// Whenever assetId changes, fetch dataset with that ID
watch(
	() => [props.assetId],
	async () => {
		if (props.assetId !== '') {
			const datasetTemp: Dataset | null = await getDataset(props.assetId);

			// We are assuming here there is only a single csv file. This may change in the future as the API allows for it.
			rawContent.value = await downloadRawFile(props.assetId, datasetTemp?.fileNames?.[0] ?? '');
			if (datasetTemp) {
				Object.entries(datasetTemp).forEach(([key, value]) => {
					if (isString(value)) {
						datasetTemp[key] = highlightSearchTerms(value);
					}
				});
				dataset.value = datasetTemp;
				console.log(dataset.value);
			}
		} else {
			dataset.value = null;
			rawContent.value = null;
		}
	},
	{ immediate: true }
);

const annotations = computed(() => dataset.value?.columns?.map((column) => column.annotations));
const showAccordion = computed(() => {
	if (dataset.value?.columns) {
		return dataset.value?.columns?.map((column) => column?.annotations ?? 0)?.length > 0
			? [1]
			: [0];
	}

	return [0];
});
</script>

<style scoped>
.container {
	margin-left: 1rem;
	margin-right: 1rem;
	max-width: 70rem;
}
.inline-message:deep(.p-message-wrapper) {
	padding-top: 0.5rem;
	padding-bottom: 0.5rem;
	background-color: var(--surface-highlight);
	color: var(--text-color-primary);
	border-radius: var(--border-radius);
	border: 4px solid var(--primary-color);
	border-width: 0px 0px 0px 6px;
}
.p-buttonset {
	white-space: nowrap;
	margin-left: 0.5rem;
}
.metadata {
	margin: 1rem;
	display: flex;
	flex-direction: row;
	justify-content: space-evenly;
}

.metadata > section {
	flex: 1;
}

/* Datatable  */
.data-row > section > header {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

.data-row > section > section:last-child {
	font-size: var(--font-body-small);
}

.feature-type {
	color: var(--text-color-subdued);
}

.description {
	padding: 1rem;
	padding-bottom: 0.5rem;
	max-width: var(--constrain-width);
}

.layout-topbar {
	top: 20px;
	background-color: red;
}
</style>
