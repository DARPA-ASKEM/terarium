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
			<div class="warn" v-if="modelErrors.length > 0">
				Errors or warnings detected, please check individual sections below.
			</div>
			<div v-for="(err, idx) of modelErrors.filter((d) => d.type === 'model')" :key="idx">
				<div :class="err.severity">{{ err.content }}</div>
			</div>

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
import TeraAsset from '@/components/asset/tera-asset.vue';
import TeraAssetEnrichment from '@/components/widgets/tera-asset-enrichment.vue';
import TeraModelDescription from '@/components/model/petrinet/tera-model-description.vue';
import TeraPetrinetParts from '@/components/model/petrinet/tera-petrinet-parts.vue';
import TeraSaveAssetModal from '@/components/project/tera-save-asset-modal.vue';
import { getModel, updateModel, getMMT } from '@/services/model';
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
	updateTime
} from '@/model-representation/service';
import type { ModelError } from '@/model-representation/service';

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
	model.value = await getModel(props.assetId);
	temporaryModel.value = cloneDeep(model.value);
	modelErrors.value = checkPetrinetAMR(model.value as Model);

	await refreshMMT();
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
	},
	{ immediate: true }
);

defineExpose({ temporaryModel });
</script>

<style scoped>
.btn-group {
	display: flex;
	align-items: center;
	gap: var(--gap-2);
	margin-left: auto;
}

.warn {
	background-color: var(--surface-warning);
}

.error {
	background-color: var(--surface-error);
}
</style>
