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
import { onMounted, ref, watch } from 'vue';
import BasicRenderer from 'graph-scaffolder/src/core/basic-renderer';
import { runDagreLayout } from '@/services/graph';
import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';
import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import {
	convertToIGraph,
	getStratificationType
} from '@/model-representation/petrinet/petrinet-service';
import { amr as amrExample } from './sir';
import { extractNestedMap } from '@/model-representation/petrinet/catlab-petri';
import {
	getAMRPresentationData,
	extractNestedStratas
} from '@/model-representation/petrinet/mira-petri';

const graphElement = ref<HTMLDivElement | null>(null);
const jsonStr = ref('');
const strataType = ref<string | null>(null);
const isCollapse = ref(true);

onMounted(async () => {
	jsonStr.value = JSON.stringify(amrExample, null, 2);

	watch(
		() => [jsonStr.value, isCollapse.value],
		async () => {
			let renderer: BasicRenderer<any, any>;
			let data: any;

			const amr = JSON.parse(jsonStr.value);
			strataType.value = getStratificationType(amr);

			if (strataType.value === null || isCollapse.value === false) {
				console.log('hihihhi');
				renderer = new PetrinetRenderer({
					el: graphElement.value as HTMLDivElement,
					useAStarRouting: false,
					useStableZoomPan: true,
					runLayout: runDagreLayout
				});
				data = convertToIGraph(amr);
			} else if (strataType.value === 'catlab') {
				const nestedMap = extractNestedMap(amr);
				renderer = new NestedPetrinetRenderer({
					el: graphElement.value as HTMLDivElement,
					useAStarRouting: false,
					useStableZoomPan: true,
					runLayout: runDagreLayout,
					nestedMap: nestedMap
				});
				data = convertToIGraph(amr);
			} else {
				const presentationData = getAMRPresentationData(amr);

				// 1. Find what are the strata dimensions
				const stateMatrixData = presentationData.stateMatrixData.map((d) => {
					const temp: any = _.cloneDeep(d);
					delete temp.id;
					delete temp.base;
					return Object.keys(temp);
				});
				const dims = _.uniq(_.flatten(stateMatrixData));
				dims.unshift('base');
				const nestedMap = extractNestedStratas(presentationData.stateMatrixData, dims);

				renderer = new NestedPetrinetRenderer({
					el: graphElement.value as HTMLDivElement,
					useAStarRouting: false,
					useStableZoomPan: true,
					runLayout: runDagreLayout,
					nestedMap: nestedMap
				});
				data = convertToIGraph(presentationData.compactModel as any);
			}

			await renderer.setData(data);
			renderer.isGraphDirty = true;
			renderer.render();
		},
		{ immediate: true }
	);
});
</script>
