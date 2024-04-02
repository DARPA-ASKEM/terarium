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
				@keydown.esc.prevent="saveAnnotation"
			/>
			<div class="btn-group">
				<Button icon="pi pi-trash" rounded text @click="deleteAnnotation" />
				<Button icon="pi pi-check" rounded text @click="saveAnnotation" />
			</div>
		</template>
		<div v-else-if="!isEmpty(annotation)" class="annotation">
			<p @click="isEditing = true">{{ annotation }}<span class="pi pi-pencil ml-2 text-xs" /></p>
		</div>
	</section>
	{{ annotation }}
	<Button
		v-if="!inNode && isEmpty(annotation) && !isEditing"
		class="add-a-note mr-auto"
		label="Add a note"
		icon="pi pi-pencil"
		size="small"
		text
		@click="isEditing = true"
	/>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, watch, PropType, onMounted } from 'vue';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
import { workflowEventBus } from '@/services/workflow';

const props = defineProps({
	inNode: {
		type: Boolean,
		default: false
	},
	state: {
		type: Object as PropType<any> | null,
		default: null
	}
});

// const emit = defineEmits(['update-state']);

const annotation = ref('');
const isEditing = ref(false);
defineExpose({ isEditing });

function saveAnnotation() {
	workflowEventBus.emit('update-annotation', annotation.value);
	isEditing.value = false;
}

function deleteAnnotation() {
	annotation.value = '';
	saveAnnotation();
}

onMounted(() => {
	workflowEventBus.on('pass-operator', ({ operatorAnnotation }) => {
		if (props.state) return;
		console.log('pass-annotation', operatorAnnotation);
		annotation.value = operatorAnnotation;
		console.log(annotation.value);
	});
});

watch(
	() => annotation.value,
	() => {
		console.log(annotation.value);
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
		font-size: var(--font-caption);
		color: var(--text-color-subdued);
		cursor: text;
	}

	& > .p-inputtext {
		font-size: var(--font-caption);
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
		padding-bottom: var(--gap-small);
		padding-left: var(--gap-small);
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
			padding: 0;
		}
	}
}
</style>
