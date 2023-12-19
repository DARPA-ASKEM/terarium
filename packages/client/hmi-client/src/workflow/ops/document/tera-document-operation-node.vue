<template>
	<main>
		<template v-if="document">
			<div>{{ document?.name }}</div>
			<Button label="Open document" @click="emit('open-drilldown')" severity="secondary" outlined />
		</template>
		<template v-else>
			<Dropdown
				class="w-full p-dropdown-sm"
				:options="documents"
				option-label="name"
				v-model="document"
				placeholder="Select document"
			/>
			<tera-operator-placeholder :operation-type="node.operationType" />
		</template>
	</main>
</template>

<script setup lang="ts">
import TeraOperatorPlaceholder from '@/components/operator/tera-operator-placeholder.vue';
import { WorkflowNode } from '@/types/workflow';
import Button from 'primevue/button';
import Dropdown from 'primevue/dropdown';
import { DocumentAsset } from '@/types/Types';
import { computed, onMounted, ref, watch } from 'vue';
import { useProjects } from '@/composables/project';
import { cloneDeep, isEmpty } from 'lodash';
import { getDocumentAsset } from '@/services/document-assets';
import { DocumentOperationState } from './document-operation';

const document = ref<DocumentAsset | null>(null);

const emit = defineEmits(['open-drilldown', 'update-state', 'append-output-port']);
const props = defineProps<{
	node: WorkflowNode<DocumentOperationState>;
}>();
const documents = computed<DocumentAsset[]>(
	() => useProjects().activeProject.value?.assets?.documents ?? []
);

onMounted(async () => {
	if (props.node.state.documentId) {
		document.value = await getDocumentAsset(props.node.state.documentId);
	}
});

watch(
	() => document.value,
	async () => {
		if (document.value?.id) {
			const state = cloneDeep(props.node.state);
			state.documentId = document.value.id;
			emit('update-state', state);

			if (isEmpty(props.node.outputs)) {
				emit('append-output-port', {
					type: 'documentId',
					label: `${document.value.name}`,
					value: [document.value.id]
				});
			}
		}
	},
	{ immediate: true }
);
</script>

<style scoped></style>
