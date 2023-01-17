<script setup lang="ts">
import Modal from '@/components/Modal.vue';
import Button from 'primevue/button';
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
		<Button
			label="New Project"
			class="p-button-text p-button-plain"
			icon="pi pi-plus"
			@click="isModalVisible = true"
		/>
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
						<Button @click="createNewProject">Create Project</Button>
						<Button class="p-button-secondary" @click="isModalVisible = false">Cancel</Button>
					</footer>
				</template>
			</Modal>
		</Teleport>
	</div>
</template>

<style scoped>
.new-project-card {
	border: 1px solid var(--surface-border);
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
	width: 100%;
	height: 100%;
	justify-content: center;
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
<style>
.new-project-card > button.p-button .p-button-label {
	/* hack necessary because of making the button bigger to fill the card */
	flex: none;
}
</style>
