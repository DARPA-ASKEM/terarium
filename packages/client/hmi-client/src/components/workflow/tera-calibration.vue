<template>
	<!--Probably rename tera-asset to something even more abstract-->
	<tera-asset :name="'Calibrate'" is-editable stretch-content>
		<template #nav>
			<tera-asset-nav :show-header-links="false">
				<template #viewing-mode>
					<span class="p-buttonset">
						<Button
							class="p-button-secondary p-button-sm"
							label="Input"
							icon="pi pi-sign-in"
							@click="calibrationView = CalibrationView.INPUT"
							:active="calibrationView === CalibrationView.INPUT"
						/>
						<Button
							class="p-button-secondary p-button-sm"
							label="Output"
							icon="pi pi-sign-out"
							@click="calibrationView = CalibrationView.OUTPUT"
							:active="calibrationView === CalibrationView.OUTPUT"
						/>
					</span>
				</template>
			</tera-asset-nav>
		</template>
		<template #edit-buttons>
			<Button icon="pi pi-play" label="Run" class="p-button-sm" />
		</template>
		<Accordion
			v-if="calibrationView === CalibrationView.INPUT && modelConfig"
			:multiple="true"
			:active-index="[0, 1, 2]"
		>
			<AccordionTab :header="modelConfig.model.name">
				<tera-model-diagram :model="modelConfig.model" :is-editable="false" />
				<!-- @update-model-content="updateModelContent" -->
			</AccordionTab>
			<AccordionTab header="Model configuation">
				<tera-model-configuration
					:model="modelConfig.model"
					:is-editable="false"
					calibration-config
				/>
			</AccordionTab>
			<AccordionTab header="Dataset name">
				<tera-dataset-datatable :raw-content="csvAsset ?? null" />
			</AccordionTab>
		</Accordion>
		<Dropdown
			placeholder="Timestep Column Name"
			class="p-button dropdown-button"
			:options="datasetColumnNames"
			v-model="timestepColumnName"
		/>
		<!-- <table v-if="featureMap">
			<tr>
				<th>Dataset Column Name</th>
				<th>Model Column Name</th>
			</tr>
			<tr v-for="(content, index) in featureMap" :key="index">
				<Dropdown placeholder="Dataset Column Name" class="p-button dropdown-button" :options="datasetColumnNames"
					v-model="featureMap[index][0]" />
				<td>{{ content[1] }}</td>
			</tr>
		</table> -->
		<Button @click="startCalibration">Start Calibration Job</Button>
		<form>
			<label for="calibrationStatus">
				<input v-model="runId" type="text" placeholder="Run ID" />
			</label>
			<Button @click="getCalibrationStatus"> Get Run Status </Button>
		</form>

		<form>
			<label for="calibrationResult">
				<input v-model="runId" type="text" placeholder="Run ID" />
			</label>
			<Button @click="getCalibrationResults"> Get Run Results </Button>
		</form>
	</tera-asset>
</template>

<script setup lang="ts">
import { computed, ref, shallowRef, watch } from 'vue';
import Button from 'primevue/button';
import { makeCalibrateJob, getRunStatus, getRunResult } from '@/services/models/simulation-service';
import { CalibrationParams, CsvAsset } from '@/types/Types';
import { ModelConfig } from '@/types/ModelConfig';
import Dropdown from 'primevue/dropdown';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { downloadRawFile } from '@/services/dataset';
import { PetriNet } from '@/petrinet/petrinet-service';
import { WorkflowNode } from '@/types/workflow';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraAssetNav from '@/components/asset/tera-asset-nav.vue';
import TeraModelDiagram from '@/components/models/tera-model-diagram.vue';
import TeraModelConfiguration from '@/components/models/tera-model-configuration.vue';
import TeraDatasetDatatable from '@/components/dataset/tera-dataset-datatable.vue';

enum CalibrationView {
	INPUT = 'input',
	OUTPUT = 'output'
}

const props = defineProps<{
	node: WorkflowNode;
}>();

const modelConfig = computed(() => props.node.inputs[0].value as ModelConfig | undefined);
const datasetId = computed(() => props.node.inputs[1].value as number | undefined);

const runId = ref(props.node.outputs?.[0]?.value ?? undefined);
const timestepColumnName = ref<string>('');

const datasetColumnNames = ref<string[]>();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);
const datasetValue = ref();

const modelColumnNames = computed(() =>
	modelConfig.value?.model.content.S.map((state) => state.sname)
);
const featureMap = computed(() => modelColumnNames.value.map((stateName) => ['', stateName]));

const calibrationView = ref(CalibrationView.INPUT);

const startCalibration = async () => {
	// Make calibration job.
	// FIXME: current need to strip out metadata, should do serverside

	if (modelConfig.value) {
		console.log(modelConfig.value);

		const { S, T, I, O } = modelConfig.value.model.content;
		// Take out all the extra content in model.content
		const cleanedModel: PetriNet = {
			S: S.map((s) => ({ sname: s.sname })),
			T: T.map((t) => ({ tname: t.tname })),
			I,
			O
		};

		if (featureMap.value) {
			const featureObject: { [index: string]: string } = {};
			// Go from 2D array to a index: value like they want
			// was just easier to work with 2D array for user input
			for (let i = 0; i < featureMap.value.length; i++) {
				featureObject[featureMap.value[i][0]] = featureMap.value[i][1];
			}

			const calibrationParam: CalibrationParams = {
				model: JSON.stringify(cleanedModel),
				initials: modelConfig.value.initialValues,
				params: modelConfig.value.parameterValues,
				timesteps_column: timestepColumnName.value,
				feature_mappings: featureObject,
				dataset: datasetValue.value
			};
			const results = await makeCalibrateJob(calibrationParam);
			runId.value = results.id;

			console.table(calibrationParam);
			console.log(results);
		}
	}
};

const getCalibrationStatus = async () => {
	console.log('Getting status of run');
	const results = await getRunStatus(Number(runId.value));
	console.log('Done');
	console.log(results);
};

const getCalibrationResults = async () => {
	console.log('Getting results of run');
	const results = await getRunResult(Number(runId.value));
	console.log('Done');
	console.log(results);
};

watch(
	() => datasetId.value, // When dataset ID changes, update datasetColumnNames
	async () => {
		if (datasetId.value) {
			// Get dataset:
			csvAsset.value = (await downloadRawFile(datasetId.value.toString())) as CsvAsset;
			datasetColumnNames.value = csvAsset.value?.headers;
			datasetValue.value = csvAsset.value?.csv.map((row) => row.join(',')).join('\n');
			console.log(datasetValue.value);
		}
	}
);
</script>

<style scoped>
.p-accordion {
	padding-top: 1rem;
}

.dropdown-button {
	width: 156px;
	height: 25px;
	border-radius: 6px;
}

:deep(.p-dropdown .p-dropdown-label.p-inputtext) {
	color: white;
}

:deep(.p-inputtext) {
	color: white;
}
</style>
