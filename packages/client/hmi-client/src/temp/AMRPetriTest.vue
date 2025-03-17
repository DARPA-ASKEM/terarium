<template>
	<div style="display: flex; flex-direction: row; padding: 1rem">
		<div style="display: flex; flex-direction: column">
			<textarea style="width: 600px; height: 425px; margin: 0 10px" v-model="jsonStr"> </textarea>
			<div style="height: 150px; max-height: 150px; overflow-y: scroll; border: 1px solid #bbb; margin: 0.5rem">
				<table style="width: 100%">
					<tr v-for="(log, idx) in errors" :key="idx" style="border: 1px solid #ddd">
						<td :class="[log.severity]" style="width: 6rem">{{ log.type }}</td>
						<td>{{ log.content }}</td>
					</tr>
				</table>
				<div v-if="errors.length === 0">No problems found</div>
			</div>
			<div>
				<button style="width: 10rem; margin: 3px; font-size: 100%" @click="isCollapsed = true">Collapse</button>
				<button style="width: 10rem; margin: 3px; font-size: 100%" @click="isCollapsed = false">Expand</button>
			</div>
		</div>
		<div>
			<div ref="graphElement" class="canvas" style="height: 600px; width: 750px"></div>
		</div>
	</div>
</template>

<script setup lang="ts">
import _ from 'lodash';
import { checkPetrinetAMR, getModelRenderer } from '@/model-representation/service';
import { getMMT } from '@/services/model';

import { onMounted, ref, watch } from 'vue';
import * as mmtExample from '@/examples/mira-petri.json';
import { convertToIGraph } from '@/model-representation/mira/mira';

const graphElement = ref<HTMLDivElement | null>(null);
const jsonStr = ref('');
const isCollapsed = ref(true);
const errors = ref<any[]>([]);

onMounted(async () => {
	jsonStr.value = JSON.stringify(mmtExample, null, 2);

	watch(
		() => [jsonStr.value, isCollapsed.value],
		async () => {
			const jsonData = JSON.parse(jsonStr.value);
			errors.value = await checkPetrinetAMR(jsonData);

			const mmtR = await getMMT(jsonData);
			if (!mmtR) return;
			const mmt = mmtR.mmt;
			const observable_summary = mmtR.observable_summary;

			const renderer = getModelRenderer(mmt, graphElement.value as HTMLDivElement, true);
			const graphData = convertToIGraph(mmt, observable_summary, isCollapsed.value);

			await renderer.setData(graphData);
			renderer.isGraphDirty = true;
			renderer.render();
		},
		{ immediate: true }
	);
});
</script>

<style scoped>
table {
	border-collapse: collapse;
}

td {
	padding: 2px;
}

td.error {
	background: #f20;
	text-align: center;
}
td.warn {
	background: #f80;
	text-align: center;
}
</style>
