<template>
	<main>
		<template v-if="code">
			<div>{{ code?.name }}</div>
			<Button
				label="Open code editor"
				@click="emit('open-drilldown')"
				severity="secondary"
				outlined
			/>
		</template>
		<template v-else>
			<Dropdown
				class="w-full p-dropdown-sm"
				:options="codeAssets"
				option-label="name"
				v-model="code"
				placeholder="Select code asset"
			/>
			<tera-operator-placeholder :operation-type="node.operationType" />
		</template>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { WorkflowNode } from '@/types/workflow';
import { getCodeAsset } from '@/services/code';
import { onMounted, ref, computed, watch } from 'vue';
import { Code } from '@/types/Types';
import { useProjects } from '@/composables/project';
import Dropdown from 'primevue/dropdown';
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import Button from 'primevue/button';
import { getCodeBlocks } from '@/utils/code-asset';
import { CodeAssetState } from './code-asset-operation';

const props = defineProps<{
	node: WorkflowNode<CodeAssetState>;
}>();

const emit = defineEmits(['update-state', 'append-output-port', 'open-drilldown']);

const code = ref<Code | null>(null);
const codeAssets = computed<Code[]>(() => useProjects().activeProject.value?.assets?.code ?? []);

onMounted(async () => {
	if (props.node.state.codeAssetId) {
		code.value = await getCodeAsset(props.node.state.codeAssetId);
	}
});

watch(
	() => code.value,
	async () => {
		if (code.value?.id) {
			const state = _.cloneDeep(props.node.state);
			state.codeAssetId = code.value.id;
			emit('update-state', state);

			if (_.isEmpty(props.node.outputs)) {
				const blocks = await getCodeBlocks(code.value);

				if (blocks.length > 0) {
					emit('append-output-port', {
						type: 'codeAssetId',
						label: `${code.value.name} code blocks (${blocks.length})`,
						value: [code.value.id]
					});
				}
			}
		}
	}
);
</script>

<style scoped></style>
