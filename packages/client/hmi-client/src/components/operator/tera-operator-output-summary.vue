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
				@keydown.enter.prevent="updateSummaryText"
				@keydown.esc.prevent="updateSummaryText"
			/>
			<div class="btn-group">
				<Button icon="pi pi-times" rounded text @click="cancelEdit" />
				<Button icon="pi pi-check" rounded text @click="updateSummaryText" />
			</div>
		</template>
		<div v-else-if="isLoading === false" class="summary">
			<p else @click="isEditing = true">{{ summaryText }}<span class="pi pi-pencil ml-2 text-xs edit-button" /></p>
		</div>
		<div v-else-if="isLoading" class="summary">
			<img src="@assets/svg/icons/magic.svg" alt="Magic icon" />
			<p>Generating summary...</p>
		</div>
	</section>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { ref, watch, onUnmounted } from 'vue';
import { Poller, PollerState } from '@/api/api';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
import { updateSummary, getSummaries } from '@/services/summary-service';
import { Summary } from '@/types/Types';

const MAX_LENGTH = 400;

const props = defineProps({
	summaryId: {
		type: String,
		required: true
	}
});

const summaryText = ref('');
const summary = ref<Summary | null>(null);
const isEditing = ref(false);
const isLoading = ref(true);

function updateSummaryText() {
	if (!summary.value || isEmpty(summary.value)) return;
	summary.value.humanSummary = summaryText.value;
	updateSummary(summary.value);
	isEditing.value = false;
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

/**
 * It can take some time (30-40 seconds) for generated summary to appear
 * */
const poller = new Poller<Summary>();
async function pollSummary() {
	isLoading.value = true;
	poller.setThreshold(15).setPollAction(async () => {
		const summaryMap = await getSummaries([props.summaryId]);
		const summaryObj = summaryMap[props.summaryId];
		if (summaryObj && summaryObj.generatedSummary) {
			return { data: summaryObj, progress: null, error: null };
		}
		return { data: null, progress: null, error: null };
	});
	const pollerResult = await poller.start();
	if (pollerResult.state === PollerState.Cancelled) {
		return;
	}

	summary.value = pollerResult.data;
	if (summary.value) {
		summaryText.value = getSummaryText(summary.value);
	}
	isLoading.value = false;
}

onUnmounted(() => {
	poller.stop();
});

watch(
	() => props.summaryId,
	async (newId, oldId) => {
		if (!newId || newId === oldId) return;
		pollSummary();
		/*
		const summaryMap = await getSummaries([props.summaryId]);
		summary.value = summaryMap[props.summaryId];
		if (summary.value) {
			summaryText.value = getSummaryText(summary.value);
		}
		*/
	},
	{ immediate: true }
);
</script>

<style scoped>
section {
	display: flex;
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
		color: var(--text-color-primary);
		cursor: text;

		img {
			padding-right: var(--gap-2);
			width: 21px;
		}
	}

	& > .p-inputtext {
		font-size: var(--font-caption);
	}

	padding-bottom: var(--gap-2);
	border-radius: var(--border-radius);
	gap: var(--gap-2);

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

	.edit-button {
		color: var(--text-color-secondary);
		cursor: pointer;
	}
}
</style>
