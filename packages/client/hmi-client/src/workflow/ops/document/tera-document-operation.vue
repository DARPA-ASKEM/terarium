<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div>
			<tera-drilldown-section :is-loading="fetchingPDF">
				<tera-pdf-embed v-if="pdfLink" :pdf-link="pdfLink" :title="document?.name || ''" />
				<!-- <tera-text-editor v-else-if="view === DocumentView.TXT" :initial-text="docText" /> -->
			</tera-drilldown-section>
			<tera-drilldown-preview hide-header>
				<h5>{{ document?.name }}</h5>

				<h7 class="clamp-text">{{ document?.text }}</h7>
				<Accordion multiple :active-index="[0, 1, 2]">
					<AccordionTab v-if="!isEmpty(clonedState.equations)">
						<template #header>
							<header>Equation Images</header>
						</template>
						<tera-expandable-panel
							v-for="(equation, i) in clonedState.equations"
							:key="i"
							hide-delete
							hide-edit
							:is-included="equation.includeInProcess"
							@update:is-included="onUpdateInclude(equation)"
						>
							<template #header>
								<h5>{{ equation.name }}</h5>
							</template>
							<Image id="img" :src="equation.asset?.metadata?.url" :alt="''" preview />
						</tera-expandable-panel>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(clonedState.figures)">
						<template #header>
							<header>Figure Images</header>
						</template>
						<tera-expandable-panel
							v-for="(figure, i) in clonedState.figures"
							:key="i"
							hide-delete
							hide-edit
							:is-included="figure.includeInProcess"
							@update:is-included="onUpdateInclude(figure)"
						>
							<template #header>
								<h5>{{ figure.name }}</h5>
							</template>
							<Image id="img" :src="figure.asset?.metadata?.url" :alt="''" preview />
						</tera-expandable-panel>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(clonedState.tables)">
						<template #header>
							<header>Table Images</header>
						</template>
						<tera-expandable-panel
							v-for="(table, i) in clonedState.tables"
							:key="i"
							hide-delete
							hide-edit
							:is-included="table.includeInProcess"
							@update:is-included="onUpdateInclude(table)"
						>
							<template #header>
								<h5>{{ table.name }}</h5>
							</template>
							<Image id="img" :src="table.asset?.metadata?.url" :alt="''" preview />
						</tera-expandable-panel>
					</AccordionTab>
				</Accordion>
				<template #footer?>
					<Button label="Close" @click="emit('close')"></Button>
				</template>
			</tera-drilldown-preview>
		</div>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { SelectableAsset, WorkflowNode, WorkflowOutput } from '@/types/workflow';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { onMounted, ref, watch } from 'vue';
import { DocumentAsset, DocumentExtraction, ExtractionAssetType } from '@/types/Types';
import { downloadDocumentAsset, getDocumentAsset } from '@/services/document-assets';
import { cloneDeep, isEmpty } from 'lodash';
import TeraExpandablePanel from '@/components/widgets/tera-expandable-panel.vue';
import Image from 'primevue/image';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { DocumentOperationState } from './document-operation';

const emit = defineEmits(['close', 'update-state', 'update-output-port']);
const props = defineProps<{
	node: WorkflowNode<DocumentOperationState>;
}>();

const document = ref<DocumentAsset | null>();
const pdfLink = ref<string | null>();
const fetchingPDF = ref(false);
const clonedState = ref(cloneDeep(props.node.state));

onMounted(async () => {
	if (props.node.state.documentId) {
		fetchingPDF.value = true;
		document.value = await getDocumentAsset(props.node.state.documentId);
		const filename = document.value?.fileNames?.[0];
		if (document.value?.id && filename)
			pdfLink.value = await downloadDocumentAsset(document.value.id, filename);
		fetchingPDF.value = false;
	}
});

function onUpdateInclude(asset: SelectableAsset<DocumentExtraction>) {
	asset.includeInProcess = !asset.includeInProcess;
	emit('update-state', clonedState.value);

	let outputPort: WorkflowOutput<DocumentOperationState> | null = null;

	if (asset.asset.assetType === ExtractionAssetType.Equation) {
		outputPort = cloneDeep(props.node.outputs?.find((port) => port.type === 'equations')) || null;
		if (!outputPort) return;
		const selected = clonedState.value.equations?.filter((eq) => eq.includeInProcess) ?? [];
		outputPort.label = `Equations (${selected?.length}/${clonedState.value.equations?.length})`;
	}

	if (asset.asset.assetType === ExtractionAssetType.Figure) {
		outputPort = cloneDeep(props.node.outputs?.find((port) => port.type === 'figures')) || null;
		if (!outputPort) return;
		const selected = clonedState.value.figures?.filter((eq) => eq.includeInProcess) ?? [];
		outputPort.label = `Figures (${selected?.length}/${clonedState.value.figures?.length})`;
	}

	if (asset.asset.assetType === ExtractionAssetType.Table) {
		outputPort = cloneDeep(props.node.outputs?.find((port) => port.type === 'tables')) || null;
		if (!outputPort) return;
		const selected = clonedState.value.tables?.filter((eq) => eq.includeInProcess) ?? [];
		outputPort.label = `Tables (${selected?.length}/${clonedState.value.tables?.length})`;
	}

	emit('update-output-port', outputPort);
}

watch(
	() => props.node.state,
	() => {
		clonedState.value = cloneDeep(props.node.state);
	},
	{ deep: true }
);
</script>

<style scoped>
.clamp-text {
	max-height: 2em;
	color: var(--gray-700);
	display: -webkit-box;
	-webkit-line-clamp: 3;
	-webkit-box-orient: vertical;
}
</style>
