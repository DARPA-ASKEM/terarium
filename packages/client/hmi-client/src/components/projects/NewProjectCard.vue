<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconAdd32 from '@carbon/icons-vue/es/add/32';
import Modal from '@/components/Modal.vue';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import * as ProjectService from '@/services/project';

const router = useRouter();

const isModalVisible = ref(false);
const name = ref('');
const description = ref('');

async function createNewProject() {
	const project = await ProjectService.create(name.value, description.value);
	if (project) {
		router.push(`/projects/${project.id}`);
		isModalVisible.value = false;
	}
}
</script>

<template>
	<div class="new-project-card">
		<Button @click="isModalVisible = true">
			<IconAdd32 />
			New Project
		</Button>
		<Teleport to="body">
			<Modal v-if="isModalVisible" class="modal" @modal-mask-clicked="isModalVisible = false">
				<template #default>
					<form>
						<label for="new-project-name">Project Name</label>
						<InputText id="new-project-name" type="text" v-model="name" />

						<label for="new-project-description">Project Purpose</label>
						<Textarea id="new-project-description" rows="5" v-model="description" />
					</form>
				</template>
				<template #footer>
					<footer>
						<Button action @click="createNewProject">Create Project</Button>
						<Button @click="isModalVisible = false">Cancel</Button>
					</footer>
				</template>
			</Modal>
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
	transition: 0.2s;
	text-align: left;
}

.new-project-card > button {
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

.modal h4 {
	margin-bottom: 1em;
}

.modal label {
	display: block;
	margin-bottom: 0.5em;
}

.modal input,
.modal textarea {
	display: block;
	margin-bottom: 2rem;
	width: 100%;
}

.modal footer {
	display: flex;
	flex-direction: row-reverse;
	gap: 1rem;
	justify-content: end;
	margin-top: 2rem;
}
</style>
