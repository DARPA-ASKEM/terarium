<template>
	<main style="width: 90%; height: 90%; overflow: scroll">
		<h4>clipboard test</h4>

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
import TeraStratifiedMatrix from '@/components/model/petrinet/model-configurations/tera-stratified-matrix.vue';
import { emptyMiraModel } from '@/model-representation/mira/mira';
import type { Model } from '@/types/Types';
import type { MiraModel, MiraTemplateParams, TemplateSummary } from '@/model-representation/mira/mira-common';
import { StratifiedMatrix } from '@/types/Model';
import { updateParameter } from '@/model-representation/service';

import * as sirJSON from '@/examples/strata-example.json';

const model = ref<Model>();
const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});
const ready = ref(false);

/**
 * Given a matrix-like CSV (with row and column labels), update the model with specified values.
 *
 * For example, with:
 * ,A,B
 * C,1,2
 * D,3,4
 *
 * Will yield (A, C, 1), (A, D, 3), (B, C, 2) and (B, D, 4) update tuples
 *
 **/
const updateByMatrixCSV = async (
	model: Model,
	paramLocationMap: Map<string, TemplateSummary[]>,
	matrixType: string,
	text: string
) => {
	// Parse
	const rows = text.split('\n');
	const columns = rows[0].split(',');

	for (let i = 1; i < rows.length; i++) {
		const data = rows[i].split(',');
		for (let j = 1; j < data.length; j++) {
			const rowLabel = data[0];
			const colLabel = columns[j];

			paramLocationMap.forEach((v, k) => {
				if (matrixType === 'subjectControllers') {
					if (rowLabel === v[0].subject && colLabel === v[0].controllers.join('-')) {
						updateParameter(model, k, 'value', +data[j]);
					}
				} else if (matrixType === 'subjectOutcome') {
					// TODO
				} else if (matrixType === 'outcomeControllers') {
					// TODO
				}
			});
		}
	}
};

const csvStringProcessor = async (item: DataTransferItem) => {
	if (item.kind !== 'string') return;
	if (item.type !== 'text/plain') return;

	const paramLocationMap = new Map<string, TemplateSummary[]>();
	const templateParams = Object.values(mmtParams.value);
	templateParams.forEach((templateParam) => {
		const params = templateParam.params;
		params.forEach((paramName) => {
			if (!paramLocationMap.has(paramName)) paramLocationMap.set(paramName, []);

			paramLocationMap.get(paramName)?.push({
				name: templateParam.name,
				subject: templateParam.subject,
				outcome: templateParam.outcome,
				controllers: templateParam.controllers
			});
		});
	});

	// Presume this is a full matrix, with row/column labels
	item.getAsString(async (text) => {
		updateByMatrixCSV(model.value as Model, paramLocationMap, 'subjectControllers', text);

		// Update
		const response = await getMMT(model.value as Model);
		mmt.value = response.mmt;
		mmtParams.value = response.template_params;
	});
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
});

onUnmounted(() => {
	document.removeEventListener('paste', processPasteEvent);
});
</script>
