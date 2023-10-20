<template>
	<div class="selected-document-pane">
		<subheader>
			<p>Publisher: {{ selectedDocument.publisher }}</p>
			<p>Journal: {{ selectedDocument.journal }}</p>
			<p>Doc ID: {{ selectedDocument.gddId }}</p>
		</subheader>
		<div>
			Abstract:
			<p class="textblock" v-html="formatAbstract(selectedDocument)"></p>
		</div>
		<div class="add-selected-buttons">
			<dropdown
				placeholder="ADD TO PROJECT"
				class="p-button dropdown-button"
				:is-dropdown-left-aligned="false"
				:options="projectsNames"
				v-on:change="addAssetsToProject"
			/>
		</div>
	</div>
</template>

<script setup lang="ts">
import { computed, PropType } from 'vue';
import { AssetType, Document, ExternalPublication } from '@/types/Types';
import { addDocuments } from '@/services/external';
import dropdown from 'primevue/dropdown';
import { useProjects } from '@/composables/project';

const props = defineProps({
	selectedDocument: {
		type: Object as PropType<Document>,
		required: true
	}
});

const emit = defineEmits(['close']);

const projectsNames = computed(() => useProjects().allProjects?.value?.map((p) => p.name));

const addResourcesToProject = async (projectId: string) => {
	// send selected items to the store
	const body: ExternalPublication = {
		xdd_uri: props.selectedDocument.gddId,
		title: props.selectedDocument.title
	};

	// FIXME: handle cases where assets is already added to the project

	// first, insert into the proper table/collection
	const res = await addDocuments(body);
	if (res && useProjects().activeProject.value) {
		const documentId = res.id;

		// then, link and store in the project assets
		const assetsType = AssetType.Publications;
		await useProjects().addAsset(assetsType, documentId, projectId);
	}
};

const formatAbstract = (item: Document) =>
	item.abstractText !== undefined ? item.abstractText : '[no abstract]';

const addAssetsToProject = async (projectName) => {
	let projectId = '';
	if (projectName !== undefined && typeof projectName.value === 'string') {
		const project = useProjects().allProjects?.value?.find((p) => p.name === projectName.value);
		projectId = project?.id as string;
	} else {
		if (useProjects().activeProject.value) return;
		projectId = useProjects().activeProject.value!.id;
	}

	addResourcesToProject(projectId);

	emit('close');
};
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
	display: flex;
	flex-direction: row;
	justify-content: flex-end;
	align-items: center;
	padding: 16px 22px 16px 0px;
	gap: 8px;
}

.add-selected-buttons button {
	margin-bottom: 2px;
}

.textblock {
	padding: 0px 0px 5px 15px;
}

/* TODO: All of this dropdown styling has been used before. If all dropdowns are green with white text and rounded we should export this into a styling component likely */

.dropdown-button {
	width: 156px;
	height: 25px;
	border-radius: 6px;
}

subheader {
	color: var(--text-color-subdued);
	font-weight: 400;
	font-size: 12px;
	padding-bottom: 5px;
}

:deep(.p-dropdown .p-dropdown-label.p-placeholder) {
	display: contents;
	color: white;
	font-size: small;
}

:deep .p-dropdown .p-dropdown-trigger {
	color: white;
}
</style>
