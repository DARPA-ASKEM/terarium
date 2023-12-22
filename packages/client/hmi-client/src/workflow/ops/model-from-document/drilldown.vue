<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<div>
			<tera-drilldown-section>
				<tera-asset-block> </tera-asset-block>
			</tera-drilldown-section>
		</div>
		<template #preview>
			<tera-drilldown-preview> </tera-drilldown-preview>
		</template>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';
import TeraDrilldownPreview from '@/components/drilldown/tera-drilldown-preview.vue';
import TeraAssetBlock from '@/components/widgets/tera-asset-block.vue';
import { onMounted, ref } from 'vue';
import { getDocumentAsset, getEquationFromImageUrl } from '@/services/document-assets';
import { DocumentAsset, ExtractionAssetType } from '@/types/Types';
import { ModelFromDocumentState } from './model-from-document-operation';

const emit = defineEmits(['close']);
const props = defineProps<{
	node: WorkflowNode<ModelFromDocumentState>;
}>();

const document = ref<DocumentAsset | null>();

onMounted(async () => {
	const documentId = props.node.inputs?.[0]?.value?.[0];
	if (documentId) {
		document.value = await getDocumentAsset(documentId);
		const equations = document.value?.assets?.filter(
			(a) => a.assetType === ExtractionAssetType.Equation
		);

		const promises = equations?.map(async (e) => {
			const res = await getEquationFromImageUrl(documentId, e.fileName);
			return res;
		});

		if (!promises) return;

		const response = Promise.all(promises);
		console.log(response);
	}
});
</script>

<style scoped></style>
