<template>
	<!-- Standard view -->
	<div
		v-if="!isEditing"
		class="wf-annotation"
		:style="{ fontSize: annotationRef.textSize + 'px' }"
		@dblclick.stop="edit"
		v-html="formattedContent"
	></div>
	<!-- Editing view -->
	<div v-else class="wf-annotation-editing">
		<textarea
			v-model="annotationRef.content"
			:rows="calculateRows()"
			class="text-area"
			:style="{ fontSize: annotationRef.textSize + 'px' }"
			@input="autoResize"
			ref="textarea"
			@focus="autoResize"
			@mousedown.stop
			@mousemove.stop
		/>
		<div class="floating-toolbar" @mousedown.stop @mousemove.stop>
			<!-- Trash -->
			<Button
				style="right: 0"
				icon="pi pi-trash"
				@click="remove()"
				outlined
				class="white-space-nowrap"
				severity="secondary"
				rounded
			/>
			<!-- Font size -->
			<div class="flex gap-2 align-items-center">
				<span class="text-sm">{{ annotationRef.textSize }}px</span>
				<Button
					icon="pi pi-minus"
					@click="adjustFontSize(-1)"
					size="small"
					outlined
					class="white-space-nowrap"
					severity="secondary"
					rounded
				/>
				<Button
					icon="pi pi-plus"
					@click="adjustFontSize(1)"
					size="small"
					outlined
					class="white-space-nowrap"
					severity="secondary"
					rounded
				/>
			</div>
			<!-- Save and cancel -->
			<div class="flex gap-2">
				<Button
					icon="pi pi-times"
					@click="reset()"
					size="small"
					outlined
					class="white-space-nowrap"
					severity="secondary"
					rounded
				/>
				<Button
					icon="pi pi-check"
					@click="update()"
					size="small"
					class="white-space-nowrap text-white"
					severity="primary"
					rounded
				/>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { WorkflowAnnotation } from '@/types/workflow';
import { ref, nextTick, computed } from 'vue';

const props = defineProps<{
	annotation: WorkflowAnnotation;
}>();

const annotationRef = ref<WorkflowAnnotation>(props.annotation);
const isEditing = ref(false);

const emit = defineEmits(['update-annotation', 'remove-annotation']);

const reset = () => {
	isEditing.value = false;
	annotationRef.value.content = annotationContentBackup.value || defaultPlaceholder;
};

const defaultPlaceholder = 'Double click to edit...';
const annotationContentBackup = ref<string | null>(null);

const edit = () => {
	// Backup current content to handle cancelation
	annotationContentBackup.value = annotationRef.value.content;

	if (annotationRef.value.content === defaultPlaceholder) {
		annotationRef.value.content = '';
	}

	isEditing.value = true;
	autoResize();
	nextTick(() => {
		textarea.value?.focus();
	});
};

const update = () => {
	/* Reset to default placeholder if content is empty */
	if (!annotationRef.value.content.trim()) {
		annotationRef.value.content = defaultPlaceholder;
	}

	emit('update-annotation', annotationRef.value);
	isEditing.value = false;
};

const remove = () => {
	emit('remove-annotation', annotationRef.value.id);
	isEditing.value = false;
};

const adjustFontSize = (delta: number) => {
	const newSize = Math.max(8, Math.min(32, (annotationRef.value.textSize || 14) + delta));
	annotationRef.value.textSize = newSize;
	autoResize();
};

const textarea = ref<HTMLTextAreaElement | null>(null);

const calculateRows = () => {
	const charsPerRow = Math.floor(35 * (12 / (annotationRef.value.textSize || 12)));
	return Math.max(1, Math.ceil(annotationRef.value.content.length / charsPerRow));
};

const autoResize = () => {
	const element = textarea.value;
	if (element) {
		element.style.height = 'auto';
		element.style.height = `${element.scrollHeight}px`;
	}
};

/* display newline characters as line breaks */
const formattedContent = computed(() => annotationRef.value.content.replace(/\n/g, '<br>'));
</script>

<style scoped>
/* Default styles */
.wf-annotation {
	cursor: pointer;
	padding: var(--gap-3) var(--gap-4);
	border: 2px dashed transparent;
	border-radius: var(--border-radius);
	position: relative;
	font-family: var(--font-family);
	background: transparent;
}

.wf-annotation:hover {
	border-color: var(--primary-color);
	background: color-mix(in srgb, var(--surface-0) 50%, var(--surface-highlight) 50%);
}

/* Editing styles */
.wf-annotation-editing {
	position: relative;
}
.wf-annotation-editing .text-area {
	width: 100%;
	padding: 10px 14px;
	min-height: 40px;
	border: 2px solid var(--primary-color);
	border-radius: var(--border-radius);
	font-family: var(--font-family);
}

/* Floating toolbar */
@keyframes fadeInUp {
	0% {
		opacity: 0;
		transform: translateY(10px);
	}
	100% {
		opacity: 1;
		transform: translateY(0);
	}
}

.wf-annotation-editing .floating-toolbar {
	position: absolute;
	background: var(--surface-highlight);
	padding: var(--gap-2) var(--gap-4);
	border-radius: 4rem;
	border: 1px solid var(--surface-border-light);
	top: -4rem;
	width: 100%;
	display: flex;
	gap: var(--gap-2);
	justify-content: space-between;

	/* Animation */
	animation: fadeInUp 0.3s ease forwards;
	opacity: 0;
}
</style>
