<template>
	<nav>
		<router-link :to="RoutePath.Home" class="terarium-logo">
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
		<aside
			v-if="evaluationScenarioCurrentStatus === EvaluationScenarioStatus.Started"
			class="evaluation-scenario-widget"
		>
			{{ evaluationScenario.name }} &ndash; {{ evaluationScenarioTask.task }} ({{
				evaluationScenarioMultipleUsers ? 'Multiple Users' : 'Single User'
			}})
			<span class="evaluation-scenario-widget-timer">{{ evaluationScenarioRuntimeString }}</span>
			<Button
				v-if="evaluationScenarioCurrentStatus === EvaluationScenarioStatus.Started"
				label="Stop"
				rounded
				size="small"
				severity="primary"
				@click="stopEvaluationScenario"
				class="shadow-2"
			/>
			<Button v-else label="Start" rounded size="small" severity="primary" @click="beginEvaluationScenario" />
		</aside>
		<template v-if="active">
			<a target="_blank" rel="noopener noreferrer" @click="isAboutModalVisible = true">About</a>
			<a target="_blank" rel="noopener noreferrer" :href="documentationUrl">Help</a>
			<tera-notification-panel />

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
		<tera-modal
			v-if="isEvaluationScenarioModalVisible"
			@modal-mask-clicked="isEvaluationScenarioModalVisible = false"
			class="w-7"
		>
			<template #header>
				<div class="flex w-full justify-content-between align-items-center">
					<h4>Evaluation scenario</h4>
					<div>
						<span class="text-sm">Status</span
						><span class="ml-2 status-chip">{{
							evaluationScenarioCurrentStatus ? evaluationScenarioCurrentStatus : 'Ready to start'
						}}</span>
					</div>
				</div>
			</template>
			<template #default>
				<form>
					<label class="text-sm" for="evaluation-scenario-name">Scenario</label>
					<Dropdown
						id="evaluation-scenario-name"
						v-model="evaluationScenario"
						:options="evalScenarios.scenarios"
						optionLabel="name"
						placeholder="Select a Scenario"
						@change="onScenarioChange"
					/>

					<label class="text-sm mt-3" for="evaluation-scenario-task">Task</label>
					<Dropdown
						id="evaluation-scenario-task"
						:options="evaluationScenario?.questions ?? []"
						v-model="evaluationScenarioTask"
						optionLabel="task"
						placeholder="Select a Task"
						@change="onTaskChange"
					/>

					<label class="text-sm mt-3" for="evaluation-scenario-description">Description</label>
					<Textarea
						id="evaluation-scenario-description"
						rows="5"
						v-model="evaluationScenarioDescription"
						:readonly="true"
					/>

					<label class="text-sm" for="evaluation-scenario-notes">Notes</label>
					<Textarea id="evaluation-scenario-notes" rows="5" v-model="evaluationScenarioNotes" />

					<div class="field-checkbox">
						<Checkbox name="multipleUsers" binary v-model="evaluationScenarioMultipleUsers" />
						<label for="multipleUsers">Multiple users</label>
					</div>
				</form>
			</template>
			<template #footer>
				<div class="flex gap-2">
					<Button
						size="large"
						class="p-button-danger"
						v-if="evaluationScenarioCurrentStatus === EvaluationScenarioStatus.Started"
						@click="stopEvaluationScenario"
						>Stop</Button
					>
					<!-- sorry for this hackary but I couldn't figure out how to make the opposite logic work -->
					<div class="hidden" v-if="evaluationScenarioCurrentStatus === EvaluationScenarioStatus.Started" />
					<Button v-else size="large" @click="beginEvaluationScenario">Begin</Button>
					<Button size="large" class="p-button-secondary" outlined @click="isEvaluationScenarioModalVisible = false"
						>Close</Button
					>
				</div>
				<div class="align-self-center">
					<p>Runtime {{ evaluationScenarioRuntimeString }}</p>
				</div>
			</template>
		</tera-modal>
		<tera-modal
			v-if="isAboutModalVisible"
			@modal-mask-clicked="isAboutModalVisible = false"
			@modal-enter-press="isAboutModalVisible = false"
		>
			<article>
				<img src="@/assets/svg/terarium-logo.svg" alt="Terarium logo" class="about-terarium-logo" />
				<p class="text-lg line-height-3 about-top-line">
					Terarium is a comprehensive <span class="underlined">modeling</span> and
					<span class="underlined">simulation</span> platform designed to help researchers and analysts:
				</p>
				<p class="about-middle">
					<span class="pi pi-search about-bullet"></span>Extract models from academic literature
				</p>
				<p class="about-middle">
					<span class="pi pi-sliders-h about-bullet"></span>Calibrate them with real world data
				</p>
				<p class="about-middle">
					<span class="pi pi-cog about-bullet"></span>Run simulations to test a variety of scenarios, and
				</p>
				<p class="about-middle"><span class="pi pi-chart-line about-bullet"></span>Compare the results.</p>
			</article>
			<article class="about-uncharted-section">
				<img
					src="@/assets/svg/uncharted-logo-official.svg"
					alt="Uncharted Software logo"
					class="about-uncharted-logo"
				/>
				<p class="about-bottom-line text-sm">
					Uncharted Software provides design, development and consulting services related to data visualization and
					analysis software.
				</p>
			</article>
			<template #footer>
				<div class="modal-footer">
					<p class="text-sm">&copy; Copyright Uncharted Software {{ new Date().getFullYear() }}</p>
					<Button class="p-button" @click="isAboutModalVisible = false" size="large">Close</Button>
				</div>
			</template>
		</tera-modal>
	</nav>
</template>

<script setup lang="ts">
import { computed, onMounted, Ref, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import Avatar from 'primevue/avatar';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Menu from 'primevue/menu';
import type { MenuItem } from 'primevue/menuitem';
import { RoutePath } from '@/router/index';
import { RouteName } from '@/router/routes';
import useAuthStore from '@/stores/auth';
import SplitButton from 'primevue/splitbutton';
import TeraModal from '@/components/widgets/tera-modal.vue';
import TeraNotificationPanel from '@/components/navbar/tera-notification-panel.vue';
import Textarea from 'primevue/textarea';
import * as EventService from '@/services/event';
import { EvaluationScenarioStatus, EventType } from '@/types/Types';
import API from '@/api/api';
import { orderBy, remove, sortBy } from 'lodash';
import { useProjects } from '@/composables/project';
import { ProjectPages } from '@/types/Project';
import { EvalScenario, Question, Scenario } from '@/types/EvalScenario';
import Dropdown from 'primevue/dropdown';
import Checkbox from 'primevue/checkbox';
import getConfiguration from '@/services/ConfigService';
import evalScenariosJson from './eval-scenarios.json';

defineProps<{
	active: boolean;
}>();

const isAboutModalVisible = ref(false);

/*
 * Navigation Menu
 */
const router = useRouter();

const menuLabel = computed(() => {
	if (useProjects().activeProject.value) {
		return useProjects().activeProject.value?.name;
	}
	return 'Home';
});

/**
 * Evaluation scenario code
 */
const isEvaluationScenarioModalVisible = ref(false);
const evalScenarios: Ref<EvalScenario> = ref(evalScenariosJson);
const evaluationScenario: Ref<Scenario> = ref(evalScenarios.value.scenarios[0]);
const evaluationScenarioTask: Ref<Question> = ref(evaluationScenario.value.questions[0]);
const evaluationScenarioDescription: Ref<string> = ref(evaluationScenarioTask.value.description);
const evaluationScenarioMultipleUsers: Ref<boolean> = ref(true);
const evaluationScenarioNotes = ref('');
const evaluationScenarioCurrentStatus: Ref<EvaluationScenarioStatus> = ref(EvaluationScenarioStatus.Stopped);
const evaluationScenarioRuntimeMillis = ref(0);
let intervalId: number;
const documentationUrl = ref('');

const evaluationScenarioRuntimeString = computed(() => {
	const h = Math.floor(evaluationScenarioRuntimeMillis.value / 1000 / 60 / 60);
	const m = Math.floor((evaluationScenarioRuntimeMillis.value / 1000 / 60 / 60 - h) * 60);
	const s = Math.floor(((evaluationScenarioRuntimeMillis.value / 1000 / 60 / 60 - h) * 60 - m) * 60);
	const hS = h < 10 ? `0${h}` : `${h}`;
	const mS = m < 10 ? `0${m}` : `${m}`;
	const sS = s < 10 ? `0${s}` : `${s}`;
	return `${hS}:${mS}:${sS}`;
});

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

const onScenarioChange = () => {
	evaluationScenarioTask.value = evaluationScenario.value.questions[0];
	evaluationScenarioDescription.value = evaluationScenarioTask.value.description;
};

const onTaskChange = () => {
	evaluationScenarioDescription.value = evaluationScenarioTask.value.description;
};

/**
 * Returns the evaluation metadata model for the given action
 * @param action	the action name to log
 */

const getEvaluationScenarioData = (action: string) => ({
	name: evaluationScenario.value.name,
	task: evaluationScenarioTask.value.task,
	description: evaluationScenarioDescription.value,
	notes: evaluationScenarioNotes.value,
	multipleUsers: evaluationScenarioMultipleUsers.value,
	action
});

/**
 * Saves the model to local storage
 */
const persistEvaluationScenario = () => {
	window.localStorage.setItem('evaluationScenarioName', evaluationScenario.value.name);
	window.localStorage.setItem('evaluationScenarioTask', evaluationScenarioTask.value.task);
	window.localStorage.setItem('evaluationScenarioDescription', evaluationScenarioDescription.value);
	window.localStorage.setItem('evaluationScenarioNotes', evaluationScenarioNotes.value);
	window.localStorage.setItem('evaluationScenarioMultipleUsers', evaluationScenarioMultipleUsers.value.toString());
};

const refreshEvaluationScenario = async () => {
	evaluationScenarioCurrentStatus.value = (
		await API.get(`/evaluation/status?name=${evaluationScenario.value.name}`)
	).data;

	evaluationScenarioRuntimeMillis.value = (
		await API.get(`/evaluation/runtime?name=${evaluationScenario.value.name}`)
	).data;
};

const loadEvaluationScenario = async () => {
	const scenarioName = window.localStorage.getItem('evaluationScenarioName');
	const scenarioIndex = scenarioName ? evalScenarios.value.scenarios.findIndex((s) => s.name === scenarioName) : 0;
	evaluationScenario.value = evalScenarios.value.scenarios[scenarioIndex];

	const taskName = window.localStorage.getItem('evaluationScenarioTask');

	let taskIndex: number = 0;
	if (taskName && evaluationScenario.value?.questions) {
		taskIndex = evaluationScenario.value.questions.findIndex((q) => q.task === taskName);
		evaluationScenarioTask.value = evaluationScenario.value.questions[taskIndex];
	}

	evaluationScenarioDescription.value = evaluationScenarioTask.value.description;
	evaluationScenarioNotes.value = window.localStorage.getItem('evaluationScenarioNotes') || '';
	evaluationScenarioMultipleUsers.value = window.localStorage.getItem('evaluationScenarioMultipleUsers') !== 'false';

	if (evaluationScenario.value) {
		await refreshEvaluationScenario();
	}
};

const startEvaluationTimer = () => {
	if (evaluationScenarioCurrentStatus.value === EvaluationScenarioStatus.Started) {
		intervalId = window.setInterval(() => {
			evaluationScenarioRuntimeMillis.value += 1000;
		}, 1000);
	}
};

/**
 * Clears the model from local storage in memory
 */
const clearEvaluationScenario = () => {
	evaluationScenario.value = evalScenarios.value.scenarios[0];
	evaluationScenarioTask.value = evaluationScenario.value.questions[0];
	evaluationScenarioDescription.value = evaluationScenarioTask.value.description;
	evaluationScenarioNotes.value = '';
	evaluationScenarioCurrentStatus.value = EvaluationScenarioStatus.Stopped;
	evaluationScenarioRuntimeMillis.value = 0;
	evaluationScenarioMultipleUsers.value = false;
	persistEvaluationScenario();
};

const homeItem: MenuItem = {
	label: 'Home',
	icon: 'pi pi-home',
	command: () => router.push(RoutePath.Home)
};
const navMenuItems = ref<MenuItem[]>([homeItem]);

/*
 * User Menu
 */
const auth = useAuthStore();
const userMenu = ref();
const isLogoutDialog = ref(false);
const userMenuItems = ref([
	{
		label: 'Evaluation scenario',
		command: () => {
			isEvaluationScenarioModalVisible.value = true;
		}
	},
	{
		label: 'Evaluation results',
		command: () => {
			router.push(RoutePath.EvaluationScenariosPath);
		}
	},
	{
		label: 'User administration',
		command: () => {
			router.push(RoutePath.UserAdmin);
		},
		visible: auth.isAdmin
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
	documentationUrl.value = await documentation();
});

const showUserMenu = (event) => {
	userMenu.value.toggle(event);
};

const userInitials = computed(() => auth.userInitials);

function closeLogoutDialog() {
	isLogoutDialog.value = false;
}

function getNavMenuItem(project) {
	return {
		label: project.name,
		icon: 'pi pi-folder',
		command: () =>
			router.push({
				name: RouteName.Project,
				params: { projectId: project.id, pageType: ProjectPages.OVERVIEW }
			})
	};
}

watch(
	() => useProjects().allProjects.value,
	() => {
		const items: MenuItem[] = [];
		const lastProjectUpdated = orderBy(useProjects().allProjects.value, ['updatedOn'], ['desc'])[0];
		useProjects().allProjects.value?.forEach((project) => items.push(getNavMenuItem(project)));

		const removedUpdatedProject = remove(items, (item) => item.label === lastProjectUpdated?.name);
		navMenuItems.value = [
			homeItem,
			...removedUpdatedProject,
			...sortBy(items, (item) => item.label?.toString().toLowerCase())
		];
	},
	{ immediate: true }
);

const documentation = async () => {
	const config = await getConfiguration();
	const host = config?.documentationUrl;
	if (!host) {
		const url = window.location.hostname.replace(/\bapp\b/g, 'documentation');
		return `https://${url}`;
	}
	return host;
};
</script>

<style scoped>
nav {
	align-items: center;
	background-color: var(--surface-section);
	border-bottom: 1px solid var(--surface-border-light);
	padding: var(--gap-2) var(--gap-4);
	display: flex;
	gap: var(--gap-8);
	grid-area: header;

	a,
	a:hover {
		text-decoration: none;
	}
}

.terarium-logo {
	margin-top: var(--gap-1-5);
}
.layout-project-selection {
	margin-right: auto;
}

.avatar {
	background-color: var(--primary-color-lighter);
	color: var(--text-color-subdued);
	cursor: pointer;
}

.avatar:hover {
	background-color: var(--primary-color-light);
	color: var(--text-color);
}

/* Split button
 * This needs to be into its own component
 */

:deep(.layout-project-selection.p-splitbutton .p-button:first-of-type) {
	border-top-right-radius: 0;
	border-bottom-right-radius: 0;
	border-right: 0 none;
	color: var(--text-color);
	pointer-events: none;
}

:deep(.p-splitbutton.p-button-outlined > .p-button) {
	box-shadow: var(--surface-400) 0 0 0 1px inset;
}

:deep(.layout-project-selection.p-splitbutton .p-button:last-of-type) {
	background-color: var(--surface-200);
	border-top-left-radius: 0;
	border-bottom-left-radius: 0;
	color: var(--text-color-light);
	padding: 0.714rem 0;
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
	margin-bottom: 1rem;
}
.about-bullet {
	color: var(--text-color-subdued);
	margin-right: 1rem;
	/* background-color: var(--primary-color-lighter); */
	border-radius: 5rem;
	padding: 0.75rem;
	font-size: 1.5rem;
}
.about-top-line {
	max-width: 90%;
	margin-bottom: 1rem;
}
.underlined {
	position: relative;
	font-weight: 900;
}
.underlined:after {
	content: '';
	position: absolute;
	bottom: -8px;
	left: 0;
	height: 7px;
	width: 100%;
	border: solid 2px var(--primary-color);
	border-color: var(--primary-color) transparent transparent transparent;
	border-radius: 50%;
}
.about-middle {
	font-size: 1rem;
	display: flex;
	align-items: center;
}
.about-uncharted-section {
	padding: 1rem;
	background: var(--surface-100);
	border: 1px solid var(--surface-border-light);
	border-radius: 6px;
	margin-top: 2rem;
}

.evaluation-scenario-widget {
	border-radius: var(--border-radius-bigger);
	background-color: var(--surface-200);
	display: flex;
	padding-left: var(--gap-4);
	margin-left: auto;
	margin-right: auto;
	align-items: center;
	gap: var(--gap-4);
	box-shadow: inset 0px 0px 5px var(--surface-border-light);

	.evaluation-scenario-widget-timer {
		font-feature-settings: 'tnum';
	}
}

.about-uncharted-logo {
	width: 8rem;
	margin-bottom: 0.5rem;
}
.about-bottom-line {
	color: var(--text-color);
}

.modal-footer {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	width: 100%;
	color: var(--text-color-subdued);
}

.evaluation-scenario-modal:deep(footer) {
	justify-content: space-between;
}
.status-chip {
	background-color: var(--surface-highlight);
	border-radius: var(--border-radius);
	padding: var(--gap-2);
}
.field-checkbox {
	font-size: var(--font-body-small);
	color: var(--text-color-primary);
	margin-bottom: 0;
	display: flex;
	align-items: center;
	& > label {
		margin-bottom: 0px !important;
	}
}
</style>
