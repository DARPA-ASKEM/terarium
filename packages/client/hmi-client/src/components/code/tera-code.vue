<template>
	<main>
		<header>
			<section class="header">
				<!-- <h4>{{ name }}</h4> -->
				<section class="name">
					<InputText v-model="codeName" class="name-input" />
				</section>
				<section class="buttons">
					<Button label="Save code" @click="saveCode" />
					<Button label="Extract model" @click="extractModel" />
				</section>
			</section>
		</header>
		<v-ace-editor
			v-model:value="codeText"
			@init="initialize"
			lang="python"
			theme="chrome"
			style="height: 100%; width: 100%"
			class="ace-editor"
		/>
		<Teleport to="body">
			<tera-modal
				v-if="isModalVisible && model"
				class="modal"
				@modal-mask-clicked="isModalVisible = false"
				@modal-enter-press="isModalVisible = false"
			>
				<template #header>
					<InputText v-model="modelName" class="name-input" />
				</template>
				<template #default>
					<tera-model-diagram :model="model" :is-editable="false" />
				</template>
				<template #footer>
					<Button label="Cancel" class="p-button-secondary" @click="isModalVisible = false" />
					<Button label="Save model" @click="saveModel" />
				</template>
			</tera-modal>
		</Teleport>
	</main>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { VAceEditor } from 'vue3-ace-editor';
import { VAceEditorInstance } from 'vue3-ace-editor/types';
import Button from 'primevue/button';
import {
	uploadCodeToProject,
	getCodeFileAsText,
	getCodeAsset,
	updateCodeAsset
} from '@/services/code';
import { useToastService } from '@/services/toast';
import { codeToAMR } from '@/services/knowledge';
import { AssetType, Code, Model } from '@/types/Types';
import TeraModal from '@/components/widgets/tera-modal.vue';
import InputText from 'primevue/inputtext';
import { createModel } from '@/services/model';
import { addAsset } from '@/services/project';
import router from '@/router';
import { RouteName } from '@/router/routes';
import useResourceStore from '@/stores/resources';
import TeraModelDiagram from '../model/petrinet/tera-model-diagram.vue';

const props = defineProps<{
	projectId: string;
	assetId: string;
}>();

const emit = defineEmits(['asset-loaded']);

const resourceStore = useResourceStore();
const toast = useToastService();

const codeName = ref('');
const codeText = ref('# Paste some code here');
const codeAsset = ref<Code>();
const editor = ref<VAceEditorInstance['_editor'] | null>(null);
const selectedText = ref('');
const progress = ref(0);
const isModalVisible = ref(false);
const model = ref<Model>();
const modelName = ref('');
/**
 * Editor initialization function
 * @param editorInstance	the Ace editor instance
 */
async function initialize(editorInstance) {
	editor.value = editorInstance;
	editorInstance.session.selection.on('changeSelection', onSelectedTextChange);
	editorInstance.setShowPrintMargin(false);
}

/**
 * Event handler for selected text change in the code editor
 */
function onSelectedTextChange() {
	selectedText.value = editor.value?.getSelectedText() ?? '';
}

async function saveCode() {
	const existingCode = resourceStore.activeProjectAssets?.code.find(
		(c) => c.name === codeName.value
	);
	if (existingCode?.id) {
		console.log(existingCode);
		console.log(codeText.value);
		const file = new File([codeText.value], codeName.value);
		const updatedCode = await updateCodeAsset(existingCode, file, progress);
		if (!updatedCode) {
			toast.error('', 'Unable to save file');
		} else {
			toast.success('', `File saved as ${codeName.value}`);
			codeAsset.value = updatedCode;
		}
		return updatedCode;
	}
	const file = new File([codeText.value], codeName.value);
	const newCodeAsset = await uploadCodeToProject(props.projectId, file, progress);
	if (!newCodeAsset) {
		toast.error('', 'Unable to save file');
	} else {
		toast.success('', `File saved as ${codeName.value}`);
		codeAsset.value = newCodeAsset;
		router.push({
			name: RouteName.ProjectRoute,
			params: {
				pageType: AssetType.Code,
				projectId: props.projectId,
				assetId: codeAsset.value.id
			}
		});
	}
	return newCodeAsset;
}

async function extractModel() {
	if (!codeAsset.value?.id) {
		const newCodeAsset = await saveCode();
		if (newCodeAsset && newCodeAsset.id) {
			const extractedAmr = await codeToAMR(newCodeAsset.id);
			if (extractedAmr) {
				model.value = extractedAmr as Model;
				modelName.value = extractedAmr.header.name;
				isModalVisible.value = true;
			}
		}
	}
}

async function saveModel() {
	// const modelToSave = {
	// 	name: modelName.value,
	// 	description: '',
	// 	schema:
	// 		'https://raw.githubusercontent.com/DARPA-ASKEM/Model-Representations/petrinet_v0.5/petrinet/petrinet_schema.json',
	// 	schema_name: 'petrinet',
	// 	model_version: '0.1',
	// 	model: cloneDeep(model.value?.model),
	// 	semantics: {
	// 		ode: {
	// 			rates: [],
	// 			initials: [],
	// 			parameters: []
	// 		}
	// 	}
	// };
	const modelToSave = model.value;
	const createdModel = await createModel(modelToSave);
	if (createdModel) {
		const { id } = await addAsset(props.projectId, AssetType.Models, createdModel.id);
		if (id) {
			router.push({
				name: RouteName.ProjectRoute,
				params: {
					pageType: AssetType.Models,
					projectId: props.projectId,
					assetId: createdModel.id
				}
			});
		} else {
			toast.error('', 'Unable to add model to project');
		}
	} else {
		toast.error('', 'Unable to save model');
	}
}

watch(
	() => props.assetId,
	async () => {
		if (props.assetId !== 'code') {
			// FIXME: assetId is 'code' for a newly opened code asset; a hack to get around some weird tab behaviour
			const code = await getCodeAsset(props.assetId);
			if (code) {
				codeAsset.value = code;
				codeName.value = code.name;
				const text = await getCodeFileAsText(props.assetId, code.name);
				if (text) {
					codeText.value = text;
				}
			} else {
				codeName.value = 'newcode.py';
			}
		} else {
			codeName.value = 'newcode.py';
		}
		emit('asset-loaded');
	},
	{ immediate: true }
);
</script>

<style scoped>
header {
	padding: 0.5rem 1rem;
	background-color: var(--surface-section);
}

header > section {
	display: flex;
	justify-content: space-between;
	flex-direction: row;
}

main {
	height: 100%;
}

h4 {
	margin-top: 0.25rem;
}

.buttons {
	display: flex;
	gap: 0.5rem;
}

.ace-editor {
	border-top: 1px solid var(--surface-border-light);
}

.header {
	display: flex;
	gap: 1rem;
}

.name {
	flex-grow: 2;
}

.name-input {
	height: 2.5rem;
	align-self: center;
	font-size: 20px;
	font-weight: var(--font-weight-semibold);
	width: 100%;
}
</style>
