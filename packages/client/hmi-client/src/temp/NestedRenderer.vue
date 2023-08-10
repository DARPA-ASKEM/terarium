<template>
	<div style="display: flex; flex-direction: column">
		<p>hello world</p>
		<div ref="graphElement" class="canvas" style="height: 600px; width: 1200px"></div>
	</div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { runDagreLayout } from '@/services/graph';
import { NestedPetrinetRenderer } from '@/model-representation/petrinet/nested-petrinet-renderer';
import { convertToIGraph } from '@/model-representation/petrinet/petrinet-service';
import { amr } from './sir.ts';

const graphElement = ref<HTMLDivElement | null>(null);

const nestedMap = {
	S: ['a', 'b', 'c'],
	R: ['x', 'y']
};

onMounted(async () => {
	const graphData = convertToIGraph(amr);

	const renderer = new NestedPetrinetRenderer({
		el: graphElement.value as HTMLDivElement,
		useAStarRouting: false,
		useStableZoomPan: true,
		runLayout: runDagreLayout,
		nestedMap: nestedMap
	});

	await renderer?.setData(graphData);
	renderer.render();
});
</script>
