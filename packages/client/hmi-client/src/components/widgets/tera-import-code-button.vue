<template>
	<main>
		<Button
			label="Import"
			class="p-button-sm p-button-outlined"
			icon="pi pi-cloud-download"
			@click="initializeCodeBrowser"
		/>
		<a :href="urlString" rel="noreferrer noopener">{{ urlString }}</a>
		<Teleport to="body">
			<tera-modal v-if="isModalVisible" class="modal" @modal-mask-clicked="isModalVisible = false">
				<template #header>
					<h5>
						Choose file to open from {{ repoOwnerAndName
						}}<template v-if="isInDirectory">/{{ currentDirectory }}</template>
					</h5>
				</template>
				<template #default>
					<ul>
						<li v-if="isInDirectory" @click="openContent()">
							<i class="pi pi-folder-open" />
							<b> ..</b>
						</li>
						<li
							v-for="(content, index) in directoryContent"
							:key="index"
							@click="openContent(content)"
						>
							<i v-if="content.type === 'dir'" class="pi pi-folder" />
							<i v-else class="pi pi-file" />
							{{ content.name }}
						</li>
					</ul>
				</template>
			</tera-modal>
		</Teleport>
	</main>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import Button from 'primevue/button';
import TeraModal from '@/components/widgets/tera-modal.vue';
import { ProjectAssetTypes } from '@/types/Project';
import { isEmpty } from 'lodash';
import { getGithubRepositoryContent, getGithubCode } from '@/services/github-import';

const props = defineProps<{
	urlString: string;
}>();

const emit = defineEmits(['open-code']);

const repoOwnerAndName = ref('');
const currentDirectory = ref('');
const directoryContent = ref();
const isModalVisible = ref(false);

const isInDirectory = computed(() => !isEmpty(currentDirectory.value));

async function initializeCodeBrowser() {
	currentDirectory.value = ''; // Goes back to root directory if modal is closed then opened again
	isModalVisible.value = true;
	repoOwnerAndName.value = new URL(props.urlString).pathname.substring(1); // owner/repo
	directoryContent.value = await getGithubRepositoryContent(
		repoOwnerAndName.value,
		currentDirectory.value
	);
}

// Content as in file or directory
async function openContent(content?) {
	// Go to parent directory
	if (!content) {
		const directoryPathArray = currentDirectory.value.split('/');
		directoryPathArray.pop();
		currentDirectory.value = directoryPathArray.join('/');
		directoryContent.value = await getGithubRepositoryContent(
			repoOwnerAndName.value,
			currentDirectory.value
		);
		return;
	}

	// Open directory
	if (content.type === 'dir') {
		currentDirectory.value = content.path;
		directoryContent.value = await getGithubRepositoryContent(
			repoOwnerAndName.value,
			currentDirectory.value
		);
		return;
	}

	// Open file in code view
	const code = await getGithubCode(repoOwnerAndName.value, content.path);

	// Will be pasted into the code editor
	emit(
		'open-code',
		{ assetName: 'New file', assetType: ProjectAssetTypes.CODE, assetId: undefined },
		// { assetName: url.name, assetId: url.name, assetType: ProjectAssetTypes.CODE }, // A new code asset would have to be created for this to work - leaving that for another issue
		code
	);
}
</script>

<style scoped>
main {
	display: flex;
	align-items: center;
	gap: 0.5rem;
}

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
