<template>
	<Teleport to="body">
		<tera-modal
			v-if="isVisible"
			class="save-as-dialog"
			@modal-mask-clicked="emit('close-modal')"
			@on-modal-open="initializeAsset"
			@modal-enter-press="save"
		>
			<template #header>
				<h4>{{ title }}</h4>
			</template>
			<template #default>
				<form @submit.prevent>
					<label for="new-name">What would you like to call it?</label>
					<InputText
						id="new-name"
						type="text"
						v-model="newName"
						placeholder="Enter a unique name"
					/>
					<slot name="extra-input-fields" />
				</form>
			</template>
			<template #footer>
				<Button label="Save" size="large" @click="save" />
				<Button label="Cancel" class="p-button-secondary" size="large" outlined @click="cancel" />
			</template>
		</tera-modal>
	</Teleport>
</template>

<script setup lang="ts">
import { ref, PropType, computed } from 'vue';
import { cloneDeep, isEmpty } from 'lodash';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { AssetType, ProgrammingLanguage } from '@/types/Types';
import type { Model, Code } from '@/types/Types';
import type { Workflow } from '@/types/workflow';
import { emptyWorkflow } from '@/services/workflow';
import { setFileExtension } from '@/services/code';
import { useProjects } from '@/composables/project';
import { newAMR } from '@/model-representation/petrinet/petrinet-service';
import * as saveAssetService from '@/services/save-asset';

const props = defineProps({
	initialName: {
		type: String,
		default: ''
	},
	isVisible: {
		type: Boolean,
		default: false
	},
	asset: {
		type: Object as PropType<Model> | Object as
			| PropType<Workflow>
			| Object as PropType<Code> | null,
		default: null
	},
	assetType: {
		type: String as PropType<AssetType>,
		default: AssetType.Model
	},
	openOnSave: {
		type: Boolean,
		default: false
	},
	isOverwriting: {
		type: Boolean,
		default: false
	}
});

const emit = defineEmits(['close-modal', 'on-save']);

let newAsset: any = null;
const newName = ref<string>(props.initialName);

const title = computed(() => {
	if (!props.asset) return `Create new ${props.assetType}`;
	if (props.isOverwriting) return `Update ${props.assetType} name`;
	return `Save as a new ${props.assetType}`;
});

function onSave(data: any) {
	emit('on-save', data);
}

function cancel() {
	newName.value = '';
	emit('close-modal');
}

// Generic save function
function save() {
	// Prepare the asset with the new name
	switch (props.assetType) {
		case AssetType.Model:
			(newAsset as Model).header.name = newName.value;
			break;
		case AssetType.Workflow:
			(newAsset as Workflow).name = newName.value;
			break;
		default:
			break;
	}

	// Save method
	if (props.isOverwriting) {
		saveAssetService.overwrite(newAsset, props.assetType, onSave);
	} else {
		saveAssetService.saveAs(newAsset, props.assetType, props.openOnSave, onSave);
	}

	emit('close-modal');
}

function initializeAsset() {
	// If an asset is passed, clone it for saving
	if (props.asset) {
		newAsset = cloneDeep(props.asset);
		return;
	}

	// Creates an empty version of the asset if there no asset passed
	switch (props.assetType) {
		case AssetType.Model:
			newAsset = newAMR() as Model;
			break;
		case AssetType.Workflow:
			if (isEmpty(newName.value)) {
				const workflows = useProjects().getActiveProjectAssets(AssetType.Workflow);
				newName.value = `workflow ${workflows.length + 1}`;
			}
			newAsset = emptyWorkflow();
			break;
		case AssetType.Code:
			newName.value = setFileExtension('untitled', ProgrammingLanguage.Python);
			newAsset = new File([''], newName.value);
			break;
		default:
			break;
	}
}
</script>

<style scoped>
.save-as-dialog:deep(section) {
	width: 40rem;
}

form {
	margin-top: var(--gap);
}
</style>
