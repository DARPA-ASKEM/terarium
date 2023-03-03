<template>
	<div class="selected-document-pane">
		<div class="add-selected-buttons">
			<dropdown-button
				:inner-button-label="'Add to a project'"
				:is-dropdown-left-aligned="true"
				:items="projectsNames"
				@item-selected="addAssetsToProject"
			/>
		</div>
		<div>Publisher: {{ selectedDocument.publisher }}</div>
		<div>Author: {{ selectedDocument.author.map((a) => a.name).join(', ') }}</div>
		<div v-html="formatAbstract(selectedDocument)"></div>
		<div>Journal: {{ selectedDocument.journal }}</div>
		<div>Doc ID:: {{ selectedDocument.gddId }}</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, PropType, ref } from 'vue';
import { DocumentAsset, DocumentType } from '@/types/Document';
import useResourcesStore from '@/stores/resources';
import { IProject } from '@/types/Project';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import * as ProjectService from '@/services/project';
import { addDocuments } from '@/services/external';
import { AssetType } from '@/types/common';

const props = defineProps({
	selectedDocument: {
		type: Object as PropType<DocumentType>,
		required: true
	}
});

const emit = defineEmits(['close']);
const resources = useResourcesStore();

const validProject = computed(() => resources.activeProject);

const projectsList = ref<IProject[]>([]);
const projectsNames = computed(() => projectsList.value.map((p) => p.name));

const addResourcesToProject = async (projectId: string) => {
	// send selected items to the store
	const body: DocumentAsset = {
		xdd_uri: props.selectedDocument.gddId,
		title: props.selectedDocument.title
	};

	// FIXME: handle cases where assets is already added to the project

	// first, insert into the proper table/collection
	const res = await addDocuments(body);
	if (res) {
		const documentId = res.id;

		// then, link and store in the project assets
		const assetsType = AssetType.DOCUMENT;
		await ProjectService.addAsset(projectId, assetsType, documentId);

		// update local copy of project assets
		validProject.value?.assets?.[AssetType.DOCUMENT].push(documentId);
		resources.activeProjectAssets?.[AssetType.DOCUMENT].push(body);
	}
};

const formatAbstract = (item: DocumentType) =>
	item.abstract !== undefined ? `Abstract: ${item.abstract}` : 'Abstract: [no abstract]';

const addAssetsToProject = async (projectName?: string) => {
	let projectId = '';
	if (projectName !== undefined) {
		const project = projectsList.value.find((p) => p.name === projectName);
		projectId = project?.id as string;
	} else {
		if (!validProject.value) return;
		projectId = validProject.value.id;
	}

	addResourcesToProject(projectId);

	emit('close');
};

onMounted(async () => {
	const all = await ProjectService.getAll();
	if (all !== null) {
		projectsList.value = all;
	}
});
</script>

<style scoped>
.selected-document-pane {
	min-height: 0;
	display: flex;
	flex-direction: column;
	overflow-y: auto;
	/* HACK: Ensure the pane is at least as long as the dropdown-button's list can be so the list isn't clipped at the bottom. */
	padding-bottom: 300px;
}

.add-selected-buttons {
	display: flex;
	margin-bottom: 2rem;
}

.add-selected-buttons button {
	margin-bottom: 2px;
}
</style>
