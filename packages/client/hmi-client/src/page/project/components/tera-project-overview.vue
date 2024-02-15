<template>
	<tera-asset
		:name="useProjects().activeProject.value?.name"
		:authors="useProjects().activeProject.value?.userName"
		:is-naming-asset="isRenamingProject"
		:publisher="`Last updated ${DateUtils.formatLong(
			useProjects().activeProject.value?.updatedOn ?? useProjects().activeProject.value?.createdOn
		)}`"
		:is-loading="useProjects().projectLoading.value"
	>
		<template #edit-buttons>
			<tera-project-menu :project="useProjects().activeProject.value" />
		</template>
		<template #overview-summary>
			<!-- Description & Contributors -->
			<p class="mt-1 mb-1">
				{{ useProjects().activeProject.value?.description }}
			</p>
		</template>
		<section class="content-container">
			<!-- Quick link buttons go here -->
			<section class="quick-links">
				<Button
					label="New model"
					size="large"
					icon="pi pi-share-alt"
					severity="secondary"
					outlined
					@click="emit('open-new-asset', AssetType.Model)"
				/>
				<Button
					size="large"
					severity="secondary"
					outlined
					@click="emit('open-new-asset', AssetType.Workflow)"
				>
					<vue-feather
						class="p-button-icon-left"
						type="git-merge"
						size="1.25rem"
						stroke="rgb(16, 24, 40)"
					/>
					<span class="p-button-label">New workflow</span>
				</Button>
			</section>

			<!-- PrimeVue editor-container go here -->
			<section class="editor-container">
				<Editor v-model="editorContent" class="editor h-full"> </Editor>
			</section>
		</section>
	</tera-asset>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Editor from 'primevue/editor';
import Button from 'primevue/button';
import * as DateUtils from '@/utils/date';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { AssetType } from '@/types/Types';
import { useProjects } from '@/composables/project';
import TeraProjectMenu from '@/components/home/tera-project-menu.vue';

const emit = defineEmits(['open-asset', 'open-new-asset']);
const isRenamingProject = ref(false);

const editorContent = ref('<div>Hello World!</div><div>Jamie <b>is</b> cool</div><div><br></div>');
</script>

<style scoped>
main {
	overflow-y: auto;
	-ms-overflow-style: none;
	/* IE and Edge */
	scrollbar-width: none;
	/* Firefox */
}

main::-webkit-scrollbar {
	display: none;
}

a {
	text-decoration: underline;
}

.content-container {
	overflow-y: auto;
	padding: 1rem;
	background: var(--surface-0);
	display: flex;
	flex-direction: column;
}

.contributors {
	flex-direction: row;
	align-items: center;
	gap: 0.75rem;
}

.quick-links {
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	margin-top: 1rem;
	margin-bottom: 1rem;
	gap: 1rem;
}
.quick-links .p-button.p-button-secondary {
	width: 100%;
	font-size: 1rem;
}
/* Ensure the parent container fills the height of its parent or set a specific height */
.editor-container {
	display: flex;
	flex-direction: column;
	flex: 1;
}
.editor-container .p-editor .ql-container {
	height: 100%;
}
.editor-container:deep(.ql-editor) {
	height: calc(100vh - 347px);
	font-family: var(--font-family);
}
</style>
