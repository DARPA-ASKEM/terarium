<template>
	<main style="display: flex; flex-direction: column; width: 90%; height: 90%; overflow: scroll">
		<h2>
			clipboard test
			<Button v-if="model" :disabled="clipboardText === ''" @click="buttonClick()"> Paste </Button>
		</h2>

		<tera-stratified-matrix
			v-if="ready"
			:mmt="mmt"
			:mmt-params="mmtParams"
			:id="'beta'"
			:stratified-matrix-type="StratifiedMatrix.Parameters"
			:should-eval="false"
			:matrix-type="'subjectControllers'"
		/>
	</main>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import { getMMT } from '@/services/model';
import Button from 'primevue/button';
import TeraStratifiedMatrix from '@/components/model/petrinet/model-configurations/tera-stratified-matrix.vue';
import { createParameterMatrix, emptyMiraModel } from '@/model-representation/mira/mira';
import type { Model } from '@/types/Types';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { StratifiedMatrix } from '@/types/Model';
import { updateParameter } from '@/model-representation/service';
import { dsvParse } from '@/utils/dsv';
import { getClipboardText } from '@/utils/clipboard';
import * as sirJSON from '@/examples/strata-example.json';

let timerId = -1;
const clipboardText = ref('');

const model = ref<Model>();
const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});
const ready = ref(false);

const updateByMatrixCSV = (
	model: Model,
	matrixType: 'subjectControllers' | 'outcomeControllers' | 'subjectOutcome',
	text: string
) => {
	const parseResult = dsvParse(text);
	const matrices = createParameterMatrix(mmt.value, mmtParams.value, 'beta');
	const matrix = matrices[matrixType];

	console.log(parseResult.entries);
	console.log(matrix);

	matrix.matrix.forEach((row) => {
		row.forEach((matrixEntry) => {
			// If we have label information, use them as they may be more accurate, otherwise use indices
			if (parseResult.hasColLabels && parseResult.hasRowLabels) {
				const match = parseResult.entries.find((entry) => {
					return entry.rowLabel === matrixEntry.rowCriteria && entry.colLabel === matrixEntry.colCriteria;
				});
				if (match) {
					updateParameter(model, matrixEntry.content.id, 'value', match.value);
				}
			} else {
				const match = parseResult.entries.find((entry) => {
					return entry.rowIdx === matrixEntry.row && entry.colIdx === matrixEntry.col;
				});
				if (match) {
					updateParameter(model, matrixEntry.content.id, 'value', match.value);
				}
			}
		});
	});
};

const csvStringProcessor = async (item: DataTransferItem) => {
	if (item.kind !== 'string') return;
	if (item.type !== 'text/plain') return;

	// Presume this is a full matrix, with row/column labels
	item.getAsString(async (text) => {
		updateByMatrixCSV(model.value as Model, 'subjectControllers', text);

		// Update
		const response = await getMMT(model.value as Model);
		mmt.value = response.mmt;
		mmtParams.value = response.template_params;
	});
};

const buttonClick = async () => {
	updateByMatrixCSV(model.value as Model, 'subjectControllers', clipboardText.value);
	const response = await getMMT(model.value as Model);
	mmt.value = response.mmt;
	mmtParams.value = response.template_params;
};

const processPasteEvent = async (event: ClipboardEvent) => {
	const clipboardData = event.clipboardData;
	if (!clipboardData) return;
	const item = clipboardData.items[0];
	await csvStringProcessor(item);
};

onMounted(async () => {
	model.value = sirJSON as Model;
	const response = await getMMT(model.value);
	mmt.value = response.mmt;
	mmtParams.value = response.template_params;
	ready.value = true;

	document.addEventListener('paste', processPasteEvent);
	timerId = window.setInterval(async () => {
		const x = await getClipboardText();
		if (x !== clipboardText.value) {
			clipboardText.value = x;
		}
	}, 1000);
});

onUnmounted(() => {
	document.removeEventListener('paste', processPasteEvent);
	window.clearInterval(timerId);
});
</script>
