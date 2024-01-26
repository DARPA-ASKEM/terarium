<template>
	<tera-modal @modal-mask-clicked="emit('close-modal')" @modal-enter-press="applyConfiguration">
		<template #header>
			<h4>{{ modalTitle }}</h4>
		</template>
		<template #default>
			<section>
				<form @submit.prevent>
					<div>
						<label for="new-project-name">Project title</label>
						<Textarea
							id="new-project-name"
							rows="2"
							v-model="title"
							placeholder="What do you want to call your project?"
						/>
					</div>
					<div>
						<label for="new-project-description">Description</label>
						<Textarea
							id="new-project-description"
							rows="5"
							v-model="description"
							placeholder="Add a short description"
						/>
					</div>
					<div>
						<!--
							TODO: Disabled until there is a domain property for Project.
							This section might have similarities to tera-filter-bar.vue.
						-->
						<label>Domain</label>
						<span>
							<Dropdown placeholder="Select domain" disabled :style="{ width: '100%' }" />
							<Button disabled label="Suggest" text />
						</span>
					</div>
				</form>
			</section>
			<section class="select-thumbnail-panel">
				<label>Select a thumbnail image for your project</label>
				<!-- <div>
					<span class="p-input-icon-left">
						<i class="pi pi-search" />
						<InputText
							placeholder="Search images..."
							v-model="imageSearch"
							icon="pi pi-search"
							disabled
						/>
					</span>
				</div>
				<p>
					The concepts mentioned in your project title and description will be used to suggest
					relevant thumbnail images.
				</p> -->
				<ul>
					<li v-for="i in 12" :key="i">
						<img
							src="@assets/svg/terarium-icon-transparent.svg"
							:alt="`Thumbnail ${i}`"
							class="thumbnail-image"
						/>
					</li>
				</ul>
			</section>
		</template>
		<template #footer>
			<Button
				@click="applyConfiguration"
				:loading="isApplyingConfiguration"
				:label="isApplyingConfiguration ? 'Loading' : confirmText"
				size="large"
			/>
			<Button
				severity="secondary"
				outlined
				@click="emit('close-modal')"
				label="Cancel"
				size="large"
			/>
		</template>
	</tera-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import Dropdown from 'primevue/dropdown';
import Textarea from 'primevue/textarea';
import TeraModal from '@/components/widgets/tera-modal.vue';
import Button from 'primevue/button';
import { useProjects } from '@/composables/project';
import useAuthStore from '@/stores/auth';
import { cloneDeep } from 'lodash';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/routes';
import { Project } from '@/types/Types';

const props = defineProps<{
	confirmText: string;
	modalTitle: string;
	project: Project | null;
}>();

const emit = defineEmits(['close-modal']);

const auth = useAuthStore();
const router = useRouter();
const userId = auth.user?.id ?? '';

const title = ref(props.project?.name ?? '');
const description = ref(props.project?.description ?? '');
const isApplyingConfiguration = ref(false);

async function createProject() {
	isApplyingConfiguration.value = true;
	const project = await useProjects().create(title.value, description.value, userId);
	if (project?.id) {
		router.push({ name: RouteName.Project, params: { projectId: project.id } });
		emit('close-modal');
	}
}

async function updateProjectConfiguration() {
	if (props.project) {
		const updatedProject = cloneDeep(props.project);
		updatedProject.name = title.value;
		updatedProject.description = description.value;
		isApplyingConfiguration.value = true;
		await useProjects().update(updatedProject);
		emit('close-modal');
	}
}

function applyConfiguration() {
	if (props.project) updateProjectConfiguration();
	else createProject();
}
</script>

<style scoped>
:deep(.content) {
	display: flex;
	gap: 1rem;
}

section {
	flex: 1;
}

form {
	display: flex;
	flex-direction: column;
	width: 100%;
	gap: 1rem;
}

p {
	color: var(--text-color-subdued);
	font-size: var(--font-caption);
}

ul {
	margin-top: 1rem;
	display: grid;
	grid-template-columns: auto auto auto;
	gap: 0.5rem;
	list-style: none;
}

li {
	outline: 1px solid var(--surface-border-light);
	cursor: pointer;
}

img {
	height: 6.5rem;
	width: 100%;
}

.thumbnail-image {
	background-color: var(--surface-0);
}

:deep(.content span) {
	display: flex;
}

/*
Doesn't rely on the margin-bottom: 1rem rule in tera-modal.vue
Should probably switch everything to use gap (like here) at some point
*/
:deep(.content input),
:deep(.content textarea) {
	margin-bottom: 0;
}

.select-thumbnail-panel {
	background: var(--surface-50);
	padding: 1rem;
	border-radius: var(--border-radius);
	border: 1px solid var(--surface-border-light);
	max-height: 100%;
	overflow-y: scroll;
}
</style>
