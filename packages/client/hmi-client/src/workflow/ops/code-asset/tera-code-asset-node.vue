<template>
	<main>code asset node</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { WorkflowNode } from '@/types/workflow';
import { onMounted, ref, computed, watch } from 'vue';
import { Code } from '@/types/Types';
import { useProjects } from '@/composables/project';
import { CodeAssetState } from './code-asset-operation';

const props = defineProps<{
	node: WorkflowNode<CodeAssetState>;
}>();

const emit = defineEmits(['update-state']);

const code = ref<Code | null>(null);
const codeAssets = computed<Code[]>(() => useProjects().activeProject.value?.assets?.code ?? []);

onMounted(async () => {
	if (props.node.state.codeAssetId) {
		code.value = codeAssets.value.find(({ id }) => id === props.node.state.codeAssetId) ?? null;
	}
});

watch(
	() => code.value,
	() => {
		if (code.value?.id) {
			const state = _.cloneDeep(props.node.state);
			state.codeAssetId = code.value.id;
			emit('update-state', state);
		}
	}
);
</script>

<style scoped></style>
