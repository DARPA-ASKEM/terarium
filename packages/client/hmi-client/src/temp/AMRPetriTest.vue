<template>
	<div style="display: flex; flex-direction: row; padding: 1rem">
		<textarea style="width: 550px; height: 550px; margin: 0 10px" v-model="jsonStr"> </textarea>
		<div ref="graphElement" class="canvas" style="height: 600px; width: 900px"></div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { runDagreLayout } from '@/services/graph';
import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';
import { convertToIGraph } from '@/model-representation/petrinet/petrinet-service';
import { amr } from './sir.ts';

const graphElement = ref<HTMLDivElement | null>(null);
const jsonStr = ref('');

const nestedMap = {
	S: ['a', 'b', 'c'],
	R: ['x', 'y']
};

onMounted(async () => {
	jsonStr.value = JSON.stringify(amr, null, 2);

	const graphData = convertToIGraph(JSON.parse(jsonStr.value));
	const renderer = new NestedPetrinetRenderer({
		el: graphElement.value as HTMLDivElement,
		useAStarRouting: false,
		useStableZoomPan: true,
		runLayout: runDagreLayout,
		nestedMap: nestedMap
	});

	await renderer?.setData(graphData);
	renderer.render();

	watch(
		() => jsonStr.value,
		async () => {
			const data = convertToIGraph(JSON.parse(jsonStr.value));
			await renderer.setData(data);
			renderer.isGraphDirty = true;
			renderer.render();
		}
	);
});
</script>
