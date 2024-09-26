<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<tera-columnar-panel>
			<tera-drilldown-section :is-loading="isFetchingPDF">
				<tera-pdf-embed v-if="pdfLink" :pdf-link="pdfLink" :title="document?.name || ''" />
				<tera-text-editor v-else-if="docText" :initial-text="docText" />
			</tera-drilldown-section>
			<tera-drilldown-preview hide-header class="pt-3 pl-2 pr-4 pb-3">
				<h5>{{ document?.name }}</h5>
				<Accordion multiple :active-index="[0, 1, 2]">
					<AccordionTab v-if="!isEmpty(clonedState.equations)">
						<template #header>
							<header>Equation images</header>
						</template>
						<tera-asset-block
							v-for="(equation, i) in clonedState.equations"
							:key="i"
							:is-toggleable="false"
							:is-included="equation.includeInProcess"
							@update:is-included="onUpdateInclude(equation)"
							class="mb-2"
						>
							<template #default>
								<tera-math-editor
									v-if="equation.asset?.metadata?.text"
									:latex-equation="equation.asset?.metadata?.text"
									:is-editable="false"
								/>
							</template>
						</tera-asset-block>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(clonedState.figures)">
						<template #header>
							<header>Figure images</header>
						</template>
						<tera-asset-block
							v-for="(figure, i) in clonedState.figures"
							:key="i"
							:is-included="figure.includeInProcess"
							@update:is-included="onUpdateInclude(figure)"
							class="mb-2"
						>
							<template #header>
								<h5>{{ figure.name }}</h5>
							</template>
							<Image id="img" :src="getAssetUrl(figure)" :alt="''" preview />
						</tera-asset-block>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(clonedState.tables)">
						<template #header>
							<header>Table images</header>
						</template>
						<tera-asset-block
							v-for="(table, i) in clonedState.tables"
							:key="i"
							:is-included="table.includeInProcess"
							@update:is-included="onUpdateInclude(table)"
							class="mb-2"
						>
							<template #header>
								<h5>{{ table.name }}</h5>
							</template>
							<Image id="img" :src="getAssetUrl(table)" :alt="''" preview />
						</tera-asset-block>
					</AccordionTab>
					<AccordionTab v-if="!isEmpty(document?.text)" header="Text">
						<tera-show-more-text :text="document?.text ?? ''" :lines="5" />
					</AccordionTab>
				</Accordion>
				<template #footer?>
					<Button label="Close" @click="emit('close')" />
				</template>
			</tera-drilldown-preview>
		</tera-columnar-panel>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { AssetBlock, WorkflowNode, WorkflowOutput } from '@/types/workflow';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import Button from 'primevue/button';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { onMounted, ref, watch } from 'vue';
import { ExtractionAssetType } from '@/types/Types';
import type { DocumentAsset, DocumentExtraction } from '@/types/Types';
import { downloadDocumentAsset, getDocumentAsset, getDocumentFileAsText } from '@/services/document-assets';
import { cloneDeep, isEmpty } from 'lodash';
import TeraAssetBlock from '@/components/widgets/tera-asset-block.vue';
import Image from 'primevue/image';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import TeraTextEditor from '@/components/documents/tera-text-editor.vue';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';
import TeraMathEditor from '@/components/mathml/tera-math-editor.vue';
import { DocumentOperationState } from './document-operation';

const emit = defineEmits(['close', 'update-state', 'append-output']);
const props = defineProps<{
	node: WorkflowNode<DocumentOperationState>;
}>();

const document = ref<DocumentAsset | null>();
const equations = ref<AssetBlock<DocumentExtraction>[]>();
const pdfLink = ref<string | null>();
const docText = ref<string | null>();
const isFetchingPDF = ref(false);
const clonedState = ref(cloneDeep(props.node.state));

onMounted(async () => {
	if (props.node.state.documentId) {
		isFetchingPDF.value = true;
		document.value = await getDocumentAsset(props.node.state.documentId);
		const filename = document.value?.fileNames?.[0];
		const isPdf = document.value?.fileNames?.[0]?.endsWith('.pdf');
		if (document.value?.id && filename) {
			if (isPdf) {
				pdfLink.value = await downloadDocumentAsset(document.value.id, filename);
			} else {
				docText.value = await getDocumentFileAsText(document.value.id, filename);
			}
		}
		if (document.value?.metadata?.equations) {
			equations.value = document.value.metadata.equations
				.filter((pages) => pages.length)
				.flatMap((page) =>
					page.map((equation) => {
						const asset: AssetBlock<DocumentExtraction> = {
							name: 'Equation',
							includeInProcess: false,
							asset: {
								fileName: filename ?? '',
								assetType: ExtractionAssetType.Equation,
								metadata: { text: equation }
							}
						};
						return asset;
					})
				);
			// .map((equation, index) => equation.name = `equation ${index+1}`)
		}
		if (equations.value && equations.value?.length > 0) {
			clonedState.value.equations = equations.value;
		}

		isFetchingPDF.value = false;
	}
});

function onUpdateInclude(asset: AssetBlock<DocumentExtraction>) {
	asset.includeInProcess = !asset.includeInProcess;
	emit('update-state', clonedState.value);

	let outputPort: WorkflowOutput<DocumentOperationState> | null = null;

	const portType = assetTypeToPortType(asset.asset.assetType);
	if (!portType) return;

	outputPort = cloneDeep(props.node.outputs?.find((port) => port.type === 'documentId')) || null;
	if (!outputPort) return;
	outputPort.value = [
		{
			...outputPort.value?.[0],
			[portType]: clonedState.value[portType]
		}
	];

	emit('append-output', outputPort);
}

function assetTypeToPortType(assetType: ExtractionAssetType) {
	switch (assetType) {
		case ExtractionAssetType.Equation:
			return 'equations';
		case ExtractionAssetType.Figure:
			return 'figures';
		case ExtractionAssetType.Table:
			return 'tables';
		default:
			return null;
	}
}
// since AWS links expire we need to use the refetched document image urls to display the images
function getAssetUrl(asset: AssetBlock<DocumentExtraction>): string {
	const foundAsset = document.value?.assets?.find((a) => a.fileName === asset.asset.fileName);
	if (!foundAsset) return '';
	return foundAsset.metadata?.url;
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
.p-panel:deep(.p-panel-footer) {
	display: none;
}
</style>
