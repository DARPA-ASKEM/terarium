<template>
	<tera-asset
		v-if="dataset"
		:name="dataset?.name"
		:is-editable="isEditable"
		:stretch-content="datasetView === DatasetView.DATA"
		@close-preview="emit('close-preview')"
	>
		<template #nav>
			<tera-asset-nav
				:asset-content="datasetContent"
				:show-header-links="datasetView === DatasetView.DESCRIPTION"
			>
				<template #viewing-mode>
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
			</tera-asset-nav>
		</template>
		<template v-if="datasetView === DatasetView.DESCRIPTION">
			<section class="metadata data-row">
				<section>
					<header>Metadata</header>
					<section>{{ dataset?.metadata || '-' }}</section>
				</section>
				<section>
					<header>URL</header>
					<section>
						<a :href="dataset?.url">{{ dataset?.url || '-' }}</a>
					</section>
				</section>
				<section>
					<header>Number of records</header>
					<section>{{ csvContent?.length }}</section>
				</section>
			</section>
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
import * as textUtil from '@/utils/text';
import { isString } from 'lodash';
import { CsvAsset, Dataset } from '@/types/Types';
import teraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraAssetNav from '@/components/asset/tera-asset-nav.vue';

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

const datasetContent = computed(() => [
	{ key: 'Description', value: dataset.value?.description },
	{
		key: 'Annotations',
		value: [...(annotations.value ?? [])]
	}
]);

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
			rawContent.value = await downloadRawFile(props.assetId, 10);
			const datasetTemp: Dataset | null = await getDataset(props.assetId);
			if (datasetTemp) {
				Object.entries(datasetTemp).forEach(([key, value]) => {
					if (isString(value)) {
						datasetTemp[key] = highlightSearchTerms(value);
					}
				});
				dataset.value = datasetTemp;
			}
		} else {
			dataset.value = null;
			rawContent.value = null;
		}
	},
	{ immediate: true }
);

const annotations = computed(() => dataset.value?.columns?.map((column) => column.annotations));
const showAccordion = computed(() =>
	dataset.value?.columns?.map((column) => column?.annotations ?? 0).length > 0 ? [1] : [0]
);
</script>

<style scoped>
.metadata {
	margin: 1rem;
	margin-bottom: 0.5rem;
	border: 1px solid var(--surface-border-light);
	border-radius: var(--border-radius);
	background-color: var(--gray-50);
	padding: 0.25rem;
	display: flex;
	flex-direction: row;
	justify-content: space-evenly;
}

.metadata > section {
	flex: 1;
	padding: 0.5rem;
}

/* Datatable  */
.data-row > section > header {
	font-size: var(--font-caption);
	color: var(--text-color-subdued);
}

.data-row > section > section:last-child {
	font-size: var(--font-body-small);
}

.annotation-row > section {
	flex: 1;
	padding: 0.5rem;
}

.numbered-list {
	list-style: numbered-list;
	margin-left: 2rem;
	list-style-position: outside;
}

ol.numbered-list li::marker {
	color: var(--text-color-subdued);
}

.feature-type {
	color: var(--text-color-subdued);
}

.description {
	padding: 1rem;
	padding-bottom: 0.5rem;
	max-width: var(--constrain-width);
}

main .annotation-group {
	padding: 0.25rem;
	border: solid 1px var(--surface-border-light);
	background-color: var(--gray-50);
	border-radius: var(--border-radius);
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin-bottom: 1rem;
	max-width: var(--constrain-width);
}

.annotation-subheader {
	font-weight: var(--font-weight-semibold);
}

.annotation-row {
	display: flex;
	flex-direction: row;
	gap: 3rem;
}

.layout-topbar {
	top: 20px;
	background-color: red;
}
</style>
