<template>
	<section :class="{ 'in-drilldown': inDrilldown }">
		<template v-if="isEditing">
			<Textarea
				v-model="annotation"
				placeholder="Add a note..."
				autoResize
				rows="1"
				@click.stop
				@keydown.enter.prevent="saveAnnotation"
			/>
			<div class="btn-group">
				<Button icon="pi pi-trash" rounded text @click="deleteAnnotation" />
				<Button icon="pi pi-check" rounded text @click="saveAnnotation" />
			</div>
		</template>
		<template v-else-if="!isEmpty(annotation)">
			<div class="annotation">
				<p>{{ annotation }}</p>
				<Button icon="pi pi-pencil" rounded text @click="isEditing = true" />
			</div>
		</template>
	</section>
	<Button
		v-if="inDrilldown && isEmpty(annotation) && !isEditing"
		class="add-a-note"
		label="Add a note"
		icon="pi pi-pencil"
		text
		@click="isEditing = true"
	/>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import { ref, watch } from 'vue';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';

const props = defineProps({
	inDrilldown: {
		type: Boolean,
		default: false
	},
	state: {
		type: Object as PropType<S>,
		required: true
	}
});

const emit = defineEmits(['update-state']);

const annotation = ref(props.state.annotation ?? '');
const isEditing = ref(false);
defineExpose({ isEditing });

function saveAnnotation() {
	const state = cloneDeep(props.state);
	if (state.annotation && isEmpty(annotation.value)) delete state.annotation;
	else state.annotation = annotation.value;
	emit('update-state', state);
	isEditing.value = false;
}

function deleteAnnotation() {
	annotation.value = '';
	saveAnnotation();
}

// Syncs the annotation with the one in node state
// This helps the annotation in the operator and drilldown to stay in sync
watch(
	() => props.state.annotation,
	() => {
		annotation.value = props.state.annotation;
	}
);
</script>

<style scoped>
section {
	display: flex;
	&:empty {
		display: none;
	}

	& .btn-group {
		display: flex;
		justify-content: end;
		& > :last-child:deep(.p-button-icon) {
			color: var(--primary-color);
		}
	}
	& .annotation {
		display: flex;
	}
	& .add-a-note {
		margin-left: auto;
		&:deep(.p-button-icon) {
			color: var(--primary-color);
		}
	}

	&:not(.in-drilldown) {
		flex-direction: column;
		& .annotation {
			flex-direction: column;
			& > p + .p-button {
				margin-left: auto;
			}
		}
	}

	&.in-drilldown {
		flex: 1;
		background-color: var(--surface-section);
		padding: var(--gap-small);
		border-radius: var(--border-radius);
		gap: var(--gap-small);
		& textarea {
			flex: 1;
			align-self: center;
		}
		& .btn-group {
			background-color: var(--surface-section);
			border-radius: var(--border-radius);
			align-self: start;
		}
		& .annotation {
			align-items: start;
			justify-content: space-between;
			width: 100%;

			& p {
				align-self: center;
			}
			& > p + .p-button {
				padding: 0 1rem;
			}
		}
	}
}
</style>
