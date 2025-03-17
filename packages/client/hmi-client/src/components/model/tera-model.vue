<template>
	<tera-asset
		v-bind="$attrs"
		:id="assetId"
		:is-loading="isModelLoading"
		:show-table-of-contents="!isWorkflow"
		:name="temporaryModel?.header.name"
		@rename="updateModelName"
	>
		<template #edit-buttons>
			<Button icon="pi pi-ellipsis-v" text rounded @click="toggleOptionsMenu" />
			<ContextMenu ref="optionsMenu" :model="optionsMenuItems" popup :pt="optionsMenuPt" />
			<aside class="btn-group">
				<tera-asset-enrichment
					v-if="!hideEnrichment"
					:asset-type="AssetType.Model"
					:assetId="assetId"
					@finished-job="fetchModel"
				/>
				<Button
					v-if="isSaveForReuse"
					label="Save for re-use"
					severity="secondary"
					outlined
					@click="onSaveForReUse"
					:disabled="!hasEditPermission"
				/>
				<Button v-else label="Save as" severity="secondary" outlined @click="onSaveAs" :disabled="!hasEditPermission" />
				<Button label="Save" @click="onSave" :disabled="!(hasChanged && hasEditPermission)" />
			</aside>
		</template>
		<section v-if="temporaryModel && mmtData">
			<Message class="mx-3" severity="warn" v-if="modelErrors.length > 0">
				Errors and/or warnings detected, please check individual sections below.
			</Message>
			<Message v-if="canModelBeSimplified" class="mx-3" severity="info">
				This model appears to have complex rate laws. It can lead to an combinatorial explosion if it is stratified.
				This can be simplified reducing the number of controllers by {{ numberOfControllersSimplifyReduces }}.
				<Button
					label="Save this simplified version as a new model."
					text
					size="small"
					class="save-simplified-button"
					@click="saveSimplifiedModel()"
				/>
			</Message>
			<tera-model-error-message
				class="mx-3"
				:modelErrors="modelErrors.filter(({ type }) => type === ModelErrorType.MODEL)"
			/>

			<tera-model-description :model="temporaryModel" :mmt-data="mmtData" @update-model="updateTemporaryModel" />
			<tera-petrinet-parts
				:model="temporaryModel"
				:mmt="mmtData.mmt"
				:mmt-params="mmtData.template_params"
				:feature-config="{ isPreview: false }"
				:model-errors="modelErrors"
				@update-state="(e: any) => onUpdateModelPart('state', e)"
				@update-parameter="(e: any) => onUpdateModelPart('parameter', e)"
				@update-observable="(e: any) => onUpdateModelPart('observable', e)"
				@update-transition="(e: any) => onUpdateModelPart('transition', e)"
				@update-time="(e: any) => onUpdateModelPart('time', e)"
			/>
		</section>
	</tera-asset>
	<tera-save-asset-modal
		:asset="temporaryModel"
		:asset-type="AssetType.Model"
		:initial-name="temporaryModel?.header.name"
		:is-visible="showSaveModal"
		:is-updating-asset="isSaveForReuse"
		:open-on-save="!isWorkflow"
		@close-modal="showSaveModal = false"
		@on-save="onModalSave"
		@on-update="onModalSave"
	/>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue';
import { cloneDeep, isEmpty, isEqual } from 'lodash';
import Button from 'primevue/button';
import ContextMenu from 'primevue/contextmenu';
import Message from 'primevue/message';
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraAssetEnrichment from '@/components/widgets/tera-asset-enrichment.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';
import TeraModelErrorMessage from '@/components/model/model-parts/tera-model-error-message.vue';
import TeraPetrinetParts from '@/components/model/petrinet/tera-petrinet-parts.vue';
import TeraSaveAssetModal from '@/components/project/tera-save-asset-modal.vue';
import { getModel, updateModel, getMMT, getSimplifyModel } from '@/services/model';
import { AssetType, type Model } from '@/types/Types';
import { useProjects } from '@/composables/project';
import { logger } from '@/utils/logger';
import { MMT } from '@/model-representation/mira/mira-common';
import {
	checkPetrinetAMR,
	updateState,
	updateParameter,
	updateObservable,
	updateTransition,
	updateTime,
	ModelErrorType
} from '@/model-representation/service';
import type { ModelError } from '@/model-representation/service';
import * as saveAssetService from '@/services/save-asset';

const props = defineProps({
	assetId: {
		type: String,
		default: ''
	},
	isWorkflow: {
		type: Boolean,
		default: false
	},
	isSaveForReuse: {
		type: Boolean,
		default: false
	},
	hideEnrichment: {
		type: Boolean,
		default: false
	}
});

const emit = defineEmits(['on-save']);

const model = ref<Model | null>(null);
const temporaryModel = ref<Model | null>(null);
const mmtData = ref<MMT | null>(null);

const isModelLoading = ref(false);
const showSaveModal = ref(false);
const hasChanged = computed(() => !isEqual(model.value, temporaryModel.value));
const hasEditPermission = useProjects().hasEditPermission();
const modelErrors = ref<ModelError[]>([]);
const numberOfControllersSimplifyReduces = ref<number>(0);
const canModelBeSimplified = computed(() => numberOfControllersSimplifyReduces.value > 0);

// Edit menu
async function onSave() {
	if (!hasEditPermission || !temporaryModel.value) return;
	await updateModel(temporaryModel.value);
	logger.info('Changes to the model has been saved.');
	await useProjects().refresh();
	await fetchModel();
}
function onSaveAs() {
	showSaveModal.value = true;
}
function onSaveForReUse() {
	showSaveModal.value = true;
}

async function checkSimplifyModel() {
	if (!model.value) return;
	const simplifyModelResponse = await getSimplifyModel(model.value);
	numberOfControllersSimplifyReduces.value = simplifyModelResponse.max_controller_decrease;
}

async function saveSimplifiedModel() {
	if (!model.value) return;
	// Note that this is cached so theres no need to save the entire amr as a ref or anything to save 1 call.
	const simplifyModelResponse = await getSimplifyModel(model.value);
	const newModel = simplifyModelResponse.amr;
	const newName = `${newModel.header.name} simplified`;
	newModel.header.name = newName;
	saveAssetService.saveAs(newModel, AssetType.Model);
}

// Save modal
function onModalSave(event: any) {
	showSaveModal.value = false;
	if (props.isWorkflow) {
		emit('on-save', event);
	}
	fetchModel();
}

// User menu
const toggleOptionsMenu = (event) => optionsMenu.value.toggle(event);
const optionsMenu = ref();
// TODO: Could be moved into tera-asset.vue
const optionsMenuItems = ref<any[]>([
	{
		icon: 'pi pi-download',
		label: 'Download',
		command: async () => {
			if (model.value) {
				const data = `text/json;charset=utf-8,${encodeURIComponent(JSON.stringify(model.value, null, 2))}`;
				const a = document.createElement('a');
				a.href = `data:${data}`;
				a.download = `${model.value.header.name ?? 'model'}.json`;
				a.innerHTML = 'download JSON';
				a.click();
				a.remove();
			}
		}
	}
]);
const optionsMenuPt = {
	submenu: {
		class: 'max-h-30rem overflow-y-scroll'
	}
};

async function updateModelName(name: string) {
	if (!temporaryModel.value) return;
	temporaryModel.value.header.name = name;
	onSave();
}

async function refreshMMT() {
	if (!temporaryModel.value) return;
	const response = await getMMT(temporaryModel.value);
	if (!response) return;
	mmtData.value = response;
}

function updateTemporaryModel(newModel: Model) {
	temporaryModel.value = cloneDeep(newModel);
}

function onUpdateModelPart(property: 'state' | 'parameter' | 'observable' | 'transition' | 'time', event: any) {
	if (!temporaryModel.value) return;
	const newModel = cloneDeep(temporaryModel.value);
	const { id, key, value } = event;
	switch (property) {
		case 'state':
			updateState(newModel, id, key, value);
			break;
		case 'parameter':
			updateParameter(newModel, id, key, value);
			break;
		case 'observable':
			updateObservable(newModel, id, key, value);
			break;
		case 'transition':
			updateTransition(newModel, id, key, value);
			break;
		case 'time':
			updateTime(newModel, key, value);
			break;
		default:
			break;
	}
	updateTemporaryModel(newModel);
}

async function fetchModel() {
	const fetchedModel = await getModel(props.assetId);
	if (fetchedModel) {
		model.value = fetchedModel;
		updateTemporaryModel(fetchedModel);
		modelErrors.value = await checkPetrinetAMR(fetchedModel);
		await refreshMMT();
	}
}

onMounted(async () => {
	const addProjectMenuItems = (await useProjects().getAllExceptActive()).map((project) => ({
		label: project.name,
		command: async () => {
			const response = await useProjects().addAsset(AssetType.Model, props.assetId, project.id);
			if (response) logger.info(`Added asset to ${project.name}`);
		}
	}));
	if (addProjectMenuItems.length === 0) return;
	optionsMenuItems.value.splice(1, 0, {
		icon: 'pi pi-plus',
		label: 'Add to project',
		items: addProjectMenuItems
	});
});

watch(
	() => props.assetId,
	async () => {
		// Reset view of model page
		model.value = null;
		temporaryModel.value = null;
		mmtData.value = null;
		if (!isEmpty(props.assetId)) {
			isModelLoading.value = true;
			await fetchModel();
			isModelLoading.value = false;
		}
		checkSimplifyModel();
	},
	{ immediate: true }
);

defineExpose({ temporaryModel });
</script>

<style scoped>
.save-simplified-button {
	/* color: blue; */
	display: contents;
}
.btn-group {
	display: flex;
	align-items: center;
	gap: var(--gap-2);
	margin-left: auto;
}
</style>
