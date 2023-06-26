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
				<Button
					v-if="isEditable"
					class="p-button-secondary p-button-sm"
					label="Transform"
					icon="pi pi-sync"
					@click="openDatesetChatTab"
					:active="datasetView === DatasetView.LLM"
				/>
			</span>
			<span v-if="datasetView === DatasetView.LLM && isEditable">
				<i class="pi pi-cog" @click="toggleSettingsMenu" />
				<Menu ref="menu" id="overlay_menu" :model="items" :popup="true" />
			</span>
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
								:key="name"
								class="annotation-row data-row"
							>
								<section>
									<header>Name</header>
									<section>{{ name }}</section>
								</section>
								<section>
									<header>Description</header>
									<section>{{ annotations[name] }}</section>
								</section>
							</section>
						</section>
					</section>
				</AccordionTab>
			</Accordion>
		</template>
		<template v-else-if="datasetView === DatasetView.DATA">
			<Accordion :multiple="true" :activeIndex="[0, 1]">
				<AccordionTab>
					<template #header>
						Data preview<span class="artifact-amount">({{ csvContent?.length }} rows)</span>
					</template>
					<tera-dataset-datatable :rows="100" :raw-content="rawContent" />
				</AccordionTab>
			</Accordion>
		</template>
		<template v-else-if="datasetView === DatasetView.LLM && isEditable">
			<div class="data-transform-container">
				<Accordion :multiple="true" v-if="DatasetView.LLM" :activeIndex="getActiveIndex">
					<AccordionTab>
						<template #header>
							Data Preview<span class="artifact-amount">({{ csvContent?.length }} rows)</span>
						</template>
						<tera-dataset-datatable
							class="tera-dataset-datatable"
							v-if="jupyterCsv"
							:rows="10"
							:raw-content="jupyterCsv"
							:previous-headers="oldCsvHeaders"
						/>
					</AccordionTab>
				</Accordion>

				<div class="gpt-header">
					<span><i class="pi pi-circle-fill kernel-status" :style="statusStyle" /></span>
					<span> <header id="GPT">TGPT</header></span>
				</div>
				<tera-jupyter-chat
					:project="props.project"
					:asset-id="props.assetId"
					:show-jupyter-settings="showKernels"
					:show-chat-thought="showChatThoughts"
					:jupyter-session="jupyterSession"
					@update-table-preview="updateJupyterTable"
					@update-kernel-status="updateKernelStatus"
				/>
			</div>
		</template>
	</tera-asset>
</template>
<script setup lang="ts">
import { computed, ref, watch, onUpdated, Ref } from 'vue';
import Accordion from 'primevue/accordion';
import Button from 'primevue/button';
import AccordionTab from 'primevue/accordiontab';
import { isString } from 'lodash';
import { downloadRawFile, getDataset } from '@/services/dataset';
import * as textUtil from '@/utils/text';
import { CsvAsset, Dataset } from '@/types/Types';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraJupyterChat from '@/components/llm/tera-jupyter-chat.vue';
import { IProject } from '@/types/Project';
import Menu from 'primevue/menu';
import { newSession } from '@/services/jupyter';

const jupyterSession = newSession('llmkernel', 'ChattyNode');

enum DatasetView {
	DESCRIPTION = 'description',
	DATA = 'data',
	LLM = 'llm'
}

const props = defineProps<{
	assetId: string;
	isEditable: boolean;
	highlight?: string;
	project: IProject;
}>();

const emit = defineEmits(['close-preview', 'asset-loaded']);
const kernelStatus = ref('');

const showKernels = ref(<boolean>false);
const showChatThoughts = ref(<boolean>false);
const menu = ref();
const newCsvContent: any = ref(null);
const newCsvHeader: any = ref(null);
const oldCsvHeaders: any = ref(null);
const dataset: Ref<Dataset | null> = ref(null);
const rawContent: Ref<CsvAsset | null> = ref(null);
const jupyterCsv: Ref<CsvAsset | null> = ref(null);

const toggleSettingsMenu = (event: Event) => {
	menu.value.toggle(event);
};

const updateKernelStatus = (statusString: string) => {
	kernelStatus.value = statusString;
};

const datasetView = ref(DatasetView.DESCRIPTION);

const chatThoughtLabel = computed(() =>
	showChatThoughts.value ? 'Auto hide chat thoughts' : 'Do not auto hide chat thoughts'
);

const kernelSettingsLabel = computed(() =>
	showKernels.value ? 'Hide Kernel Settings' : 'Show Kernel Settings'
);

const csvContent = computed(() => rawContent.value?.csv);

const getActiveIndex = computed(() => {
	if (!jupyterCsv.value) {
		return [1];
	}
	return [0, 1];
});

const statusStyle = computed(() => {
	if (kernelStatus.value === 'idle') {
		return 'color: green';
	}

	if (kernelStatus.value === 'busy') {
		return 'color: orange';
	}

	return 'color: red';
});

const items = ref([
	{
		label: 'Chat Options',
		items: [
			{
				label: kernelSettingsLabel,
				command: () => {
					showKernels.value = !showKernels.value;
				}
			},
			{
				label: chatThoughtLabel,
				command: () => {
					showChatThoughts.value = !showChatThoughts.value;
					console.log(showChatThoughts);
				}
			}
		]
	}
]);

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

const openDatesetChatTab = () => {
	datasetView.value = DatasetView.LLM;
	jupyterCsv.value = null;
};

const updateJupyterTable = (newJupyterCsv: CsvAsset) => {
	jupyterCsv.value = newJupyterCsv;
};

onUpdated(() => {
	if (dataset.value) {
		emit('asset-loaded');
	}
});

watch(
	() => [jupyterCsv.value?.csv],
	() => {
		if (jupyterCsv.value?.csv) {
			oldCsvHeaders.value = newCsvHeader.value;
			newCsvContent.value = jupyterCsv.value.csv.slice(1, jupyterCsv.value.csv.length);
			newCsvHeader.value = jupyterCsv.value.headers;
		}
	}
);

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
.p-buttonset {
	white-space: nowrap;
	margin-left: 0.5rem;
}
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

.tera-dataset-datatable {
	width: 100%;
}

.data-transform-container {
	display: flex;
	flex-direction: column;
	padding: 0.5rem;
	margin: 0.5rem;
	max-height: 90%;
}

.kernel-status {
	margin-right: 10px;
}

.gpt-header {
	display: flex;
	width: 90%;
}
</style>
