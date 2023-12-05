<template>
	<main>
		<template v-if="code">{{ code?.name }}</template>
		<template v-else>
			<Dropdown
				class="w-full"
				:options="codeAssets"
				option-label="name"
				v-model="code"
				placeholder="Select code asset"
			/>
			<tera-operator-placeholder-graphic :operation-type="node.operationType" />
		</template>
	</main>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { WorkflowNode } from '@/types/workflow';
import { onMounted, ref, computed, watch } from 'vue';
import { Code } from '@/types/Types';
import { useProjects } from '@/composables/project';
import Dropdown from 'primevue/dropdown';
import TeraOperatorPlaceholderGraphic from '@/workflow/operator/tera-operator-placeholder-graphic.vue';
import { CodeAssetState } from './code-asset-operation';

const props = defineProps<{
	node: WorkflowNode<CodeAssetState>;
}>();

const emit = defineEmits(['update-state', 'append-output-port']);

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

			if (_.isEmpty(props.node.outputs)) {
				emit('append-output-port', {
					type: 'codeAssetId',
					label: code.value.name,
					value: [code.value.id]
				});
			}
		}
	}
);
</script>

<style scoped></style>
