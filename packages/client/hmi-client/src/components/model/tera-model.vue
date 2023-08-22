<template>
	<!--main is temporary just for css purposes-->
	<main>
		<tera-asset
			v-if="model"
			:name="model.name"
			:feature-config="featureConfig"
			:is-naming-asset="isNaming"
			:stretch-content="view === ModelView.MODEL"
		>
			<template #name-input>
				<!--@keyup.enter="updateModelName" this is being reworked in rename-dataset branch-->
				<InputText v-if="isNaming" v-model.lazy="newName" placeholder="Title of new model" />
			</template>
			<template #edit-buttons>
				<span class="p-buttonset">
					<Button
						class="p-button-secondary p-button-sm"
						label="Description"
						icon="pi pi-list"
						@click="view = ModelView.DESCRIPTION"
						:active="view === ModelView.DESCRIPTION"
					/>
					<Button
						class="p-button-secondary p-button-sm"
						label="Model"
						icon="pi pi-file"
						@click="view = ModelView.MODEL"
						:active="view === ModelView.MODEL"
					/>
					<Button
						class="p-button-secondary p-button-sm"
						label="Transform"
						icon="pi pi-sync"
						@click="view = ModelView.NOTEBOOK"
						:active="view === ModelView.NOTEBOOK"
					/>
				</span>
				<template v-if="!featureConfig.isPreview">
					<!--Disable this until rename-dataset is merged-->
					<Button
						disabled
						icon="pi pi-ellipsis-v"
						class="p-button-icon-only p-button-text p-button-rounded"
						@click="toggleOptionsMenu"
					/>
					<Menu ref="optionsMenu" :model="optionsMenuItems" :popup="true" />
				</template>
			</template>
			<tera-model-description v-if="view === ModelView.DESCRIPTION" :model="model" />
			<Accordion v-else-if="view === ModelView.MODEL" multiple :active-index="[0, 1, 2, 3]">
				<AccordionTab header="Model diagram">
					<tera-model-diagram
						:model="model"
						:is-editable="!featureConfig.isPreview"
						@update-model="updateModelContent"
					/>
				</AccordionTab>
				<AccordionTab header="Model equations"> </AccordionTab>
				<AccordionTab header="Model observables"></AccordionTab>
				<AccordionTab header="Model configurations"></AccordionTab>
			</Accordion>
		</tera-asset>
	</main>
</template>

<script setup lang="ts">
import { watch, PropType, ref, computed } from 'vue';
import { isEmpty } from 'lodash';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraModelDescription from '@/components/model/tera-model-description.vue';
import TeraModelDiagram from '@/components/model/tera-model-diagram.vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Menu from 'primevue/menu';
import { getModel, updateModel } from '@/services/model';
import { FeatureConfig } from '@/types/common';
import { IProject } from '@/types/Project';
import { Model } from '@/types/Types';

enum ModelView {
	DESCRIPTION,
	MODEL,
	NOTEBOOK
}

const props = defineProps({
	project: {
		type: Object as PropType<IProject> | null,
		default: null
	},
	assetId: {
		type: String,
		default: 'sir-model-id'
	},
	highlight: {
		type: String,
		default: ''
	},
	featureConfig: {
		type: Object as PropType<FeatureConfig>,
		default: { isPreview: false } as FeatureConfig
	}
});

const model = ref<Model | null>(null);
const view = ref(ModelView.DESCRIPTION);

const newName = ref('New Model');
const isRenaming = ref(false);

const isNaming = computed(() => isEmpty(props.assetId) || isRenaming.value);

const toggleOptionsMenu = (event) => {
	optionsMenu.value.toggle(event);
};

// User menu
const optionsMenu = ref();
const optionsMenuItems = ref([
	{
		icon: 'pi pi-pencil',
		label: 'Rename',
		command() {
			isRenaming.value = true;
			newName.value = model.value?.name ?? '';
		}
	}
	// { icon: 'pi pi-clone', label: 'Make a copy', command: initiateModelDuplication }
	// ,{ icon: 'pi pi-trash', label: 'Remove', command: deleteModel }
]);

// Centralize this eventually
function updateModelContent(updatedModel: Model) {
	model.value = updatedModel;
	updateModel(updatedModel);
}

watch(
	() => [props.assetId],
	async () => {
		// Reset view of model page
		isRenaming.value = false;
		view.value = ModelView.DESCRIPTION;
		model.value = !isEmpty(props.assetId) ? await getModel(props.assetId) : null;
	},
	{ immediate: true }
);
</script>

<style scoped>
/* main is temporary to mimic what tera-model is wrapped in */
main {
	display: grid;
	grid-template-rows: auto 1fr;
	height: 100%;
	width: 100%;
	background-color: var(--surface-section);
	overflow-y: auto;
	overflow-x: hidden;
}
</style>
