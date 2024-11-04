<template>
	<section class="flex flex-column">
		<div class="flex m-3 gap-2">
			<div class="w-12">
				<h5>Paste raw funman output here</h5>
				<textarea v-model="rawOutput" />
				<button @click="processRawOutput">Process raw output</button>
			</div>
			<div class="w-12">
				<h5>Processed funman output</h5>
				<textarea v-model="processedOutput" />
			</div>
		</div>
		<div v-if="modelConfiguration && processedResult" class="flex m-3 gap-2">
			<div>
				<h5>Model configuration</h5>
				<code>
					<ul class="white">
						<li v-for="(param, index) in modelConfiguration.parameterSemanticList" :key="index">
							<b>{{ param.referenceId }}</b>
							<span>[{{ param.distribution.parameters }}]</span>
						</li>
					</ul>
				</code>
			</div>
			<template v-for="(boxes, i) in [trueBoxes, falseBoxes, unknownPoints]" :key="i">
				<div v-for="(param, j) in modelConfiguration.parameterSemanticList" :key="j">
					<div>
						{{ param.referenceId }}
						<ul>
							<li v-for="(box, k) in boxes" :key="k">
								{{ displayBounds(box.parameters[param.referenceId]) }}
							</li>
						</ul>
					</div>
				</div>
			</template>
		</div>
	</section>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { processFunman, type ProcessedFunmanResult } from '@/services/models/funman-service';
import { getModelConfigurationById } from '@/services/model-configurations';
import { ModelConfiguration } from '@/types/Types';

const rawOutput = ref('');
const processedOutput = ref('');
const processedResult = ref<ProcessedFunmanResult | null>(null);
const trueBoxes = ref<any>([]);
const falseBoxes = ref<any>([]);
const unknownPoints = ref<any>([]);
const modelConfiguration = ref<ModelConfiguration | null>(null);

async function processRawOutput() {
	const jsonOutput = JSON.parse(rawOutput.value);
	processedResult.value = processFunman(jsonOutput);

	trueBoxes.value = processedResult.value.boxes.filter((box) => box.label === 'true');
	falseBoxes.value = processedResult.value.boxes.filter((box) => box.label === 'false');
	unknownPoints.value = processedResult.value.boxes.filter((box) => box.label === 'unknown');

	processedOutput.value = JSON.stringify(processedResult.value, null, 2);
	modelConfiguration.value = await getModelConfigurationById(jsonOutput.modelConfigurationId);
}

function displayBounds(boxParameter: any) {
	const { lb, ub } = boxParameter;
	return `[${lb}, ${ub}]`;
}
</script>

<style scoped>
textarea {
	width: 100%;
	height: 90%;
}

section {
	overflow: auto;
}

section > * {
	flex: 1;
}

.white {
	background-color: white;
}

li {
	display: flex;
	justify-content: space-between;
}
</style>
