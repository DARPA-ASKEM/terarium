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

import { onMounted, ref, watch } from 'vue';
import * as mmtExample from '@/examples/mmt.json';
import { collapseTemplates, converToIGraph } from '@/model-representation/mira/mira';

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
			const renderer = getModelRenderer(jsonData, graphElement.value as HTMLDivElement);
			const { templatesSummary } = collapseTemplates(jsonData);
			const graphData = converToIGraph(templatesSummary);

			await renderer.setData(graphData);
			renderer.isGraphDirty = true;
			renderer.render();
		},
		{ immediate: true }
	);
});
</script>
