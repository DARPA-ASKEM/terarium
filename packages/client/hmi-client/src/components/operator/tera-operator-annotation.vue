<template>
	<section :class="{ 'in-node': inNode, 'is-editing': isEditing }">
		<template v-if="isEditing">
			<Textarea
				v-focus
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
		<div v-else-if="!isEmpty(annotation)" class="annotation">
			<p>{{ annotation }}</p>
			<Button icon="pi pi-pencil" rounded text @click="isEditing = true" />
		</div>
	</section>
	<Button
		v-if="!inNode && isEmpty(annotation) && !isEditing"
		class="add-a-note"
		label="Add a note"
		icon="pi pi-pencil"
		size="small"
		text
		@click="isEditing = true"
	/>
</template>

<script setup lang="ts">
import { isEmpty, cloneDeep } from 'lodash';
import { ref, watch, PropType } from 'vue';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';

const props = defineProps({
	inNode: {
		type: Boolean,
		default: false
	},
	state: {
		type: Object as PropType<any>,
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
	flex: 1;
	&:empty {
		display: none;
	}

	& > .btn-group {
		display: flex;
		justify-content: end;
		& > :last-child:deep(.p-button-icon) {
			color: var(--primary-color);
		}
	}

	& > .annotation {
		display: flex;
		justify-content: space-between;
		width: 100%;
	}

	&.in-node {
		flex-direction: column;
		& > .annotation {
			flex-direction: column;
			& > p + .p-button {
				align-self: end;
			}
		}
	}

	/* In drilldown */
	&:not(.in-node) {
		background-color: var(--surface-section);
		padding: var(--gap-xsmall) var(--gap-small) var(--gap-xsmall) var(--gap);
		border-radius: var(--border-radius);
		gap: var(--gap-small);
		& > textarea {
			flex: 1;
			align-self: center;
		}
		& > .btn-group {
			align-self: start;
		}
		& p {
			align-self: center;
			& + .p-button {
				padding: 0 1rem;
			}
		}

		&.is-editing {
			padding: var(--gap-xsmall) var(--gap-small);
		}
	}
}
</style>
