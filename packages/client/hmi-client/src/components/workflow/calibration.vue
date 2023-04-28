<template>
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
import { ref } from 'vue';
import Button from 'primevue/button';
// import { WorkflowNode } from '@/types/workflow';
import { makeCalibrateJob, getRunStatus, getRunResult } from '@/services/models/simulation-service';
import { CalibrationParams } from '@/types/Types';
import { calibrationParamExample } from '@/temp/calibrationExample';

// const props = defineProps<{
// 	workflowNode: WorkflowNode;
// }>();

const runId = ref('');

const startCalibration = async () => {
	// Make calibration job.
	const calibrationParam: CalibrationParams = calibrationParamExample;
	runId.value = await makeCalibrateJob(calibrationParam);
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
</script>

<style scoped></style>
