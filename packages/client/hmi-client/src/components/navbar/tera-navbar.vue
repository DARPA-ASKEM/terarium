<template>
	<nav>
		<section class="header-left">
			<router-link :to="RoutePath.Home">
				<img src="@assets/svg/terarium-icon.svg" height="30" alt="Terarium icon" />
			</router-link>
			<div class="navigation-dropdown" @click="showNavigationMenu">
				<h1 v-if="useProjects().activeProject.value?.id || isDataExplorer">
					{{ useProjects().activeProject.value?.name ?? 'Explorer' }}
				</h1>
				<img
					v-else
					src="@assets/svg/terarium-wordmark.svg"
					height="16"
					alt="Terarium icon with name"
					class="terariumLogo"
				/>
				<i class="pi pi-angle-down" />
			</div>
			<Menu ref="navigationMenu" :model="navMenuItems" :popup="true" class="navigation-menu" />
		</section>
		<section v-if="active" class="header-right">
			<Avatar :label="userInitials" class="avatar m-2" shape="circle" @click="showUserMenu" />
			<Menu ref="userMenu" :model="userMenuItems" :popup="true" />
			<Dialog header="Logout" v-model:visible="isLogoutDialog">
				<p>You will be returned to the login screen.</p>
				<template #footer>
					<Button label="Cancel" class="p-button-secondary" @click="closeLogoutDialog" />
					<Button label="Ok" @click="auth.logout" />
				</template>
			</Dialog>
		</section>
		<aside class="suggested-terms" v-if="!isEmpty(terms) && isDataExplorer">
			Suggested terms:
			<Chip v-for="term in terms" :key="term" removable remove-icon="pi pi-times">
				<span @click="searchBarRef?.addToQuery(term)">{{ term }}</span>
			</Chip>
		</aside>
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
	</nav>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { isEmpty } from 'lodash';
import Avatar from 'primevue/avatar';
import Button from 'primevue/button';
import Chip from 'primevue/chip';
import Dialog from 'primevue/dialog';
import Menu from 'primevue/menu';
import { MenuItem } from 'primevue/menuitem';
import { RoutePath, useCurrentRoute } from '@/router/index';
import { RouteMetadata, RouteName } from '@/router/routes';
import useAuthStore from '@/stores/auth';
import InputText from 'primevue/inputtext';
import TeraModal from '@/components/widgets/tera-modal.vue';
import Textarea from 'primevue/textarea';
import * as EventService from '@/services/event';
import { EvaluationScenarioStatus, EventType } from '@/types/Types';
import API from '@/api/api';
import { useProjects } from '@/composables/project';

defineProps<{
	active: boolean;
}>();

/*
 * Navigation Menu
 */
const router = useRouter();
const navigationMenu = ref();

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
const showNavigationMenu = (event) => {
	navigationMenu.value.toggle(event);
};
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

/*
 * Search
 */
const searchBarRef = ref();

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
		navMenuItems.value = [homeItem, explorerItem, { label: 'Projects', items }];
	},
	{ immediate: true }
);
</script>

<style scoped>
nav {
	background-color: var(--surface-section);
	border-bottom: 1px solid var(--surface-border-light);
	padding: 0.5rem 1rem;
	display: grid;
	column-gap: 0.5rem;
	grid-template-areas:
		'header-left search-bar header-right'
		'suggested-terms suggested-terms suggested-terms';
	grid-template-columns: minMax(max-content, 25%) auto minMax(min-content, 25%);
}

.navigation-dropdown {
	border-color: transparent;
	border-style: solid;
	border-radius: var(--border-radius);
	border-width: 1px;
	cursor: pointer;
	font-size: var(--font-body-medium);
	font-weight: var(--font-weight-semibold);
	padding: 0.5rem;
}

.navigation-dropdown:hover,
.navigation-dropdown:focus {
	background-color: var(--surface-ground);
}

.terariumLogo {
	cursor: pointer;
}

/* Search Bar */

.search-bar {
	grid-area: search-bar;
	margin-left: auto;
	margin-right: auto;
	min-width: 25vw;
	max-width: 60rem;
	width: 100%;
}

/* Header Right */

.header-right {
	grid-area: header-right;
	margin-left: auto;
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

/* Header Left */
.header-left {
	align-items: center;
	display: flex;
	gap: 0.15rem;
	grid-area: header-left;
	height: 100%;
}

.header-left:deep(.p-dropdown-label.p-inputtext) {
	padding-right: 0;
}

.header-left > div {
	align-items: center;
	cursor: pointer;
	display: flex;
}

.p-dropdown {
	border: 0;
}

.p-dropdown-label {
	color: var(--text-color-secondary);
}

i {
	color: var(--text-color-subdued);
	margin-left: 0.5rem;
	vertical-align: bottom;
}

/* Suggested terms */
.suggested-terms {
	align-items: center;
	color: var(--text-color-subdued);
	display: flex;
	column-gap: 0.5rem;
	font-size: var(--font-caption);
	grid-area: suggested-terms;
	justify-content: center;
	margin-top: 0.5rem;
	white-space: nowrap;
}

.clear-search-terms:enabled {
	color: var(--text-color-secondary);
	background-color: transparent;
}

.clear-search-terms:enabled:hover {
	background-color: var(--surface-hover);
	color: var(--text-color-secondary);
}

.navigation-menu {
	margin-top: 0.25rem;
	min-width: fit-content !important;
}
</style>
