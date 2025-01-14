<template>
	<tera-modal class="w-9">
		<template #default>
			<div class="grid" style="height: 70vh">
				<aside class="flex flex-column col-3">
					<label class="p-text-secondary pb-2">Select a template</label>
					<div v-for="[id, { name }] in scenarioMap" :key="id" class="flex align-items-center py-1 template-option">
						<RadioButton :inputId="id" :value="id" v-model="selectedTemplateId" />
						<label class="pl-2" :for="id">{{ name }}</label>
					</div>
				</aside>
				<main class="col-9 flex flex-column">
					<component
						v-if="getScenario()"
						ref="scenarioComponent"
						:is="getScenario().component"
						:scenario="getScenario().instance"
						@save-workflow="saveWorkflow()"
					/>
				</main>
			</div>
		</template>
		<template #footer>
			<Button
				label="Create"
				size="large"
				@click="saveWorkflow"
				:disabled="!getScenario().instance.isValid()"
				:loading="isCreatingWorkflow"
			/>
			<Button label="Close" class="p-button-secondary" size="large" outlined @click="emit('close-modal')" />
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import TeraModal from '@/components/widgets/tera-modal.vue';
import Button from 'primevue/button';
import { markRaw, nextTick, onMounted, ref } from 'vue';
import type { Component } from 'vue';
import RadioButton from 'primevue/radiobutton';
import { BaseScenario } from '@/components/workflow/scenario-templates/base-scenario';
import { createWorkflow } from '@/services/workflow';
import { AssetType } from '@/types/Types';
import { useProjects } from '@/composables/project';
import router from '@/router';
import { RouteName } from '@/router/routes';
import TeraBlankCanvasTemplate from '@/components/workflow/scenario-templates/blank-canvas/tera-blank-canvas-template.vue';
import TeraSituationalAwarenessTemplate from '@/components/workflow/scenario-templates/situational-awareness/tera-situational-awareness-template.vue';
import TeraSensitivityAnalysisTemplate from '@/components/workflow/scenario-templates/sensitivity-analysis/tera-sensitivity-analysis-template.vue';
import TeraDecisionMakingTemplate from '@/components/workflow/scenario-templates/decision-making/tera-decision-making-template.vue';
import TeraHorizonScanningTemplate from '@/components/workflow/scenario-templates/horizon-scanning/tera-horizon-scanning-template.vue';
import TeraValueOfInformationTemplate from '@/components/workflow/scenario-templates/value-of-information/tera-value-of-information-template.vue';
import { BlankCanvasScenario } from '@/components/workflow/scenario-templates/blank-canvas/blank-canvas-scenario';
import { SituationalAwarenessScenario } from '@/components/workflow/scenario-templates/situational-awareness/situational-awareness-scenario';
import { SensitivityAnalysisScenario } from '@/components/workflow/scenario-templates/sensitivity-analysis/sensitivity-analysis-scenario';
import { DecisionMakingScenario } from '@/components/workflow/scenario-templates/decision-making/decision-making-scenario';
import { HorizonScanningScenario } from '@/components/workflow/scenario-templates/horizon-scanning/horizon-scanning-scenario';
import { ValueOfInformationScenario } from '@/components/workflow/scenario-templates/value-of-information/value-of-information-scenario';
import TeraModelFromLiteratureTemplate from '@/components/workflow/scenario-templates/model-from-literature/tera-model-from-literature-template.vue';
import { ModelFromLiteratureScenario } from '@/components/workflow/scenario-templates/model-from-literature/model-from-literature-scenario';
import { CalibrateEnsembleScenario } from '@/components/workflow/scenario-templates/calibrate-ensemble/calibrate-ensemble-scenario';
import TeraCalibrateEnsembleTemplate from '@/components/workflow/scenario-templates/calibrate-ensemble/tera-calibrate-ensemble-template.vue';

interface ScenarioItem {
	name: string;
	instance: BaseScenario;
	component: Component;
}
const scenarioComponent = ref();
const scenarioMap = ref(
	new Map<string, ScenarioItem>([
		[
			BlankCanvasScenario.templateId,
			{
				name: BlankCanvasScenario.templateName,
				instance: new BlankCanvasScenario(),
				component: markRaw(TeraBlankCanvasTemplate)
			}
		],
		[
			SituationalAwarenessScenario.templateId,
			{
				name: SituationalAwarenessScenario.templateName,
				instance: new SituationalAwarenessScenario(),
				component: markRaw(TeraSituationalAwarenessTemplate)
			}
		],
		[
			SensitivityAnalysisScenario.templateId,
			{
				name: SensitivityAnalysisScenario.templateName,
				instance: new SensitivityAnalysisScenario(),
				component: markRaw(TeraSensitivityAnalysisTemplate)
			}
		],
		[
			DecisionMakingScenario.templateId,
			{
				name: DecisionMakingScenario.templateName,
				instance: new DecisionMakingScenario(),
				component: markRaw(TeraDecisionMakingTemplate)
			}
		],
		[
			HorizonScanningScenario.templateId,
			{
				name: HorizonScanningScenario.templateName,
				instance: new HorizonScanningScenario(),
				component: markRaw(TeraHorizonScanningTemplate)
			}
		],
		[
			ValueOfInformationScenario.templateId,
			{
				name: ValueOfInformationScenario.templateName,
				instance: new ValueOfInformationScenario(),
				component: markRaw(TeraValueOfInformationTemplate)
			}
		],
		[
			ModelFromLiteratureScenario.templateId,
			{
				name: ModelFromLiteratureScenario.templateName,
				instance: new ModelFromLiteratureScenario(),
				component: markRaw(TeraModelFromLiteratureTemplate)
			}
		],
		[
			CalibrateEnsembleScenario.templateId,
			{
				name: CalibrateEnsembleScenario.templateName,
				instance: new CalibrateEnsembleScenario(),
				component: markRaw(TeraCalibrateEnsembleTemplate)
			}
		]
	])
);

const emit = defineEmits(['close-modal']);
// get first map entry (Blank Canvas)
const selectedTemplateId = ref<any>(scenarioMap.value.keys().next().value);
const isCreatingWorkflow = ref(false);

const saveWorkflow = async () => {
	if (!getScenario().instance.isValid()) return;

	isCreatingWorkflow.value = true;
	const scenario = getScenario();
	const wf = await scenario.instance.createWorkflow();
	const response = await createWorkflow(wf);

	const projectId = useProjects().activeProject.value?.id;
	await useProjects().addAsset(AssetType.Workflow, response.id, projectId);

	await useProjects().refresh();

	// redirect to the asset page
	router.push({
		name: RouteName.Project,
		params: {
			projectId,
			pageType: AssetType.Workflow,
			assetId: response.id
		}
	});

	isCreatingWorkflow.value = false;

	emit('close-modal');
};

onMounted(() => {
	/* HACK: wait for the modal to be fully rendered before focusing the input,
	it seems that the auto-focus on tera-input-text does not play nicely on the initial render of the modal */
	nextTick(() => {
		scenarioComponent.value.$refs.blankTemplate?.$refs.nameInput?.focusInput();
	});
});
const getScenario = () => scenarioMap.value.get(selectedTemplateId.value) as ScenarioItem;
</script>
<style scoped>
.template-option:first-of-type {
	margin-bottom: var(--gap-5);
}
</style>
