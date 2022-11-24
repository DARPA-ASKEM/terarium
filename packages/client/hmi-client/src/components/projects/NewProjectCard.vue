<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconAddFilled32 from '@carbon/icons-vue/es/add--filled/32';
import Modal from '@/components/Modal.vue';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import API from '@/api/api';

const isModalVisible = ref(false);
const name = ref('New Project');
const description = ref('');
const router = useRouter();

// refacator as a composable?
// eslint-disable-next-line @typescript-eslint/no-shadow
async function postProject(name: string, description: string) {
	return API.post('/projects', {
		name,
		description
	});
}

function createNewProject() {
	postProject(name.value, description.value).then((response) => {
		const { status, data } = response;
		if (status === 201) {
			router.push(`/projects/${data.id}`);
			isModalVisible.value = false;
		}
	});
}
</script>

<template>
	<div class="new-project-card">
		<Button @click="isModalVisible = true"
			>Create New Project
			<IconAddFilled32 />
		</Button>
		<Teleport to="body">
			<modal v-if="isModalVisible" @modal-mask-clicked="isModalVisible = false">
				<template #header>
					<h3>Create a new project</h3>
				</template>
				<template #default>
					<label for="input">Project title</label>
					<input v-model="name" placeHolder="New Project" />
					<label for="input">Description</label>
					<input v-model="description" />
				</template>
				<template #footer>
					<Button class="modal-button" @click="createNewProject">OK</Button>
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
	width: 100%;
	height: 100%;
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
	width: 25vw;
}

.modal-button {
	width: 25%;
	background-color: var(--un-color-accent);
}
</style>
