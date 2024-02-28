<!-- This template is a copy of tera-external-publication with some elements stripped out.  TODO: merge the concept of external publication and document asset -->
<template>
	<tera-asset
		:feature-config="featureConfig"
		:name="highlightSearchTerms(document?.name)"
		:overline="highlightSearchTerms(document?.source)"
		@close-preview="emit('close-preview')"
		:hide-intro="view === DocumentView.PDF"
		:stretch-content="view === DocumentView.PDF"
		:show-sticky-header="view === DocumentView.PDF"
		:is-loading="documentLoading"
	>
		<template #edit-buttons>
			<SelectButton
				:model-value="view"
				@change="if ($event.value) view = $event.value;"
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
			<ContextMenu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
		</template>
		<Accordion
			v-if="view === DocumentView.EXTRACTIONS"
			:multiple="true"
			:active-index="[0, 1, 2, 3, 4, 5, 6, 7]"
		>
			<AccordionTab v-if="!isEmpty(formattedAbstract)">
				<template #header>
					<header id="Abstract">Abstract</header>
				</template>
				<p v-html="formattedAbstract" />
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(figures)">
				<template #header>
					<header id="Figures">
						Figures<span class="artifact-amount">({{ figures.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="(ex, index) in figures" :key="index" class="extracted-item">
						<Image id="img" class="extracted-image" :src="ex.metadata?.url" :alt="''" preview />
						<tera-show-more-text
							:text="highlightSearchTerms(ex.metadata?.content ?? '')"
							:lines="previewLineLimit"
						/>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(tables)">
				<template #header>
					<header id="Tables">
						Tables<span class="artifact-amount">({{ tables.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="(ex, index) in tables" :key="index" class="extracted-item">
						<div class="extracted-image">
							<Image id="img" :src="ex.metadata?.url" :alt="''" preview />
							<tera-show-more-text
								:text="highlightSearchTerms(ex.metadata?.content ?? '')"
								:lines="previewLineLimit"
							/>
						</div>
					</li>
				</ul>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(equations)">
				<template #header>
					<header id="Equations">
						Equations<span class="artifact-amount">({{ equations.length }})</span>
					</header>
				</template>
				<ul>
					<li v-for="(ex, index) in equations" :key="index" class="extracted-item">
						<div class="extracted-image">
							<Image id="img" :src="ex.metadata?.url" :alt="''" preview />
							<tera-show-more-text
								:text="highlightSearchTerms(ex.metadata?.content ?? '')"
								:lines="previewLineLimit"
							/>
						</div>
						<tera-math-editor v-if="ex.metadata.equation" :latex-equation="ex.metadata.equation" />
					</li>
				</ul>
			</AccordionTab>
		</Accordion>
		<!-- Adding this here for now...we will need a way to listen to the extraction job since this takes some time in the background when uploading a doucment-->
		<p
			class="pl-3"
			v-if="
				isEmpty(document?.assets) &&
				view === DocumentView.EXTRACTIONS &&
				viewOptions[1]?.value === DocumentView.PDF
			"
		>
			PDF Extractions may still be processsing please refresh in some time...
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
import { computed, ref, watch, onUpdated } from 'vue';
import { isEmpty } from 'lodash';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { FeatureConfig } from '@/types/common';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import * as textUtil from '@/utils/text';
import TeraAsset from '@/components/asset/tera-asset.vue';
import type { DocumentAsset } from '@/types/Types';
import { AssetType, ExtractionAssetType } from '@/types/Types';
import {
	downloadDocumentAsset,
	getDocumentAsset,
	getDocumentFileAsText
} from '@/services/document-assets';
import Image from 'primevue/image';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import SelectButton from 'primevue/selectbutton';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import TeraTextEditor from './tera-text-editor.vue';

enum DocumentView {
	EXTRACTIONS = 'Extractions',
	PDF = 'PDF',
	TXT = 'Text',
	NOT_FOUND = 'Not found'
}
const props = defineProps<{
	assetId: string;
	highlight?: string;
	previewLineLimit: number;
	featureConfig?: FeatureConfig;
}>();

const document = ref<DocumentAsset | null>(null);
const pdfLink = ref<string | null>(null);
const view = ref(DocumentView.EXTRACTIONS);

const extractionsOption = { value: DocumentView.EXTRACTIONS, icon: 'pi pi-list' };
const pdfOption = { value: DocumentView.PDF, icon: 'pi pi-file-pdf' };
const txtOption = { value: DocumentView.TXT, icon: 'pi pi-file' };
const notFoundOption = { value: DocumentView.NOT_FOUND, icon: 'pi pi-file', disabled: true };

const viewOptions = computed(() => {
	const options: { value: DocumentView; icon: string; disabled?: boolean }[] = [extractionsOption];
	const filename = document.value?.fileNames?.[0];
	const isPdf = filename?.endsWith('.pdf');

	if (isPdf) {
		options.push(pdfOption);
	} else if (docText.value) {
		options.push(txtOption);
	} else {
		options.push(notFoundOption);
	}
	return options;
});

const docText = ref<string | null>(null);

const documentLoading = ref(false);

const figures = computed(
	() =>
		document.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Figure) || []
);
const tables = computed(
	() =>
		document.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Table) || []
);
const equations = computed(
	() =>
		document.value?.assets?.filter((asset) => asset.assetType === ExtractionAssetType.Equation) ||
		[]
);

const optionsMenu = ref();
const optionsMenuItems = ref([
	{
		icon: 'pi pi-plus',
		label: 'Add to project',
		items:
			useProjects()
				.allProjects.value?.filter(
					(project) => project.id !== useProjects().activeProject.value?.id
				)
				.map((project) => ({
					label: project.name,
					command: async () => {
						const response = await useProjects().addAsset(
							AssetType.Document,
							props.assetId,
							project.id
						);
						if (response) logger.info(`Added asset to ${project.name}`);
						else logger.error('Failed to add asset to project');
					}
				})) ?? []
	}
	// ,{ icon: 'pi pi-trash', label: 'Remove', command: deleteDataset }
]);

const emit = defineEmits(['close-preview', 'asset-loaded']);

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

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
				pdfLink.value = await downloadDocumentAsset(props.assetId, filename); // Generate PDF download link on (doi change)
				view.value = DocumentView.PDF;
			} else {
				docText.value =
					filename && document.value?.id
						? document.value?.text ?? (await getDocumentFileAsText(document.value.id, filename))
						: document.value?.text ?? null;
				if (docText.value !== null) view.value = DocumentView.TXT;
			}

			documentLoading.value = false;
		} else {
			document.value = null;
		}
	},
	{ immediate: true }
);

const formattedAbstract = computed(() => {
	if (!document.value || !document.value.description) return '';
	return highlightSearchTerms(document.value.description);
});

onUpdated(() => {
	if (document.value) {
		emit('asset-loaded');
	}
});
</script>
<style scoped>
.container {
	margin-left: 1rem;
	margin-right: 1rem;
	max-width: 70rem;
}

.extracted-item {
	border: 1px solid var(--surface-border-light);
	padding: 1rem;
	border-radius: var(--border-radius);
}

.extracted-item > .extracted-image {
	display: block;
	max-width: 30rem;
	margin-bottom: 0.5rem;
	width: fit-content;
	padding: 8px;
	border: 1px solid var(--gray-300);
	border-radius: 6px;
	object-fit: contain;
}
</style>
