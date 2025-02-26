<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<tera-columnar-panel>
			<tera-drilldown-section :is-loading="isFetchingPDF">
				<div v-if="props.node.state?.taskProgress">
					Processing...
					<ProgressBar :value="props.node?.state?.taskProgress * 100" />
				</div>
				<tera-pdf-embed v-if="pdfLink" :pdf-link="pdfLink" :title="document?.name || ''" />
				<tera-text-editor v-else-if="docText" :initial-text="docText" />
			</tera-drilldown-section>
		</tera-columnar-panel>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import ProgressBar from 'primevue/progressbar';

import type { DocumentAsset } from '@/types/Types';
import { WorkflowNode } from '@/types/workflow';

import { downloadDocumentAsset, getDocumentAsset, getDocumentFileAsText } from '@/services/document-assets';

import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraTextEditor from '@/components/documents/tera-text-editor.vue';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraPdfEmbed from '@/components/widgets/tera-pdf-embed.vue';

import { DocumentOperationState } from './document-operation';

const emit = defineEmits(['close', 'update-state', 'append-output']);
const props = defineProps<{
	node: WorkflowNode<DocumentOperationState>;
}>();

const document = ref<DocumentAsset | null>();
const pdfLink = ref<string | null>();
const docText = ref<string | null>();
const isFetchingPDF = ref(false);

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
		isFetchingPDF.value = false;
	}
});
</script>
