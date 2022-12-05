<template>
	<div class="breakdown-pane-container">
		<div class="add-selected-buttons">
			<dropdown-button
				v-if="selectedSearchItems.length > 0"
				:inner-button-label="'Add to a project'"
				:is-dropdown-left-aligned="false"
				:items="projectsNames"
				@item-selected="addAssetsToProject"
			/>
		</div>
		<div class="selected-items-container">
			<div v-for="(item, indx) in selectedSearchItems" class="selected-item" :key="`item-${indx}`">
				<div class="content">
					<div>Publisher: {{ item.publisher }}</div>
					<div>Author: {{ item.author.map((a) => a.name).join(', ') }}</div>
					<!-- TODO: May need more formatting than just replcing <p></p> in future -->
					<div>
						Abstract: {{item.abstract.replace( "
						<p>","\n" ).replace("</p>
						","\n") }}
					</div>
					<div>Journal: {{ item.journal }}</div>
					<div>Doc ID:: {{ item.gddid }}</div>
				</div>
			</div>
		</div>
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
	selectedSearchItems: {
		type: Array as PropType<XDDArticle[]>,
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
	props.selectedSearchItems.forEach(async (selectedItem) => {
		const body: PublicationAsset = {
			id: projectId,
			xdd_uri: selectedItem.gddid,
			title: selectedItem.title
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
	});
};

const addAssetsToProject = async (projectName?: string) => {
	if (props.selectedSearchItems.length === 0) return;

	let projectId = '';
	if (projectName !== undefined && typeof projectName === 'string') {
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
.invalid-project {
	background-color: gray;
	cursor: not-allowed;
}

.selected-title {
	margin-bottom: 5px;
	font-size: larger;
	text-align: center;
	font-weight: bold;
	color: var(--un-color-accent);
}

.add-selected-buttons {
	display: flex;
}

.add-selected-buttons button {
	margin-bottom: 5px;
	padding-top: 4px;
	padding-bottom: 4px;
}

.breakdown-pane-container {
	margin-bottom: 40px;
	min-height: 0;
	display: flex;
	flex-direction: column;
}

.selected-items-container {
	display: flex;
	flex-direction: column;
	overflow-y: auto;
	margin-top: 10px;
}

.selected-items-container .selected-item {
	padding: 5px;
	background: var(--un-color-white);
	margin-top: 1px;
}

.selected-items-container .item-header {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
}

.selected-items-container .item-header .item-title {
	font-weight: 500;
}

.selected-items-container .item-header .icon {
	margin-right: 5px;
}
</style>
