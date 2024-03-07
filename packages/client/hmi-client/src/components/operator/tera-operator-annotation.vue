<template>
	<section>
		<template v-if="isEditing">
			<Textarea
				v-model="annotation"
				placeholder="Add a note..."
				autoResize
				rows="1"
				@keydown.enter.prevent="emit('confirm-annotation', annotation)"
			/>
			<div>
				<Button icon="pi pi-trash" rounded text @click="deleteAnnotation" />
				<Button icon="pi pi-check" rounded text @click="emit('confirm-annotation', annotation)" />
			</div>
		</template>
		<template v-else-if="!isEmpty(annotation)">
			<p>{{ annotation }}</p>
			<Button icon="pi pi-pencil" rounded text @click="emit('show-editor')" />
		</template>
	</section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { isEmpty } from 'lodash';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';

const props = defineProps({
	isEditing: {
		type: Boolean,
		default: false
	},
	savedAnnotation: {
		type: String,
		default: ''
	}
});

const emit = defineEmits(['confirm-annotation', 'show-editor']);

const annotation = ref(props.savedAnnotation);

function deleteAnnotation() {
	annotation.value = '';
	emit('confirm-annotation', annotation.value);
}
</script>

<style scoped>
section {
	display: flex;
	flex-direction: column;
	&:empty {
		display: none;
	}
}

div {
	display: flex;
	justify-content: end;

	& > :last-child:deep(.p-button-icon) {
		color: var(--primary-color);
	}
}

p + .p-button {
	margin-left: auto;
}
</style>
