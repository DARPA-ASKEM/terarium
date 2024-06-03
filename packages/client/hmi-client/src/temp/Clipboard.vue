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
		/>
	</main>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue';
import { getMMT } from '@/services/model';
import TeraStratifiedMatrix from '@/components/model/petrinet/model-configurations/tera-stratified-matrix.vue';
import { emptyMiraModel } from '@/model-representation/mira/mira';
import type { Model } from '@/types/Types';
import type { MiraModel, MiraTemplateParams } from '@/model-representation/mira/mira-common';
import { StratifiedMatrix } from '@/types/Model';

import * as sirJSON from '@/examples/strata-example.json';

const model = ref<Model | null>(null);
const mmt = ref<MiraModel>(emptyMiraModel());
const mmtParams = ref<MiraTemplateParams>({});
const ready = ref(false);

const csvStringProcessor = async (item: DataTransferItem) => {
	if (item.kind !== 'string') return;
	if (item.type !== 'text/plain') return;

	const buffer: any[] = [];

	// Presume this is a full matrix, with row/column labels
	item.getAsString((text) => {
		// Parse

		const rows = text.split('\n');
		const columns = rows[0].split(',');

		for (let i = 1; i < rows.length; i++) {
			const data = rows[i].split(',');
			for (let j = 1; j < data.length; j++) {
				const rowLabel = data[0];
				const colLabel = columns[j];
				console.log('...', colLabel, rows[0], j);
				buffer.push({
					row: rowLabel,
					col: colLabel,
					value: data[j]
				});
			}
		}
		console.log('buffer', buffer);
	});

	return buffer;
};

const processPasteEvent = async (event: ClipboardEvent) => {
	const clipboardData = event.clipboardData;
	if (!clipboardData) return;
	const item = clipboardData.items[0];

	csvStringProcessor(item);
};

onMounted(async () => {
	model.value = sirJSON as any;
	const response: any = await getMMT(sirJSON as any);
	mmt.value = response.mmt;
	mmtParams.value = response.template_params;
	ready.value = true;

	document.onpaste = processPasteEvent;
});

onUnmounted(() => {
	document.onpaste = null;
});
</script>
