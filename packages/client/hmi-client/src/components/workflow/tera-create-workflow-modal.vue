<template>
	<tera-modal class="w-9">
		<template #header>
			<h4>Create a new workflow</h4>
		</template>
		<template #default>
			<div class="grid">
				<aside class="col-2">
					<label>Select a template</label>
					<div v-for="scenario in scenarios" :key="scenario.id" class="flex align-items-center py-1">
						<RadioButton
							:inputId="scenario.id"
							:value="scenario.id"
							v-model="selectedTemplateId"
							:disabled="scenario.id !== 'blank-canvas'"
						/>
						<label class="pl-2" :for="scenario.id">{{ scenario.displayName }}</label>
					</div>
				</aside>
				<main class="col-10 flex flex-column gap-3">
					<component v-if="getScenario()" :is="getScenario().component" :scenario="getScenario().instance" />
				</main>
			</div>
		</template>
		<template #footer>
			<Button label="Create" size="large" @click="saveWorkflow" />
			<Button label="Close" class="p-button-secondary" size="large" outlined @click="emit('close-modal')" />
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import TeraModal from '@/components/widgets/tera-modal.vue';
import Button from 'primevue/button';
import { ref } from 'vue';
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
import { BlankCanvasScenario } from '@/components/workflow/scenario-templates/blank-canvas/blank-canvas-scenario';
import { SituationalAwarenessScenario } from '@/components/workflow/scenario-templates/situational-awareness/situational-awareness-scenario';

interface ScenarioItem {
	displayName: string;
	id: string;
	instance: BaseScenario;
	component: Component;
}

const scenarios = ref<ScenarioItem[]>([
	{
		displayName: BlankCanvasScenario.templateName,
		id: BlankCanvasScenario.templateId,
		instance: new BlankCanvasScenario(),
		component: TeraBlankCanvasTemplate
	},
	{
		displayName: SituationalAwarenessScenario.templateName,
		id: SituationalAwarenessScenario.templateId,
		instance: new SituationalAwarenessScenario(),
		component: TeraSituationalAwarenessTemplate
	}
]);

const emit = defineEmits(['close-modal']);

const selectedTemplateId = ref<any>(scenarios.value[0].id);

const saveWorkflow = async () => {
	const scenario = getScenario();
	const wf = scenario.instance.createWorkflow();
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

	emit('close-modal');
};

const getScenario = () => scenarios.value.find((s) => s.id === selectedTemplateId.value) as ScenarioItem;
</script>
