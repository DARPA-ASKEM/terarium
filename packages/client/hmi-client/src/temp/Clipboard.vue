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
import { dsvParse } from '@/utils/dsv';

import * as sirJSON from '@/examples/strata-example.json';

const model = ref<Model>();
const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});
const ready = ref(false);

const updateByMatrixCSV = async (
	model: Model,
	paramLocationMap: Map<string, TemplateSummary[]>,
	matrixType: string,
	text: string
) => {
	const parseResult = dsvParse(text);

	parseResult.entries.forEach((entry) => {
		paramLocationMap.forEach((v, k) => {
			let row: string | null = null;
			let col: string | null = null;

			if (matrixType === 'subjectControllers') {
				row = v[0].subject;
				col = v[0].controllers.join('-');
			} else if (matrixType === 'subjectOutcome') {
				row = v[0].subject;
				col = v[0].outcome;
			} else if (matrixType === 'outcomeControllers') {
				row = v[0].outcome;
				col = v[0].controllers.join('-');
			}

			if (entry.rowLabel === row && entry.colLabel === col) {
				updateParameter(model, k, 'value', entry.value);
			}
		});
	});
};

const createParamLocationMap = (mmtParams: MiraTemplateParams) => {
	const paramLocationMap = new Map<string, TemplateSummary[]>();
	const templateParams = Object.values(mmtParams);
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
	return paramLocationMap;
};

const csvStringProcessor = async (item: DataTransferItem) => {
	if (item.kind !== 'string') return;
	if (item.type !== 'text/plain') return;

	// Presume this is a full matrix, with row/column labels
	item.getAsString(async (text) => {
		const paramLocationMap = createParamLocationMap(mmtParams.value);
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
