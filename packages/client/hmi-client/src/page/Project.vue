<script setup lang="ts">
import { ProjectType } from '@/types/Project';
import { ref, nextTick } from 'vue';
import ResourcesList from '@/components/resources/resources-list.vue';
import InputText from 'primevue/inputtext';
import { update as updateProject } from '@/services/project';
import useResourcesStore from '@/stores/resources';
import Button from 'primevue/button';
import Menu from 'primevue/menu';

const props = defineProps<{
	project: ProjectType;
}>();

const resources = useResourcesStore();
const isEditingProject = ref(false);
const inputElement = ref<HTMLInputElement | null>(null);
const newProjectName = ref<string>('');

function formatTimeStamp(timestamp) {
	const formattedDate = new Date(timestamp).toLocaleDateString(undefined, {
		weekday: 'long',
		year: 'numeric',
		month: 'long',
		day: 'numeric'
	});
	return `Last updated ${formattedDate}`;
}

async function editProject() {
	newProjectName.value = props.project.name;
	isEditingProject.value = true;
	await nextTick();
	// @ts-ignore
	inputElement.value?.$el.focus();
}

async function updateProjectName() {
	isEditingProject.value = false;
	const updatedProject = props.project;
	updatedProject.name = newProjectName.value;
	const id = await updateProject(updatedProject);
	if (id) {
		resources.setActiveProject(updatedProject);
	}
}

const projectMenu = ref();
const projectMenuItems = ref([
	{
		label: 'Edit',
		command: editProject
	}
]);

function showProjectMenu(event) {
	projectMenu.value.toggle(event);
}
</script>

<template>
	<div class="flex-container">
		<header>
			<Button
				icon="pi pi-ellipsis-v"
				class="p-button-rounded menu-button"
				@click="showProjectMenu"
			/>
			<Menu ref="projectMenu" :model="projectMenuItems" :popup="true" />
			<InputText
				v-model="newProjectName"
				ref="inputElement"
				class="project-name-input"
				@keyup.enter="updateProjectName"
				:class="{ isVisible: isEditingProject }"
			>
			</InputText>
			<h3 :class="{ isVisible: !isEditingProject }">
				{{ project?.name }}
			</h3>

			<p class="secondary-text">{{ formatTimeStamp(project?.timestamp) }}</p>
		</header>
		<section class="content-container">
			<section class="summary">
				<!-- This div is so that child elements will automatically collapse margins -->
				<div>
					<!-- Author -->
					<section class="description">
						<p>
							{{ project?.description }}
						</p>
					</section>
					<section class="contributors">
						{{ project?.username }}
						<Button label="+ Add contributor" />
					</section>
				</div>
			</section>
			<section class="detail">
				<resources-list :project="project" />
			</section>
		</section>
	</div>
</template>

<style scoped>
.flex-container {
	display: flex;
	flex-direction: column;
	width: 100%;
	background: white;
}

a {
	text-decoration: underline;
}

.content-container {
	margin-left: 1rem;
	column-gap: 2rem;
	flex-direction: row;
}

header {
	margin: 1rem;
}

section {
	display: flex;
	width: 100%;
	flex-direction: column;
}

.summary {
	flex: 0.25;
}

.contributors {
	line-height: 1.75rem;
	margin: 0 0 1rem 0;
	align-items: flex-start;
}

.detail {
	flex: 0.75;
}

.summary,
.detail {
	margin: 1rem 0;
	display: block;
}

.summary section {
	margin-bottom: 1rem;
}

.related {
	font-weight: var(--font-weight-semibold);
}

h3 {
	font-size: 24px;
	margin-bottom: 1rem;
	display: inline;
	visibility: hidden;
}

.project-name-input {
	font-size: 24px;
	position: absolute;
	font-weight: 600;
	padding: 0 0 0 1rem;
	margin-left: -1rem;
	border: 0;
	visibility: hidden;
	width: 33%;
}

.secondary-text {
	color: var(--text-color-secondary);
}

.isVisible {
	visibility: visible;
}

.p-button,
.p-button:enabled:hover,
.p-button:enabled:focus {
	background-color: transparent;
	color: var(--text-color-secondary);
	padding: 0;
}

ul {
	list-style: none;
	display: inline-flex;
}

.item,
.item:enabled:hover,
.item:enabled:focus {
	background-color: var(--surface-secondary);
	color: var(--text-color-secondary);
	padding: 0 0.5rem 0 0.5rem;
	margin: 0.5rem;
}

.menu-button {
	position: absolute;
	right: 0;
}
</style>
