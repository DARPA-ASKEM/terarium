<template>
	<tera-drilldown
		:node="node"
		@on-close-clicked="emit('close')"
		@update-state="(state: any) => emit('update-state', state)"
	>
		<tera-drilldown-section>
			<tera-code :asset-id="node.state?.codeAssetId ?? ''" is-preview @apply-changes="onApplyChanges" />
		</tera-drilldown-section>
	</tera-drilldown>
</template>

<script setup lang="ts">
import { WorkflowNode } from '@/types/workflow';
import TeraCode from '@/components/code/tera-code.vue';
import TeraDrilldown from '@/components/drilldown/tera-drilldown.vue';
import { cloneDeep } from 'lodash';
import type { Code } from '@/types/Types';
import { getCodeBlocks } from '@/utils/code-asset';
import TeraDrilldownSection from '@/components/drilldown/tera-drilldown-section.vue';

import { CodeAssetState } from './code-asset-operation';

const props = defineProps<{
	node: WorkflowNode<CodeAssetState>;
}>();

const emit = defineEmits(['close', 'update-output-port', 'update-state']);

async function onApplyChanges(code: Code) {
	const outputPort = cloneDeep(props.node.outputs[0]);
	if (!code || !outputPort) return;
	const blocks = await getCodeBlocks(code);
	outputPort.label = `${code.name} code blocks (${blocks.length})`;
	emit('update-output-port', outputPort);
}
</script>

<style scoped></style>
