<template>
	<tera-workflow-node :node="node">
		<template #body v-if="viewState === VIEWOPTIONS.ONNODE">
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
	</tera-workflow-node>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import { makeCalibrateJob, getRunStatus, getRunResult } from '@/services/models/simulation-service';
import { CalibrationParams } from '@/types/Types';
import { calibrationParamExample } from '@/temp/calibrationExample';
import { WorkflowNode, VIEWOPTIONS } from '@/types/workflow';
import TeraWorkflowNode from './tera-workflow-node.vue';

const props = defineProps<{
	node: WorkflowNode;
}>();

const runId = ref('');
const viewState = computed(() => props.node.viewState);

const startCalibration = async () => {
	// Make calibration job.
	const calibrationParam: CalibrationParams = calibrationParamExample;
	const results = await makeCalibrateJob(calibrationParam);
	runId.value = results.id;
	console.log(runId.value);
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
