<template>
	<div style="display: flex; flex-direction: row; padding: 1rem">
		<div style="display: flex; flex-direction: column">
			<textarea style="width: 550px; height: 550px; margin: 0 10px" v-model="jsonStr"> </textarea>
			<div>
				<button style="width: 10rem; margin: 3px; font-size: 125%" @click="isCollapse = true">
					Collapse
				</button>
				<button style="width: 10rem; margin: 3px; font-size: 125%" @click="isCollapse = false">
					Expand
				</button>
			</div>
		</div>
		<div>
			<div style="position: fixed; padding: 2px">
				{{ strataType === null ? 'No strata' : strataType }}
			</div>
			<div ref="graphElement" class="canvas" style="height: 600px; width: 900px"></div>
		</div>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { getModelRenderer } from '@/model-representation/service';
import { getMMT } from '@/services/model';

import { onMounted, ref, watch } from 'vue';
import * as mmtExample from '@/examples/mira-petri.json';
import {
	collapseTemplates,
	convertToIGraph,
	collapseParameters,
	createParameterMatrix
} from '@/model-representation/mira/mira';

const graphElement = ref<HTMLDivElement | null>(null);
const jsonStr = ref('');
const strataType = ref<string | null>(null);
const isCollapse = ref(true);

onMounted(async () => {
	jsonStr.value = JSON.stringify(mmtExample, null, 2);

	watch(
		() => jsonStr.value,
		async () => {
			const jsonData = JSON.parse(jsonStr.value);
			const mmtR = await getMMT(jsonData);
			const mmt = mmtR.mmt;
			const template_params = mmtR.template_params;

			const renderer = getModelRenderer(mmt, graphElement.value as HTMLDivElement, false);
			const { templatesSummary } = collapseTemplates(mmt);
			const graphData = convertToIGraph(templatesSummary);

			// Create all possible matrices
			const rootParams = collapseParameters(mmt, template_params);
			const rootParamKeys = [...rootParams.keys()];

			const lines: any[] = [];
			rootParamKeys.forEach((key) => {
				const matrices = createParameterMatrix(mmt, template_params, key);
				let header = '';

				const subjectOutcome = matrices.subjectOutcome;
				const subjectControllers = matrices.subjectControllers;
				const outcomeControllers = matrices.outcomeControllers;

				if (subjectOutcome.matrix.length > 0) {
					lines.push(`subject-outcome of ${key}`);
					header = ',' + subjectOutcome.rowNames.join(',');
					lines.push('');
					lines.push(header);
					subjectOutcome.matrix.forEach((r, idx) => {
						const rowStr = subjectOutcome.colNames[idx] + r.map((d) => d.content.id).join(',');
						lines.push(rowStr);
					});
					lines.push('');
					lines.push('');
				}

				if (subjectControllers.matrix.length > 0) {
					lines.push(`subject-controllers of ${key}`);
					header = ',' + subjectControllers.rowNames.join(',');
					lines.push('');
					lines.push(header);
					subjectControllers.matrix.forEach((r, idx) => {
						const rowStr =
							subjectControllers.colNames[idx] + ',' + r.map((d) => d.content.id).join(',');
						lines.push(rowStr);
					});
					lines.push('');
					lines.push('');
				}

				if (outcomeControllers.matrix.length > 0) {
					lines.push(`outcome-controllers of ${key}`);
					header = ',' + outcomeControllers.rowNames.join(',');
					lines.push('');
					lines.push(header);
					outcomeControllers.matrix.forEach((r, idx) => {
						const rowStr =
							outcomeControllers.colNames[idx] + ',' + r.map((d) => d.content.id).join(',');
						lines.push(rowStr);
					});
					lines.push('');
					lines.push('');
				}
			});

			console.log(lines.join('\n'));

			await renderer.setData(graphData);
			renderer.isGraphDirty = true;
			renderer.render();
		},
		{ immediate: true }
	);
});
</script>
