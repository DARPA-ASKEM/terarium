<template>
	<section :class="{ 'is-editing': isEditing }">
		<template v-if="isEditing">
			<Textarea
				v-focus
				v-model="summaryText"
				:maxlength="MAX_LENGTH"
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
		<div v-else class="summary">
			<p else @click="isEditing = true">
				{{ summaryText }}<span class="pi pi-pencil ml-2 text-xs" />
			</p>
		</div>
		<!--
		<div v-else-if="!isNil(activeOutput?.summary)" class="summary">
			<img v-if="isGenerating || isGenerated" src="@assets/svg/icons/magic.svg" alt="Magic icon" />
			<p v-if="isGenerating">Generating AI summary...</p>
			<p v-else @click="isEditing = true">
				{{ summary }}<span class="pi pi-pencil ml-2 text-xs" />
			</p>
		</div>
		--></section>
</template>

<script setup lang="ts">
// import { isNil } from 'lodash';
import { ref, onMounted } from 'vue';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
import { getSummaries } from '@/services/summary-service';
import { Summary } from '@/types/Types';

const MAX_LENGTH = 400;

const props = defineProps({
	summaryId: {
		type: String,
		required: true
	}
});

// const emit = defineEmits(['update-output-port', 'generate-output-summary']);

const summaryText = ref('');
const summary = ref<Summary | null>(null);

const isEditing = ref(false);

/*
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
*/

function updateSummary() {
	console.log('hihi');
}

function getSummaryText(s: Summary) {
	if (s.humanSummary) {
		return s.humanSummary;
	}
	return s.generatedSummary || '';
}

function cancelEdit() {
	isEditing.value = false;
	if (summary.value) {
		summaryText.value = getSummaryText(summary.value);
	}
}

onMounted(async () => {
	const summaryMap = await getSummaries([props.summaryId]);
	summary.value = summaryMap[props.summaryId];
	if (summary.value) {
		summaryText.value = getSummaryText(summary.value);
	}
});
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
