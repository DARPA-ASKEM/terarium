<template>
	<main>
		<template v-if="code">
			<h6>{{ code?.name }}</h6>
			<Button label="Open" @click="emit('open-drilldown')" severity="secondary" outlined />
		</template>
		<template v-else>
			<Dropdown
				class="w-full p-dropdown-sm"
				:options="codeAssets"
				option-label="assetName"
				@update:model-value="onCodeChange"
				placeholder="Select a code asset"
			/>
			<tera-operator-placeholder :node="node" />
		</template>
	</main>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import _ from 'lodash';
import { WorkflowNode } from '@/types/workflow';
import { getCodeAsset } from '@/services/code';
import { AssetType } from '@/types/Types';
import type { Code, ProjectAsset } from '@/types/Types';
import { useProjects } from '@/composables/project';
import Dropdown from 'primevue/dropdown';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import Button from 'primevue/button';
import { getCodeBlocks } from '@/utils/code-asset';
import { canPropagateResource } from '@/services/workflow';
import { CodeAssetState } from './code-asset-operation';

const props = defineProps<{
	node: WorkflowNode<CodeAssetState>;
}>();

const emit = defineEmits(['update-state', 'append-output', 'open-drilldown']);

const code = ref<Code | null>(null);
const codeAssets = computed(() => useProjects().getActiveProjectAssets(AssetType.Code));

async function getCodeById(codeAssetId: string) {
	code.value = await getCodeAsset(codeAssetId);

	if (code.value?.id) {
		const state = _.cloneDeep(props.node.state);
		state.codeAssetId = code.value.id;
		emit('update-state', state);

		const outputs = props.node.outputs;
		if (canPropagateResource(outputs)) {
			const blocks = await getCodeBlocks(code.value);
			emit('append-output', {
				type: 'codeAssetId',
				label: `${code.value.name} code blocks (${blocks.length})`,
				value: [code.value.id]
			});
		}
	}
}

async function onCodeChange(chosenCode: ProjectAsset) {
	await getCodeById(chosenCode.assetId);
}

onMounted(async () => {
	const state = props.node.state;
	if (state.codeAssetId) {
		await getCodeById(state.codeAssetId);
	}
});
</script>

<style scoped></style>
