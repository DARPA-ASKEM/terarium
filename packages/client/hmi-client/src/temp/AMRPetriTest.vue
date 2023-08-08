<template>
	<div style="display: flex; flex-direction: row; padding: 1rem">
		<textarea style="width: 550px; height: 550px; margin: 0 10px" v-model="jsonStr"> </textarea>
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
import { BasicRenderer } from 'graph-scaffolder/src/core/basic-renderer';
import { runDagreLayout } from '@/services/graph';
import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';
import { PetrinetRenderer } from '@/model-representation/petrinet/petrinet-renderer';
import {
	convertToIGraph,
	getStratificationType
} from '@/model-representation/petrinet/petrinet-service';
import { amr as amrExample } from './sir.ts';
import { extractNestedMap } from '@/model-representation/petrinet/catlab-petri';
import {
	getAMRPresentationData,
	extractNestedStratas
} from '@/model-representation/petrinet/mira-petri';

const graphElement = ref<HTMLDivElement | null>(null);
const jsonStr = ref('');
const strataType = ref<string | null>(null);

onMounted(async () => {
	jsonStr.value = JSON.stringify(amrExample, null, 2);

	watch(
		() => jsonStr.value,
		async () => {
			let renderer: BasicRenderer;
			let data: any;

			const amr = JSON.parse(jsonStr.value);
			strataType.value = getStratificationType(amr);

			if (strataType.value === null) {
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
					const temp = _.cloneDeep(d);
					delete temp._id;
					delete temp._base;
					return Object.keys(temp);
				});
				const dims = _.uniq(_.flatten(stateMatrixData));
				dims.unshift('_base');
				const nestedMap = extractNestedStratas(presentationData.stateMatrixData, dims);

				renderer = new NestedPetrinetRenderer({
					el: graphElement.value as HTMLDivElement,
					useAStarRouting: false,
					useStableZoomPan: true,
					runLayout: runDagreLayout,
					nestedMap: nestedMap
				});
				data = convertToIGraph(presentationData.compactModel);
			}

			await renderer.setData(data);
			renderer.isGraphDirty = true;
			renderer.render();
		},
		{ immediate: true }
	);
});
</script>
