<template>
	<Button
		label="Import"
		class="p-button-sm p-button-outlined"
		icon="pi pi-cloud-download"
		@click="openCodeBrowser(url)"
	/>
	<Teleport to="body">
		<modal v-if="isModalVisible" class="modal" @modal-mask-clicked="isModalVisible = false">
			<template #header>
				<h5>Choose file to open from {{ chosenRepositoryName }}</h5>
			</template>
			<template #default>
				<ul class="repository-content">
					<li v-for="(content, index) in filesToSelect" :key="index" @click="openCode(content)">
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
import { ref } from 'vue';
import Button from 'primevue/button';
import modal from '@/components/widgets/Modal.vue';
import { ProjectAssetTypes } from '@/types/Project';
import { getGithubRepositoryContent, getGithubCode } from '@/services/github-import';

defineProps<{
	url: Object;
}>();

const emit = defineEmits(['open-asset']);

const chosenRepositoryName = ref('');
const filesToSelect = ref();
const isModalVisible = ref(false);

async function openCodeBrowser(url) {
	isModalVisible.value = true;
	chosenRepositoryName.value = url.full_name;
	filesToSelect.value = await getGithubRepositoryContent(url.contents_url.slice(0, -8));
}

async function openCode(url) {
	if (url.download_url === null) {
		chosenRepositoryName.value = `${chosenRepositoryName.value}/${url.name}`;
		filesToSelect.value = await getGithubRepositoryContent(url.url);
		return;
	}

	emit(
		'open-asset',
		{ assetName: 'New file', assetType: ProjectAssetTypes.CODE },
		// { assetName: url.name, assetId: url.name, assetType: ProjectAssetTypes.CODE }, // A new code asset would have to be created for this to work - leaving that for another issue
		await getGithubCode(url.download_url)
	);
}
</script>

<style>
.repository-content {
	list-style: none;
	display: flex;
	flex-direction: column;
	gap: 0.25rem;
	margin-top: 1rem;
}

.repository-content li {
	padding: 0.25rem;
	cursor: pointer;
	border-radius: 0.5rem;
}

.repository-content li:hover {
	background-color: var(--surface-hover);
}
</style>
