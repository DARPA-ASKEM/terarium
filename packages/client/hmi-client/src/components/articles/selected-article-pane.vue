<template>
	<div class="selected-article-pane">
		<div class="add-selected-buttons">
			<dropdown-button
				:inner-button-label="'Add to a project'"
				:is-dropdown-left-aligned="true"
				:items="projectsNames"
				@item-selected="addAssetsToProject"
			/>
		</div>
		<div>Publisher: {{ selectedArticle.publisher }}</div>
		<div>Author: {{ selectedArticle.author.map((a) => a.name).join(', ') }}</div>
		<div>Abstract: {{ formatAbstract(selectedArticle) }}</div>
		<div>Journal: {{ selectedArticle.journal }}</div>
		<div>Doc ID:: {{ selectedArticle.gddid }}</div>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, PropType, ref } from 'vue';
import { PublicationAsset, XDDArticle } from '@/types/XDD';
import useResourcesStore from '@/stores/resources';
import { Project, PUBLICATIONS } from '@/types/Project';
import DropdownButton from '@/components/widgets/dropdown-button.vue';
import * as ProjectService from '@/services/project';
import { addPublication } from '@/services/external';

const props = defineProps({
	selectedArticle: {
		type: Object as PropType<XDDArticle>,
		required: true
	}
});

const emit = defineEmits(['close']);
const resources = useResourcesStore();

const validProject = computed(() => resources.activeProject);

const projectsList = ref<Project[]>([]);
const projectsNames = computed(() => projectsList.value.map((p) => p.name));

const addResourcesToProject = async (projectId: string) => {
	// send selected items to the store
	const body: PublicationAsset = {
		xdd_uri: props.selectedArticle.gddid,
		title: props.selectedArticle.title
	};

	// FIXME: handle cases where assets is already added to the project

	// first, insert into the proper table/collection
	const res = await addPublication(body);
	if (res) {
		const publicationId = res.id;

		// then, link and store in the project assets
		const assetsType = PUBLICATIONS;
		await ProjectService.addAsset(projectId, assetsType, publicationId);

		// update local copy of project assets
		validProject.value?.assets.publications.push(publicationId);
		resources.activeProjectAssets?.publications.push(body);
	}
};

// TODO: May need more formatting than just replcing <p></p> in future
const formatAbstract = (item: XDDArticle) =>
	item.abstract.replace('<p>', '\n').replace('</p>', '\n') || '[no abstract]';

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
.selected-article-pane {
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
