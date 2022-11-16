<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconAddFilled32 from '@carbon/icons-vue/es/add--filled/32';
import Modal from '@/components/Modal.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';

const isModalVisible = ref(false);
const newProjectTitle = ref('New Project');
const router = useRouter();

function postProject(name: string) {
	// Placeholder implementation - replace with api call
	return {
		id: '3',
		name,
		description: 'This is a new project'
	};
}

function createNewProject() {
	isModalVisible.value = false;
	const { id } = postProject(newProjectTitle.value);
	const routeLocation = router.resolve({ path: `/${id}` });
	window.open(routeLocation.href, '_blank');
}
</script>

<template>
	<div class="new-project-card">
		<Button @click="isModalVisible = true">Create New Project
			<IconAddFilled32 />
		</Button>
		<Teleport to="body">
			<modal :isVisible="isModalVisible">
				<template #header>
					<h3>Create a new project</h3>
				</template>
				<template #body>
					<label for="input">Project title</label>
					<input v-model="newProjectTitle" placeHolder="New Project" />
					<button class="modal-button" type="submit" @click="createNewProject">OK</button>
				</template>
			</modal>
		</Teleport>
	</div>
</template>

<style scoped>
.new-project-card {
	border: 1px solid var(--un-color-body-stroke);
	background-color: var(--un-color-body-surface-primary);
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
	height: 15rem;
	min-width: 20rem;
	border-radius: 0.5rem;
	margin: 0.5rem;
	transition: 0.2s;
	text-align: left;
}

.new-project-card button {
	font-size: 1.25rem;
	font-weight: 500;
	border-radius: 0.5rem;
	width: 85%;
	margin: auto;
	justify-content: center;
	cursor: pointer;
	background-color: transparent;
	color: var(--un-color-body-text-disabled);
	box-shadow: none;
}

.new-project-card button:hover {
	background-color: var(--un-color-body-surface-secondary);
	color: var(--un-color-body-text-secondary);
}

.new-project-card button svg {
	color: var(--un-color-body-text-disabled);
}

.new-project-card button:hover svg {
	color: var(--un-color-body-text-secondary);
}

h3 {
	font: var(--un-font-h3);
	color: var(--un-color-body-text-secondary);
}

label {
	display: block;
}

input {
	margin: 0.5rem;
	margin-left: 0;
	width: 50vh;
}
</style>
