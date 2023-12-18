<template>
	<nav>
		<router-link :to="RoutePath.Home">
			<img src="@assets/svg/terarium-logo.svg" height="30" alt="Terarium logo" />
		</router-link>
		<SplitButton
			v-if="active"
			:label="menuLabel"
			class="layout-project-selection"
			:model="navMenuItems"
			severity="secondary"
			outlined
			rounded
		/>
		<template v-if="active">
			<a target="_blank" rel="noopener noreferrer" @click="isAboutModalVisible = true">About</a>
			<a target="_blank" rel="noopener noreferrer" :href="documentation">Documentation</a>
			<Avatar :label="userInitials" class="avatar m-2" shape="circle" @click="showUserMenu" />
			<Menu ref="userMenu" :model="userMenuItems" :popup="true" />
			<Dialog header="Logout" v-model:visible="isLogoutDialog">
				<p>You will be returned to the login screen.</p>
				<template #footer>
					<Button label="Cancel" class="p-button-secondary" @click="closeLogoutDialog" />
					<Button label="Ok" @click="auth.logout" />
				</template>
			</Dialog>
		</template>
		<Teleport to="body">
			<tera-modal
				v-if="isEvaluationScenarioModalVisible"
				@modal-mask-clicked="isEvaluationScenarioModalVisible = false"
			>
				<template #header>
					<h4>Evaluation Scenario</h4>
				</template>
				<template #default>
					<form>
						<label for="evaluation-scenario-name">Scenario</label>
						<InputText id="evaluation-scenario-name" type="text" v-model="evaluationScenarioName" />

						<label for="evaluation-scenario-task">Task</label>
						<InputText
							id="evaluation-scenario-task"
							type="text"
							v-model="evaluationScenarioTask"
							placeholder="What is the scenario question?"
						/>

						<label for="evaluation-scenario-description">Description</label>
						<Textarea
							id="evaluation-scenario-description"
							rows="5"
							v-model="evaluationScenarioDescription"
							placeholder="Describe what you are working on"
						/>

						<label for="evaluation-scenario-notes">Notes</label>
						<Textarea id="evaluation-scenario-notes" rows="5" v-model="evaluationScenarioNotes" />

						<p>Status: {{ evaluationScenarioCurrentStatus }}</p>
						<p>Runtime: {{ evaluationScenarioRuntimeString }}</p>
					</form>
				</template>
				<template #footer>
					<Button class="p-button-secondary" @click="isEvaluationScenarioModalVisible = false"
						>Close</Button
					>
					<Button
						class="p-button-danger"
						v-if="
							evaluationScenarioCurrentStatus === EvaluationScenarioStatus.Started ||
							evaluationScenarioCurrentStatus === EvaluationScenarioStatus.Resumed ||
							evaluationScenarioCurrentStatus === EvaluationScenarioStatus.Paused
						"
						:disabled="!isEvaluationScenarioValid"
						@click="stopEvaluationScenario"
						>Stop</Button
					>
					<Button
						class="p-button-warning"
						v-if="
							evaluationScenarioCurrentStatus === EvaluationScenarioStatus.Started ||
							evaluationScenarioCurrentStatus === EvaluationScenarioStatus.Resumed
						"
						@click="pauseEvaluationScenario"
						>Pause</Button
					>
					<Button
						class="p-button-warning"
						v-if="evaluationScenarioCurrentStatus === EvaluationScenarioStatus.Paused"
						@click="resumeEvaluationScenario"
						>Resume</Button
					>
					<Button
						:disabled="!isEvaluationScenarioValid || evaluationScenarioCurrentStatus !== ''"
						@click="beginEvaluationScenario"
						>Begin</Button
					>
				</template>
			</tera-modal>
		</Teleport>
		<Teleport to="body">
			<tera-modal
				v-if="isAboutModalVisible"
				@modal-mask-clicked="isAboutModalVisible = false"
				@modal-enter-press="isAboutModalVisible = false"
			>
				<template #header>
					<h4>About Terarium</h4>
				</template>
				<article>
					<img
						src="@/assets/svg/terarium-logo.svg"
						alt="Terarium logo"
						class="about-terarium-logo"
					/>
					<p>
						Terarium is a comprehensive modeling and simulation platform designed to help
						researchers and analysts find models in academic literature, parameterize and calibrate
						them, run simulations to test a variety of scenarios, and analyze the results.
					</p>
				</article>
				<article>
					<img
						src="@/assets/svg/uncharted-logo-official.svg"
						alt="Uncharted Software logo"
						class="about-uncharted-logo"
					/>
					<p>
						Uncharted Software provides design, development and consulting services related to data
						visualization and analysis software.
					</p>
				</article>
				<template #footer>
					<div class="modal-footer">
						<p>&copy; Copyright Uncharted Software {{ new Date().getFullYear() }}</p>
						<Button class="p-button" @click="isAboutModalVisible = false">Close</Button>
					</div>
				</template>
			</tera-modal>
		</Teleport>
	</nav>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Avatar from 'primevue/avatar';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Menu from 'primevue/menu';
import { MenuItem } from 'primevue/menuitem';
import { RoutePath, useCurrentRoute } from '@/router/index';
import { RouteMetadata, RouteName } from '@/router/routes';
import useAuthStore from '@/stores/auth';
import InputText from 'primevue/inputtext';
import SplitButton from 'primevue/splitbutton';
import TeraModal from '@/components/widgets/tera-modal.vue';
import Textarea from 'primevue/textarea';
import * as EventService from '@/services/event';
import { EvaluationScenarioStatus, EventType } from '@/types/Types';
import API from '@/api/api';
import { useProjects } from '@/composables/project';

defineProps<{
	active: boolean;
}>();

const isAboutModalVisible = ref(false);

/*
 * Navigation Menu
 */
const router = useRouter();

const menuLabel = computed(() => {
	if (isDataExplorer.value) {
		return 'Explorer';
	}
	if (useProjects().activeProject.value) {
		return useProjects().activeProject.value?.name;
	}
	return 'Home';
});

/**
 * Evaluation scenario code
 */
const isEvaluationScenarioModalVisible = ref(false);
const evaluationScenarioName = ref('');
const evaluationScenarioTask = ref('');
const evaluationScenarioDescription = ref('');
const evaluationScenarioNotes = ref('');
const evaluationScenarioCurrentStatus = ref('');
const evaluationScenarioRuntimeMillis = ref(0);
let intervalId: number;

const evaluationScenarioRuntimeString = computed(() => {
	const h = Math.floor(evaluationScenarioRuntimeMillis.value / 1000 / 60 / 60);
	const m = Math.floor((evaluationScenarioRuntimeMillis.value / 1000 / 60 / 60 - h) * 60);
	const s = Math.floor(
		((evaluationScenarioRuntimeMillis.value / 1000 / 60 / 60 - h) * 60 - m) * 60
	);
	const hS = h < 10 ? `0${h}` : `${h}`;
	const mS = m < 10 ? `0${m}` : `${m}`;
	const sS = s < 10 ? `0${s}` : `${s}`;
	return `${hS}:${mS}:${sS}`;
});
const isEvaluationScenarioValid = computed(
	() =>
		evaluationScenarioName.value !== '' &&
		evaluationScenarioTask.value !== '' &&
		evaluationScenarioDescription.value !== ''
);

/**
 * Logs an event to the server to begin an evaluation. Additionally, persists the evaluation
 * model to local storage
 */
const beginEvaluationScenario = async () => {
	await EventService.create(
		EventType.EvaluationScenario,
		useProjects().activeProject.value?.id,
		JSON.stringify(getEvaluationScenarioData(EvaluationScenarioStatus.Started))
	);
	persistEvaluationScenario();
	await refreshEvaluationScenario();
	startEvaluationTimer();
	isEvaluationScenarioModalVisible.value = false;
};

/**
 * Logs an event to the server to stop an evaluation.  Clears the persisted model in local storage.
 */
const stopEvaluationScenario = async () => {
	await EventService.create(
		EventType.EvaluationScenario,
		useProjects().activeProject.value?.id,
		JSON.stringify(getEvaluationScenarioData(EvaluationScenarioStatus.Stopped))
	);
	clearEvaluationScenario();
	window.clearInterval(intervalId);
	isEvaluationScenarioModalVisible.value = false;
};

/**
 * Logs an event to the server to pause an evaluation.
 */
const pauseEvaluationScenario = async () => {
	await EventService.create(
		EventType.EvaluationScenario,
		useProjects().activeProject.value?.id,
		JSON.stringify(getEvaluationScenarioData(EvaluationScenarioStatus.Paused))
	);
	await refreshEvaluationScenario();
	window.clearInterval(intervalId);
	isEvaluationScenarioModalVisible.value = false;
};

const resumeEvaluationScenario = async () => {
	await EventService.create(
		EventType.EvaluationScenario,
		useProjects().activeProject.value?.id,
		JSON.stringify(getEvaluationScenarioData(EvaluationScenarioStatus.Resumed))
	);
	await refreshEvaluationScenario();
	startEvaluationTimer();
	isEvaluationScenarioModalVisible.value = false;
};

/**
 * Returns the evaluation metadata model for the given action
 * @param action	the action name to log
 */

const getEvaluationScenarioData = (action: string) => ({
	name: evaluationScenarioName.value,
	task: evaluationScenarioTask.value,
	description: evaluationScenarioDescription.value,
	notes: evaluationScenarioNotes.value,
	action
});

/**
 * Saves the model to local storage
 */
const persistEvaluationScenario = () => {
	window.localStorage.setItem('evaluationScenarioName', evaluationScenarioName.value);
	window.localStorage.setItem('evaluationScenarioTask', evaluationScenarioTask.value);
	window.localStorage.setItem('evaluationScenarioDescription', evaluationScenarioDescription.value);
	window.localStorage.setItem('evaluationScenarioNotes', evaluationScenarioNotes.value);
};

const refreshEvaluationScenario = async () => {
	evaluationScenarioCurrentStatus.value = (
		await API.get(`/evaluation/status?name=${evaluationScenarioName.value}`)
	).data;

	evaluationScenarioRuntimeMillis.value = (
		await API.get(`/evaluation/runtime?name=${evaluationScenarioName.value}`)
	).data;
};

const loadEvaluationScenario = async () => {
	evaluationScenarioName.value = window.localStorage.getItem('evaluationScenarioName') || '';
	evaluationScenarioTask.value = window.localStorage.getItem('evaluationScenarioTask') || '';
	evaluationScenarioDescription.value =
		window.localStorage.getItem('evaluationScenarioDescription') || '';
	evaluationScenarioNotes.value = window.localStorage.getItem('evaluationScenarioNotes') || '';

	if (evaluationScenarioName.value !== '') {
		await refreshEvaluationScenario();
	}
};

const startEvaluationTimer = () => {
	if (
		evaluationScenarioCurrentStatus.value === EvaluationScenarioStatus.Started ||
		evaluationScenarioCurrentStatus.value === EvaluationScenarioStatus.Resumed
	) {
		intervalId = window.setInterval(() => {
			evaluationScenarioRuntimeMillis.value += 1000;
		}, 1000);
	}
};

/**
 * Clears the model from local storage in memory
 */
const clearEvaluationScenario = () => {
	evaluationScenarioName.value = '';
	evaluationScenarioTask.value = '';
	evaluationScenarioDescription.value = '';
	evaluationScenarioNotes.value = '';
	evaluationScenarioCurrentStatus.value = '';
	evaluationScenarioRuntimeMillis.value = 0;
	persistEvaluationScenario();
};

const homeItem: MenuItem = {
	label: RouteMetadata[RouteName.Home].displayName,
	icon: RouteMetadata[RouteName.Home].icon,
	command: () => router.push(RoutePath.Home)
};
const explorerItem: MenuItem = {
	label: RouteMetadata[RouteName.DataExplorer].displayName,
	icon: RouteMetadata[RouteName.DataExplorer].icon,
	command: () => router.push(RoutePath.DataExplorer)
};
const navMenuItems = ref<MenuItem[]>([homeItem, explorerItem]);
const currentRoute = useCurrentRoute();
const isDataExplorer = computed(() => currentRoute.value.name === RouteName.DataExplorer);

/*
 * User Menu
 */
const auth = useAuthStore();
const userMenu = ref();
const isLogoutDialog = ref(false);
const userMenuItems = ref([
	{
		label: 'Evaluation Scenario',
		command: () => {
			isEvaluationScenarioModalVisible.value = true;
		}
	},
	{
		label: 'User Administration',
		command: () => {
			router.push(RoutePath.UserAdmin);
		},
		visible: auth.user?.roles.some((r) => r.name === 'ADMIN')
	},
	{
		label: 'Logout',
		command: () => {
			isLogoutDialog.value = true;
		}
	}
]);

onMounted(async () => {
	await loadEvaluationScenario();
	startEvaluationTimer();
});

const showUserMenu = (event) => {
	userMenu.value.toggle(event);
};

const userInitials = computed(() => auth.userInitials);

function closeLogoutDialog() {
	isLogoutDialog.value = false;
}

watch(
	() => useProjects().allProjects.value,
	() => {
		const items: MenuItem[] = [];
		useProjects().allProjects.value?.forEach((project) => {
			items.push({
				label: project.name,
				icon: 'pi pi-folder',
				command: () => router.push({ name: RouteName.Project, params: { projectId: project.id } })
			});
		});
		navMenuItems.value = [homeItem, explorerItem, ...items];
	},
	{ immediate: true }
);

const documentation = computed(() => {
	const host = window.location.hostname ?? 'localhost';
	if (host === 'localhost') {
		return '//localhost:8000';
	}
	const url = host.replace(/\bapp\b/g, 'documentation');
	return `https://${url}`;
});
</script>

<style scoped>
nav {
	align-items: center;
	background-color: var(--surface-section);
	border-bottom: 1px solid var(--surface-border-light);
	padding: 0.5rem 1rem;
	display: flex;
	gap: var(--gap-large);

	a,
	a:hover {
		text-decoration: none;
	}
}

.layout-project-selection {
	margin-right: auto;
}

.avatar {
	color: var(--text-color-subdued);
	background-color: var(--surface-ground);
	cursor: pointer;
}

.avatar:hover {
	color: var(--text-color);
	background-color: var(--surface-hover);
}

/* Split button
 * This needs to be into its own component
 */

:deep(.layout-project-selection.p-splitbutton .p-button:first-of-type) {
	border-top-right-radius: 0;
	border-bottom-right-radius: 0;
	border-right: 0 none;
	color: var(--text-color);
}

:deep(.layout-project-selection.p-splitbutton .p-button:last-of-type) {
	background-color: var(--surface-200);
	border-top-left-radius: 0;
	border-bottom-left-radius: 0;
	color: var(--text-color-light);
	padding: 0.714rem 0.4rem;
	width: calc(3rem + 1px);

	&:hover {
		background-color: var(--surface-50);
		color: var(--text-color);
	}
}

.modal-footer {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	width: 100%;
}

.about-terarium-logo {
	width: 20rem;
}

.about-uncharted-logo {
	width: 10rem;
	margin-top: 3rem;
}

.modal-footer {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	width: 100%;
}
</style>
