<template>
	<section :class="{ 'is-editing': isEditing }">
		<template v-if="isEditing">
			<Textarea
				v-focus
				v-model="summary"
				:maxlength="MAX_LENGTH"
				placeholder="Add a note..."
				autoResize
				rows="1"
				@click.stop
				@keydown.enter.prevent="updateSummary"
				@keydown.esc.prevent="updateSummary"
			/>
			<div class="btn-group">
				<Button icon="pi pi-times" rounded text @click="cancelEdit" />
				<Button icon="pi pi-check" rounded text @click="updateSummary" />
			</div>
		</template>
		<div v-else-if="!isNil(activeOutput?.summary)" class="summary">
			<img v-if="isGenerating || isGenerated" src="@assets/svg/icons/magic.svg" alt="Magic icon" />
			<p v-if="isGenerating">Generating AI summary...</p>
			<p v-else-if="!isRemoved" @click="isEditing = true">
				{{ summary }}<span class="pi pi-pencil ml-2 text-xs" />
			</p>
		</div>
	</section>
</template>

<script setup lang="ts">
import { cloneDeep, isNil } from 'lodash';
import { ref, watch, PropType, computed } from 'vue';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
import { getActiveOutput } from '@/services/workflow';
import { WorkflowNode } from '@/types/workflow';

const MAX_LENGTH = 400;

const props = defineProps({
	node: {
		type: Object as PropType<WorkflowNode<any>>,
		required: true
	}
});

const activeOutput = computed(() => getActiveOutput(props.node));

const emit = defineEmits(['update-output-port', 'generate-output-summary']);

const summary = ref(activeOutput.value?.summary);

const isEditing = ref(false);

function updateSummary() {
	const updated = cloneDeep(activeOutput.value);
	if (!updated) return;
	if (updated.summary === summary.value) {
		isEditing.value = false;
		return;
	}
	updated.summaryHasBeenEdited = true;
	updated.summary = summary.value;
	emit('update-output-port', updated);
	isEditing.value = false;
}

function cancelEdit() {
	isEditing.value = false;
	summary.value = activeOutput.value?.summary;
}

const isGenerating = computed(
	() => activeOutput?.value?.summaryHasBeenEdited !== true && activeOutput?.value?.summary === ''
);
const isGenerated = computed(
	() =>
		activeOutput?.value?.summaryHasBeenEdited !== true &&
		(activeOutput?.value?.summary?.length ?? 0) > 0
);
const isRemoved = computed(
	() => activeOutput?.value?.summaryHasBeenEdited === true && activeOutput?.value?.summary === ''
);

watch(
	() => activeOutput.value,
	() => {
		if (isGenerating.value) {
			emit('generate-output-summary', activeOutput.value);
		} else {
			summary.value = activeOutput.value?.summary;
		}
	},
	{ immediate: true, deep: true }
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
		color: var(--primary-color);
	}

	& > .summary {
		display: flex;
		width: 100%;
		font-size: var(--font-caption);
		color: var(--text-color-primary);
		cursor: text;

		img {
			padding-right: var(--gap-small);
			width: 21px;
		}
	}

	& > .p-inputtext {
		font-size: var(--font-caption);
	}

	padding-bottom: var(--gap-small);
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
</style>
