<template>
	<tera-modal class="w-9">
		<template #header>
			<h4>Create a new workflow</h4>
		</template>
		<template #default>
			<div class="grid">
				<aside class="flex flex-column col-3">
					<label class="p-text-secondary pb-2">Select a template</label>
					<div v-for="scenario in scenarios" :key="scenario.id" class="flex align-items-center py-1">
						<RadioButton :inputId="scenario.id" :value="scenario.id" v-model="selectedTemplateId" />
						<label class="pl-2" :for="scenario.id">{{ scenario.displayName }}</label>
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
import { BlankCanvasScenario } from '@/components/workflow/scenario-templates/blank-canvas/blank-canvas-scenario';
import { SituationalAwarenessScenario } from '@/components/workflow/scenario-templates/situational-awareness/situational-awareness-scenario';
import { SensitivityAnalysisScenario } from '@/components/workflow/scenario-templates/sensitivity-analysis/sensitivity-analysis-scenario';
import { DecisionMakingScenario } from '@/components/workflow/scenario-templates/decision-making/decision-making-scenario';

interface ScenarioItem {
	displayName: string;
	id: string;
	instance: BaseScenario;
	component: Component;
}
const scenarioComponent = ref();
const scenarios = ref<ScenarioItem[]>([
	{
		displayName: BlankCanvasScenario.templateName,
		id: BlankCanvasScenario.templateId,
		instance: new BlankCanvasScenario(),
		component: markRaw(TeraBlankCanvasTemplate)
	},
	{
		displayName: SituationalAwarenessScenario.templateName,
		id: SituationalAwarenessScenario.templateId,
		instance: new SituationalAwarenessScenario(),
		component: markRaw(TeraSituationalAwarenessTemplate)
	},
	{
		displayName: SensitivityAnalysisScenario.templateName,
		id: SensitivityAnalysisScenario.templateId,
		instance: new SensitivityAnalysisScenario(),
		component: markRaw(TeraSensitivityAnalysisTemplate)
	},
	{
		displayName: DecisionMakingScenario.templateName,
		id: DecisionMakingScenario.templateId,
		instance: new DecisionMakingScenario(),
		component: markRaw(TeraDecisionMakingTemplate)
	}
]);

const emit = defineEmits(['close-modal']);

const selectedTemplateId = ref<any>(scenarios.value[0].id);
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
const getScenario = () => scenarios.value.find((s) => s.id === selectedTemplateId.value) as ScenarioItem;
</script>
