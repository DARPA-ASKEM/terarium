<template>
	<div class="wf-annotation" v-if="!isEditing" @dblclick.stop="edit">
		{{ annotation.content }}
	</div>
	<div style="display: flex; flex-direction: column" v-else>
		<textarea v-model="annotationRef.content" rows="4" />
		<div style="display: flex; justify-content: space-between">
			<div>
				<Button label="Ok" @click="update()" size="small" outlined class="white-space-nowrap" severity="secondary" />
				<Button label="Cancel" @click="reset()" size="small" outlined class="white-space-nowrap" severity="secondary" />
			</div>
			<Button
				style="right: 0"
				label="Delete"
				@click="remove()"
				size="small"
				outlined
				class="white-space-nowrap"
				severity="danger"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { WorkflowAnnotation } from '@/types/workflow';
import { ref } from 'vue';

const props = defineProps<{
	annotation: WorkflowAnnotation;
}>();

const annotationRef = ref<WorkflowAnnotation>(props.annotation);
const isEditing = ref(false);
const defaultContent = props.annotation.content;

const emit = defineEmits(['update-annotation', 'remove-annotation']);

const reset = () => {
	isEditing.value = false;
	annotationRef.value.content = defaultContent;
};

const edit = () => {
	isEditing.value = !isEditing.value;
};

const update = () => {
	emit('update-annotation', annotationRef.value);
	isEditing.value = false;
};

const remove = () => {
	emit('remove-annotation', annotationRef.value.id);
	isEditing.value = false;
};
</script>

<style>
.workflow-annotation {
	max-width: 150px;
	padding: 4px;
}
.workflowannotation:hover {
	background: var(--surface-warning);
}
</style>
