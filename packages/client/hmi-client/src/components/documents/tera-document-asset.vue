<template>
	<tera-asset
		:id="assetId"
		:feature-config="featureConfig"
		:name="document?.name ?? ''"
		:overline="document?.source ?? ''"
		@close-preview="emit('close-preview')"
		:hide-intro="view === DocumentView.PDF"
		:is-loading="documentLoading"
	>
		<template #edit-buttons>
			<SelectButton
				:model-value="view"
				@change="changeView"
				:options="viewOptions"
				option-value="value"
				option-disabled="disabled"
			>
				<template #option="{ option }">
					<i :class="`${option.icon} p-button-icon-left`" />
					<span class="p-button-label">{{ option.value }}</span>
				</template>
			</SelectButton>
			<Button
				icon="pi pi-ellipsis-v"
				class="p-button-icon-only p-button-text p-button-rounded"
				@click="toggleOptionsMenu"
			/>
			<ContextMenu ref="optionsMenu" :model="optionsMenuItems" :popup="true" :pt="optionsMenuPt" />
		</template>
		<Accordion v-if="view === DocumentView.EXTRACTIONS" :multiple="true" :active-index="[0, 1, 2, 3, 4, 5, 6, 7]">
			<!-- Abstract -->
			<AccordionTab v-if="!isEmpty(formattedAbstract)">
				<template #header>
					<header id="Abstract">Abstract</header>
				</template>
				<p v-html="formattedAbstract" />
			</AccordionTab>

			<!-- Figures -->
			<AccordionTab v-if="!isEmpty(figures)">
				<template #header>
					<header id="Figures">
						Figures<span class="artifact-amount">({{ figures.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="(ex, index) in figures" :key="index" class="extracted-item">
						<Image id="img" class="extracted-image col-4" :src="ex.metadata?.url" :alt="''" preview />
						<tera-show-more-text
							v-if="ex.metadata?.content"
							class="extracted-caption col-7"
							:text="ex.metadata?.content ?? ''"
							:lines="previewLineLimit"
						/>
						<div v-else class="no-extracted-text">No extracted text</div>
					</li>
				</ul>
			</AccordionTab>

			<!-- Tables -->
			<AccordionTab v-if="!isEmpty(tables)">
				<template #header>
					<header id="Tables">
						Tables<span class="artifact-amount">({{ tables.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="(ex, index) in tables" :key="index" class="extracted-item">
						<Image id="img" class="extracted-image col-4" :src="ex.metadata?.url" :alt="''" preview />
						<tera-show-more-text
							v-if="ex.metadata?.content"
							class="extracted-caption col-7"
							:text="ex.metadata?.content ?? ''"
							:lines="previewLineLimit"
						/>
						<div v-else class="no-extracted-text">No extracted text</div>
					</li>
				</ul>
			</AccordionTab>

			<!-- Equations -->
			<AccordionTab v-if="!isEmpty(equations)">
				<template #header>
					<header id="Equations">
						Equations<span class="artifact-amount">({{ equations.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="(ex, index) in equations" :key="index" class="extracted-item">
						<Image id="img" class="extracted-image col-4" :src="ex.metadata?.url" :alt="''" preview />
						<tera-show-more-text
							v-if="ex.metadata?.content"
							class="extracted-caption col-7"
							:text="ex.metadata?.content ?? ''"
							:lines="previewLineLimit"
						/>
						<div v-else class="no-extracted-text">No extracted text</div>

						<tera-math-editor v-if="ex.metadata.equation" :latex-equation="ex.metadata.equation" />
					</li>
				</ul>
			</AccordionTab>
		</Accordion>
		<p
			class="pl-3"
			v-if="
				isEmpty(document?.assets) && view === DocumentView.EXTRACTIONS && viewOptions[1]?.value === DocumentView.PDF
			"
		>
			PDF Extractions are processing please come back in some time...
		</p>
		<tera-pdf-embed
			v-else-if="view === DocumentView.PDF && pdfLink"
			:pdf-link="pdfLink"
			:title="document?.name || ''"
		/>
		<tera-text-editor v-else-if="view === DocumentView.TXT" :initial-text="docText ?? ''" />
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, onUpdated, ref, watch } from 'vue';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import type { FeatureConfig, ExtractionStatusUpdate } from '@/types/common';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import TeraAsset from '@/components/asset/tera-asset.vue';
import type { ClientEvent, DocumentAsset } from '@/types/Types';
import { AssetType, ClientEventType, ExtractionAssetType, ProgressState } from '@/types/Types';
import { downloadDocumentAsset, getDocumentAsset, getDocumentFileAsText } from '@/services/document-assets';
import Image from 'primevue/image';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import SelectButton from 'primevue/selectbutton';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import { subscribe, unsubscribe } from '@/services/ClientEventService';
import TeraTextEditor from './tera-text-editor.vue';

enum DocumentView {
	EXTRACTIONS = 'Extractions',
	PDF = 'PDF',
	TXT = 'Text',
	NOT_FOUND = 'Not found'
}
const props = defineProps<{
	assetId: string;
	previewLineLimit: number;
	featureConfig?: FeatureConfig;
}>();

const emit = defineEmits(['close-preview', 'asset-loaded', 'remove']);

const document = ref<DocumentAsset | null>(null);
const pdfLink = ref<string | null>(null);
const view = ref(DocumentView.EXTRACTIONS);

const extractionsOption = { value: DocumentView.EXTRACTIONS, icon: 'pi pi-list' };
const pdfOption = { value: DocumentView.PDF, icon: 'pi pi-file-pdf' };
const txtOption = { value: DocumentView.TXT, icon: 'pi pi-file' };
const notFoundOption = { value: DocumentView.NOT_FOUND, icon: 'pi pi-file', disabled: true };

const changeView = (event) => {
	if (event.value) {
		view.value = event.value;
	}
};

const viewOptions = computed(() => {
	const options: { value: DocumentView; icon: string; disabled?: boolean }[] = [extractionsOption];
	const isPdf = document.value?.fileNames?.some((file) => file.endsWith('.pdf')) ?? false;

	if (isPdf) {
		options.push({ ...pdfOption, disabled: documentLoading.value });
	} else if (docText.value) {
		options.push({ ...txtOption, disabled: documentLoading.value });
	} else {
		options.push(notFoundOption);
	}
	return options;
});

const docText = ref<string | null>(null);

const documentLoading = ref(false);

const figures = computed(
	() => document.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Figure) || []
);
const tables = computed(
	() => document.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Table) || []
);
const equations = computed(
	() => document.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Equation) || []
);

const optionsMenu = ref();
const optionsMenuItems = ref([
	{
		icon: 'pi pi-plus',
		label: 'Add to project',
		items:
			useProjects()
				.allProjects.value?.filter((project) => project.id !== useProjects().activeProject.value?.id)
				.map((project) => ({
					label: project.name,
					command: async () => {
						const response = await useProjects().addAsset(AssetType.Document, props.assetId, project.id);
						if (response) logger.info(`Added asset to ${project.name}`);
					}
				})) ?? []
	},
	{ icon: 'pi pi-trash', label: 'Remove', command: () => emit('remove') }
]);
const optionsMenuPt = {
	submenu: {
		class: 'max-h-30rem overflow-y-scroll'
	}
};

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

/* TODO: When fetching a document by id, its id and fileNames don't get returned.
 Once they do see about adjusting the conditionals */
watch(
	() => props.assetId,
	async () => {
		if (props.assetId) {
			view.value = DocumentView.EXTRACTIONS;
			pdfLink.value = null;
			documentLoading.value = true;
			document.value = await getDocumentAsset(props.assetId);
			const filename = document.value?.fileNames?.[0];

			if (filename?.endsWith('.pdf')) {
				// Generate PDF download link on assetId change
				downloadDocumentAsset(props.assetId, filename).then((pdfLinkResponse) => {
					if (pdfLinkResponse) {
						pdfLink.value = pdfLinkResponse;
					}
				});
			} else if (filename && document.value?.id) {
				if (document.value?.text) {
					docText.value = document.value.text;
				} else {
					getDocumentFileAsText(document.value.id, filename).then((text) => {
						docText.value = text;
					});
				}
			} else {
				docText.value = document.value?.text ?? null;
			}

			documentLoading.value = false;
		} else {
			document.value = null;
		}
	},
	{ immediate: true }
);

const formattedAbstract = computed<string>(() => document.value?.documentAbstract ?? '');

onUpdated(() => {
	if (document.value) {
		emit('asset-loaded');
	}
});

onMounted(async () => {
	await subscribe(ClientEventType.ExtractionPdf, subscribeToExtraction);
});

async function subscribeToExtraction(event: ClientEvent<ExtractionStatusUpdate>) {
	if (!event.data || event.data.data.documentId !== props.assetId) return;
	const status = event.data.state;
	// FIXME: adding the 'dispatching' check since there seems to be an issue with the status of the extractions.
	if (status === ProgressState.Complete || event.data.message.includes('Dispatching')) {
		document.value = await getDocumentAsset(props.assetId);
	}
}

onUnmounted(async () => {
	await unsubscribe(ClientEventType.ExtractionPdf, subscribeToExtraction);
});
</script>

<style scoped>
.extracted-item {
	display: flex;
	flex-direction: row;
	gap: var(--gap);
}

.extracted-item > .extracted-image {
	display: block;
	padding: 8px;
	border: 1px solid var(--gray-200);
	border-radius: var(--border-radius);
	object-fit: contain;
}

.no-extracted-text {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
	font-style: italic;
	padding: var(--gap-small);
}
</style>
