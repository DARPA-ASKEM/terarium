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
import BasicRenderer from 'graph-scaffolder/src/core/basic-renderer';
import { runDagreLayout } from '@/services/graph';
import { extractNestedStratas } from '@/model-representation/petrinet/mira-petri';
// import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';
// import { getStratificationType } from '@/model-representation/petrinet/petrinet-service';
// import * as amrExample from '@/examples/sir.json';
// import { getGraphData, getPetrinetRenderer } from '@/model-representation/petrinet/petri-util';

import { onMounted, ref, watch } from 'vue';
import * as mmtExample from '@/examples/mmt.json';
import {
	collapseTemplates,
	converToIGraph,
	getContextKeys
} from '@/model-representation/mira/mira';

const graphElement = ref<HTMLDivElement | null>(null);
const jsonStr = ref('');
const strataType = ref<string | null>(null);
const isCollapse = ref(true);

let renderer: BasicRenderer<any, any>;

onMounted(async () => {
	jsonStr.value = JSON.stringify(mmtExample, null, 2);

	watch(
		() => jsonStr.value,
		async () => {
			const collapsedTemplates = collapseTemplates(mmtExample);
			// console.log(collapsedTemplates);
			// console.log(getContextKeys(mmtExample));

			const dims = getContextKeys(mmtExample);
			const graphData = converToIGraph(collapsedTemplates);

			// Testing
			const processedSet = new Set<string>();
			const conceptData: any = [];
			mmtExample.templates.forEach((t) => {
				['subject', 'outcome', 'controller'].forEach((conceptKey) => {
					if (!t[conceptKey]) return;
					const conceptName = t[conceptKey].name;
					if (processedSet.has(conceptName)) return;
					conceptData.push({
						// FIXME: use reverse-lookup to get root concept
						base: _.first(conceptName.split('_')),
						...t[conceptKey].context
					});

					processedSet.add(conceptName);
				});
			});

			dims.unshift('base');
			const nestedMap = extractNestedStratas(conceptData, dims);
			console.log(nestedMap);

			renderer = new NestedPetrinetRenderer({
				el: graphElement.value as HTMLDivElement,
				useAStarRouting: false,
				useStableZoomPan: true,
				runLayout: runDagreLayout,
				dims: dims,
				nestedMap: nestedMap
			});
			await renderer.setData(graphData);
			renderer.isGraphDirty = true;
			renderer.render();
		},
		{ immediate: true }
	);

	/*
	jsonStr.value = JSON.stringify(amrExample, null, 2);

	watch(
		() => [jsonStr.value, isCollapse.value],
		async () => {
			let renderer: BasicRenderer<any, any>;
			let data: any;

			const amr = JSON.parse(jsonStr.value);
			strataType.value = getStratificationType(amr);

			data = getGraphData(amr, isCollapse.value);

			if (isCollapse.value === false) {
				renderer = new PetrinetRenderer({
					el: graphElement.value as HTMLDivElement,
					useAStarRouting: false,
					useStableZoomPan: true,
					runLayout: runDagreLayout
				});
			} else {
				renderer = getPetrinetRenderer(amr, graphElement.value as HTMLDivElement);
			}

			await renderer.setData(data);
			renderer.isGraphDirty = true;
			renderer.render();
		},
		{ immediate: true }
	);
	*/
});
</script>
