<template>
	<Dropdown
		placeholder="Timestep Column Name"
		class="p-button dropdown-button"
		:options="datasetColumnNames"
		v-model="timestepColumnName"
	/>
	<table v-if="featureMap">
		<tr>
			<th>Dataset Column Name</th>
			<th>Model Column Name</th>
		</tr>
		<tr v-for="(content, index) in featureMap" :key="index">
			<Dropdown
				placeholder="Dataset Column Name"
				class="p-button dropdown-button"
				:options="datasetColumnNames"
				v-model="featureMap[index][0]"
			/>
			<td>{{ content[1] }}</td>
		</tr>
	</table>
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
</template>

<script setup lang="ts">
import { ref, shallowRef, watch } from 'vue';
import Button from 'primevue/button';
import { makeCalibrateJob, getRunStatus, getRunResult } from '@/services/models/simulation-service';
import { CalibrationParams, CsvAsset } from '@/types/Types';
import { ModelConfig } from '@/types/ModelConfig';
import Dropdown from 'primevue/dropdown';
import { downloadRawFile } from '@/services/dataset';
import { PetriNet } from '@/petrinet/petrinet-service';
// import { calibrationParamExample } from '@/temp/calibrationExample';

const props = defineProps<{
	modelConfig: ModelConfig | null;
	datasetId: number | null;
}>();

const runId = ref('');
const timestepColumnName = ref<string>('');
const datasetColumnNames = ref<string[]>();
const modelColumnNames = ref();
const csvAsset = shallowRef<CsvAsset | undefined>(undefined);
const datasetValue = ref();

// const featuredIndex = computed(props.modelConfig.model.content || []);
// const featureValues = ref<[String]>(['']);
const featureMap = ref();

const startCalibration = async () => {
	// Make calibration job.
	// FIXME: current need to strip out metadata, should do serverside
	const cleanedModel: PetriNet = {
		S: [],
		T: [],
		I: [],
		O: []
	};
	if (props.modelConfig) {
		// Take out all the extra content in model.content
		cleanedModel.S = props.modelConfig.model.content.S.map((s) => ({ sname: s.sname }));
		cleanedModel.T = props.modelConfig.model.content.T.map((t) => ({ tname: t.tname }));
		cleanedModel.I = props.modelConfig.model.content.I;
		cleanedModel.O = props.modelConfig.model.content.O;

		if (featureMap.value) {
			const featureObject: { [index: string]: string } = {};
			// Go from 2D array to a index: value like they want
			// was just easier to work with 2D array for user input
			for (let i = 0; i < featureMap.value.length; i++) {
				featureObject[featureMap.value[i][0]] = featureMap.value[i][1];
			}

			const calibrationParam: CalibrationParams = {
				model: JSON.stringify(cleanedModel),
				initials: props.modelConfig.initialValues,
				params: props.modelConfig.parameterValues,
				timesteps_column: timestepColumnName.value,
				feature_mappings: featureObject,
				dataset: datasetValue.value
			};
			console.log(calibrationParam);
			const results = await makeCalibrateJob(calibrationParam);
			runId.value = results.id;
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
	() => props.modelConfig,
	async () => {
		if (props.modelConfig) {
			console.log(props.modelConfig);
			// When model changes get model column names
			modelColumnNames.value = props.modelConfig.model?.content?.S.map((state) => state.sname);

			// initialize featureMap
			// featureMap.value = [[]];
			featureMap.value = modelColumnNames.value.map((stateName) => ['123', stateName]);
		}
	}
);

watch(
	() => props.datasetId, // When dataset ID changes, update datasetColumnNames
	async () => {
		if (props.datasetId) {
			// Get dataset:
			csvAsset.value = (await downloadRawFile(props.datasetId.toString())) as CsvAsset;
			datasetColumnNames.value = csvAsset.value?.headers;
			datasetValue.value = csvAsset.value?.csv.map((row) => row.join(',')).join('\n');
		}
	}
);
</script>

<style scoped>
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
