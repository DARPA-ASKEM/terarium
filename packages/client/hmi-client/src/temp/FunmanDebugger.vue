<template>
	<main>
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
			<div v-if="modelConfiguration && processedResult" class="flex m-3 gap-4">
				<div>
					<h5>Model configuration bounds</h5>
					<code>
						<ul>
							<li v-for="(param, index) in modelConfiguration.parameterSemanticList" :key="index">
								<b>{{ param.referenceId }}</b>
								<span class="ml-auto" v-if="param.distribution.parameters?.minimum">
									[{{ param.distribution.parameters?.minimum }}, {{ param.distribution.parameters?.maximum }}]
								</span>
								<span class="ml-auto" v-else>
									{{ param.distribution.parameters?.value }}
								</span>
							</li>
						</ul>
					</code>
				</div>
				<div>
					<div v-for="(boxes, i) in [trueBoxes, falseBoxes, unknownPoints]" :key="i">
						<h5>{{ boxLabels[i] }} box bounds</h5>
						<code class="flex">
							<div v-for="(param, j) in modelConfiguration.parameterSemanticList" :key="j">
								<div>
									<ul>
										<li>
											<b>{{ param.referenceId }}</b>
										</li>
										<li v-for="(box, k) in boxes" :key="k">
											box{{ box.boxId }}:
											<div>
												<br />
												bounds: <b>{{ displayBounds(box.parameters[param.referenceId]) }}</b>
												<br />
												&nbsp;point: <b>{{ box.parameters[param.referenceId].point }}</b>
											</div>
										</li>
									</ul>
								</div>
							</div>
						</code>
					</div>
				</div>
			</div>
		</section>
	</main>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { processFunman, type ProcessedFunmanResult } from '@/services/models/funman-service';
import { getModelConfigurationById } from '@/services/model-configurations';
import { ModelConfiguration } from '@/types/Types';

const rawOutput = ref('');
const processedOutput = ref('');
const processedResult = ref<ProcessedFunmanResult | null>(null);
const modelConfiguration = ref<ModelConfiguration | null>(null);

const trueBoxes = ref<any>([]);
const falseBoxes = ref<any>([]);
const unknownPoints = ref<any>([]);
const boxLabels = ['True', 'False', 'Unknown'];

async function processRawOutput() {
	const jsonOutput = JSON.parse(rawOutput.value);
	processedResult.value = processFunman(jsonOutput);

	trueBoxes.value = processedResult.value.boxes.filter((box) => box.label === 'true');
	falseBoxes.value = processedResult.value.boxes.filter((box) => box.label === 'false');
	unknownPoints.value = processedResult.value.boxes.filter((box) => box.label === 'unknown');

	processedOutput.value = JSON.stringify(processedResult.value, null, 2);
	modelConfiguration.value = await getModelConfigurationById(jsonOutput.modelConfigurationId);
	console.log(modelConfiguration.value);
}

function displayBounds(boxParameter: any) {
	const { lb, ub } = boxParameter;
	if (lb === ub) return lb;
	return `[${lb}, ${ub}]`;
}
</script>

<style scoped>
textarea {
	width: 100%;
	height: 90%;
}

section {
	flex: 1;
	overflow: auto;
	padding-bottom: 1rem;
	& > div {
		min-height: 50%;
	}
}

h5 {
	margin: 0.5rem 0;
}

ul {
	padding: 1rem;
	outline: 1px solid gray;
	background-color: white;
}

li {
	display: flex;
}
</style>
