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
		<!-- PrimeVue editor-container -->
		<section class="editor-container">
			<Editor v-model="editorContent" class="editor h-full"> </Editor>
		</section>
	</tera-asset>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Editor from 'primevue/editor';
import * as DateUtils from '@/utils/date';
import TeraAsset from '@/components/asset/tera-asset.vue';
import { useProjects } from '@/composables/project';
import TeraProjectMenu from '@/components/home/tera-project-menu.vue';

const isRenamingProject = ref(false);

const editorContent = ref(`
    <div>
        <h2>Hey there! </h2>
        <p>This is your project overview page. Use this space however you like. Not sure where to start? Here are some things you can try:</p>
		<br>
        <ul>
            <li><strong>Upload stuff:</strong> Upload documents, models, code or datasets with the green button in the bottom left corner.</li>
            <li><strong>Explore and add:</strong> Use the project selector in the top nav to switch to the Explorer where you can find documents, models and datasets that you can add to your project.</li>
            <li><strong>Build a model:</strong> Create a model that fits just what you need.</li>
            <li><strong>Create a workflow:</strong> Connect resources with operators so you can focus on the science and not the plumbing.</li>
        </ul>
		<br>
        <p>Feel free to erase this text and make it your own.</p>
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

.content-container {
	background-color: var(--surface-b);
}

.editor-container {
	display: flex;
	flex-direction: column;
	flex: 1;
}

.editor-container .p-editor .ql-container {
	height: 100%;
}

.editor-container:deep(.ql-editor) {
	height: calc(100vh - 240px);
	font-family: var(--font-family);
	font-size: var(--font-size);
}
/* Editor toolbar formatting */
:deep(.p-editor-container .p-editor-toolbar) {
	border-radius: 0px;
	border-color: var(--surface-border-light) !important;
	border-top: none !important;
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
