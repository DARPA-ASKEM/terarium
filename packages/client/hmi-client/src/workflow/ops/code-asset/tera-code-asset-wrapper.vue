<template>
	<tera-drilldown :title="node.displayName" @on-close-clicked="emit('close')">
		<section>
			<tera-code :asset-id="props.node?.state?.codeAssetId ?? ''" is-preview />
		</section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import TeraCode from '@/components/code/tera-code.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { CodeAssetState } from './code-asset-operation';

const props = defineProps<{
	node: WorkflowNode<CodeAssetState>;
}>();

const emit = defineEmits(['close']);
</script>

<style scoped></style>
