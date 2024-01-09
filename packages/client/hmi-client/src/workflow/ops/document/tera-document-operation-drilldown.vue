<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div>
			<tera-drilldown-section :is-loading="isFetchingPDF">
				<tera-pdf-embed v-if="pdfLink" :pdf-link="pdfLink" :title="document?.name || ''" />
				<tera-text-editor v-else-if="docText" :initial-text="docText" />
			</tera-drilldown-section>
			<tera-drilldown-preview hide-header>
				<h5>{{ document?.name }}</h5>

				<tera-extractions :document="document" :state="clonedState" @update="onUpdateAsset" />
				<template #footer?>
					<Button label="Close" @click="emit('close')"></Button>
				</template>
			</tera-drilldown-preview>
		</div>
	</tera-drilldown>
</template>

<script setup lang="ts">
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';
import { onMounted, ref, watch } from 'vue';
import { DocumentAsset } from '@/types/Types';
import {
	downloadDocumentAsset,
	getDocumentAsset,
	getDocumentFileAsText
} from '@/services/document-assets';
import { cloneDeep } from 'lodash';
import TeraTextEditor from '@/components/documents/tera-text-editor.vue';
import TeraExtractions from '@/components/documents/tera-extractions.vue';
import { DocumentOperationPortType, DocumentOperationState } from './document-operation';

const emit = defineEmits(['close', 'update-state', 'update-output-port']);
const props = defineProps<{
	node: WorkflowNode<DocumentOperationState>;
}>();

const document = ref<DocumentAsset | null>();
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
		if (document.value?.id && filename)
			if (isPdf) {
				pdfLink.value = await downloadDocumentAsset(document.value.id, filename);
			} else {
				docText.value = await getDocumentFileAsText(document.value.id, filename);
			}
		isFetchingPDF.value = false;
	}
});

function onUpdateAsset(state: DocumentOperationState) {
	emit('update-state', state);
	const outputPorts = props.node.outputs?.filter((port) =>
		[
			DocumentOperationPortType.EQUATION,
			DocumentOperationPortType.TABLE,
			DocumentOperationPortType.FIGURE
		].includes(port.type as DocumentOperationPortType)
	);

	outputPorts.forEach((port) => {
		const outputPort = cloneDeep(port);
		if (!outputPort) return;
		const selected = state[outputPort.type]?.filter((a) => a.includeInProcess) ?? [];
		outputPort.label = `${outputPort.type} (${selected?.length}/${state[outputPort.type]?.length})`;
		outputPort.value = [
			{
				documentId: outputPort.value?.[0]?.documentId,
				[outputPort.type]: state[outputPort.type]
			}
		];
		emit('update-output-port', outputPort);
	});
}

watch(
	() => props.node.state,
	() => {
		clonedState.value = cloneDeep(props.node.state);
	},
	{ deep: true }
);
</script>

<style scoped></style>
