<template>
	<Button
		label="Import"
		class="p-button-sm p-button-outlined"
		icon="pi pi-cloud-download"
		@click="openCodeBrowser"
	/>
	<Teleport to="body">
		<modal v-if="isModalVisible" class="modal" @modal-mask-clicked="isModalVisible = false">
			<template #header>
				<h5>Choose file to open from {{ chosenRepositoryName }}</h5>
			</template>
			<template #default>
				<ul>
					<li v-if="isInFolder" @click="openContent()">
						<i class="pi pi-folder-open" />
						<b> ..</b>
					</li>
					<li v-for="(content, index) in filesToSelect" :key="index" @click="openContent(content)">
						<i v-if="content.download_url === null" class="pi pi-folder" />
						<i v-else class="pi pi-file" />
						{{ content.name }}
					</li>
				</ul>
			</template>
		</modal>
	</Teleport>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import modal from '@/components/widgets/Modal.vue';
import { ProjectAssetTypes } from '@/types/Project';
import {
	getGithubRepositoryAttributes,
	getGithubRepositoryContent,
	getGithubCode
} from '@/services/github-import';

const props = defineProps<{
	urlString: string;
}>();

const emit = defineEmits(['open-asset']);

const chosenRepositoryName = ref('');
const filesToSelect = ref();
const isModalVisible = ref(false);

const isInFolder = computed(() => chosenRepositoryName.value.split('/').length > 2);

async function openCodeBrowser() {
	isModalVisible.value = true;

	const splitUrl = props.urlString.split('/');
	chosenRepositoryName.value = `${splitUrl[splitUrl.length - 2]}/${splitUrl[splitUrl.length - 1]}`;

	const repoAttributes = await getGithubRepositoryAttributes(chosenRepositoryName.value);
	filesToSelect.value = await getGithubRepositoryContent(repoAttributes.contents_url.slice(0, -8));
}

// Content as in file or folder
async function openContent(content?) {
	// Go to parent folder
	if (!content) {
		const directoryPathArray = chosenRepositoryName.value.split('/');
		directoryPathArray.pop();
		chosenRepositoryName.value = directoryPathArray.join('/');

		console.log(chosenRepositoryName.value);
		const repoAttributes = await getGithubRepositoryAttributes(chosenRepositoryName.value);
		console.log(repoAttributes);
		filesToSelect.value = await getGithubRepositoryContent(
			repoAttributes.contents_url.slice(0, -8)
		);
		return;
	}

	console.log(content);

	// If a folder is chosen
	if (content.download_url === null) {
		chosenRepositoryName.value = `${chosenRepositoryName.value}/${content.name}`;
		filesToSelect.value = await getGithubRepositoryContent(content.url);
		return;
	}

	// Open file in code view
	emit(
		'open-asset',
		{ assetName: 'New file', assetType: ProjectAssetTypes.CODE },
		// { assetName: url.name, assetId: url.name, assetType: ProjectAssetTypes.CODE }, // A new code asset would have to be created for this to work - leaving that for another issue
		await getGithubCode(content.download_url)
	);
}
</script>

<style scoped>
ul {
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
	min-width: 40vw;
	height: 50vh;
	overflow-y: auto;
}

ul li {
	padding: 0.25rem;
	cursor: pointer;
	border-radius: 0.5rem;
}

ul li:hover {
	background-color: var(--surface-hover);
}
</style>
