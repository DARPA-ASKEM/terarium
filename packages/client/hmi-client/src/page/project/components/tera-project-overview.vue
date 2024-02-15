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

const editorContent = ref(`
    <div>
        <h2>Hey there! </h2>
        <p>Welcome to your project overview page. Use this space to track progress, brainstorm, or lay out your project steps. Not sure where to start? Here are some things you can try:</p>
		<br>
        <ul>
            <li><strong>Upload stuff:</strong> Got documents or code? Upload them right into your project with the green button in the bottom left corner.</li>
            <li><strong>Explore and add:</strong> Dive into the Explorer to find documents, models, and datasets that you can add to your project.</li>
            <li><strong>Build a model:</strong> Do you have something specific in mind? Start a new model from scratch that fits just what you need.</li>
            <li><strong>Create a workflow:</strong> Design workflows to connect operators so you can focus on the science and not the coding and orchestration.</li>
        </ul>
		<br>
        <p>Feel free to erase this text and use this space however you like. Keep notes, outline your project, or whatever else helps you stay organized.</p>
    </div>
    `);
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
	height: calc(100vh - 344px);
	font-family: var(--font-family);
}

:deep(.ql-picker-label) {
	font-family: var(--font-family);
}

:deep(.ql-active) {
	background-color: var(--surface-highlight) !important;
	color: var(--text-color-subdued) !important;
}

:deep(.ql-active .ql-stroke) {
	stroke: var(--text-color-subdued) !important;
}
:deep(.ql-selected) {
	background-color: var(--surface-highlight) !important;
}
</style>
