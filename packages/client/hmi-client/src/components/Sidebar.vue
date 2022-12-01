<script setup lang="ts">
/**
 * Sidebar component for navigating view.
 * */
import { ref } from 'vue';
import { RouteParamsRaw, useRouter } from 'vue-router';

// Icons
import IconArrowLeft16 from '@carbon/icons-vue/es/arrow--left/16';
import IconArrowRight16 from '@carbon/icons-vue/es/arrow--right/16';
import IconAccount32 from '@carbon/icons-vue/es/account/32';
import IconAppConnectivity32 from '@carbon/icons-vue/es/app-connectivity/32';
import IconDocument32 from '@carbon/icons-vue/es/document/32';
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import IconTableSplit32 from '@carbon/icons-vue/es/table--split/32';
import IconFlow32 from '@carbon/icons-vue/es/flow/32';
import IconUser32 from '@carbon/icons-vue/es/user/32';
import IconChartCombo32 from '@carbon/icons-vue/es/chart--combo/32';

// Components
import Button from '@/components/Button.vue';
import ModelSidebarPanel from '@/components/sidebar-panel/model-sidebar-panel.vue';
import DocumentsSidebarPanel from '@/components/sidebar-panel/documents-sidebar-panel.vue';
import ProfileSidebarPanel from '@/components/sidebar-panel/profile-sidebar-panel.vue';
import SimulationResultSidebarPanel from '@/components/sidebar-panel/simulation-result-sidebar-panel.vue';
import SimulationPlanSidebarPanel from '@/components/sidebar-panel/simulation-plan-sidebar-panel.vue';

import { RouteName } from '@/router/index';
import { MODELS, PLANS, SIMULATION_RUNS, Project } from '@/types/Project';

const router = useRouter();

const props = defineProps<{
	project: Project | null;
}>();

// Manage Side Panel
const isSidePanelClose = ref(false);
function closeSidePanel() {
	isSidePanelClose.value = true;
}
function openSidePanel() {
	isSidePanelClose.value = false;
}

// The Project page is the default
const selectedView = ref<RouteName>(RouteName.ProjectRoute);

function showSidebar(view: RouteName): boolean {
	// Test for Sidebar that doesn't need Project
	const needProject = [
		RouteName.ModelRoute,
		RouteName.DocumentRoute,
		RouteName.ProfileRoute
	].includes(view);

	// Sidebars that needs a defined Project
	const noNeedProject =
		[RouteName.SimulationRoute, RouteName.SimulationResultRoute].includes(view) && !!props.project;

	return needProject || noNeedProject;
}

const openView = (view: RouteName) => {
	// Open the appropriate view
	if (selectedView.value !== view && Object.values(RouteName).includes(view)) {
		// Set the Route parameters
		const params: RouteParamsRaw = {};

		// Set the projectId, except for the Document Route
		if (view !== RouteName.DocumentRoute && props?.project?.id) {
			params.projectId = props.project.id;
		}

		if (view === RouteName.ModelRoute) {
			params.modelId = props?.project?.assets[MODELS]?.[0] ?? 1;
		}

		if (view === RouteName.SimulationRoute) {
			params.simulationId = props?.project?.assets[PLANS]?.[0] ?? 1;
		}

		if (view === RouteName.SimulationResultRoute) {
			params.simulationRunId = props?.project?.assets[SIMULATION_RUNS]?.[0] ?? 1;
		}

		// Change the view
		router.push({ name: view, params });
		selectedView.value = view;
	} else if (showSidebar(view) && !isSidePanelClose.value) {
		openSidePanel();
	}
};
</script>

<template>
	<section>
		<nav>
			<ul>
				<li
					:active="selectedView === RouteName.ProjectRoute"
					:title="RouteName.ProjectRoute"
					@click="openView(RouteName.ProjectRoute)"
				>
					<IconAccount32 />
				</li>
				<li
					:active="selectedView === RouteName.SimulationRoute"
					:title="RouteName.SimulationRoute"
					@click="openView(RouteName.SimulationRoute)"
				>
					<IconAppConnectivity32 />
				</li>
				<li
					:active="selectedView === RouteName.ModelRoute"
					:title="RouteName.ModelRoute"
					@click="openView(RouteName.ModelRoute)"
				>
					<IconMachineLearningModel32 />
				</li>
				<li
					disabled
					:active="selectedView === RouteName.DatasetRoute"
					:title="RouteName.DatasetRoute"
					@click="openView(RouteName.DatasetRoute)"
				>
					<IconTableSplit32 />
				</li>
				<li
					:active="selectedView === RouteName.DocumentRoute"
					:title="RouteName.DocumentRoute"
					@click="openView(RouteName.DocumentRoute)"
				>
					<IconDocument32 />
				</li>
				<li
					:active="selectedView === RouteName.SimulationResultRoute"
					@click="openView(RouteName.SimulationResultRoute)"
				>
					<IconChartCombo32 />
				</li>
			</ul>
			<ul>
				<li
					disabled
					:active="selectedView === RouteName.ProvenanceRoute"
					:title="RouteName.ProvenanceRoute"
					@click="openView(RouteName.ProvenanceRoute)"
				>
					<IconFlow32 />
				</li>
				<li
					disabled
					:active="selectedView === RouteName.ProfileRoute"
					:title="RouteName.ProfileRoute"
					@click="openView(RouteName.ProfileRoute)"
				>
					<IconUser32 />
				</li>
			</ul>
			<Button round class="side-panel-control" v-if="isSidePanelClose" @click="openSidePanel">
				<IconArrowRight16 />
			</Button>
		</nav>
		<aside v-if="showSidebar(selectedView)" :class="{ 'side-panel-close': isSidePanelClose }">
			<header>{{ selectedView }}</header>
			<main>
				<ModelSidebarPanel v-if="selectedView === RouteName.ModelRoute" />
				<DocumentsSidebarPanel v-if="selectedView === RouteName.DocumentRoute" />
				<ProfileSidebarPanel v-if="selectedView === RouteName.ProfileRoute" />
				<SimulationResultSidebarPanel
					v-if="project && selectedView === RouteName.SimulationResultRoute"
					:project="project"
				/>
				<SimulationPlanSidebarPanel
					v-if="project && selectedView === RouteName.SimulationRoute"
					:project="project"
				/>
			</main>
			<Button round class="side-panel-control" @click="closeSidePanel">
				<IconArrowLeft16 />
			</Button>
		</aside>
	</section>
</template>

<style scoped>
section {
	display: flex;
	height: 100%;
	isolation: isolate;
}

section nav,
section aside {
	position: relative;
}

section .side-panel-control {
	--btn-background: var(--un-color-accent-mono);
	--btn-box-shadow: none;
	position: absolute;
	right: 0;
	top: 50%;
	transform: translateX(50%);
}

nav {
	background-color: var(--un-color-accent);
	box-shadow: var(--un-box-shadow-default);
	display: flex;
	flex-direction: column;
	height: calc(100vh - var(--header-height));
	justify-content: space-between;
	padding-top: 0.33rem;
	width: 4rem;
	z-index: 2;
}

nav ul {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	list-style: none;
	gap: 0.5rem;
}

nav ul li {
	border-radius: 4px;
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	height: 3rem;
	width: 3rem;
}

nav li svg {
	fill: var(--un-color-accent-light);
}

nav li[active='true'] svg {
	fill: var(--un-color-white);
}

nav li:hover {
	background-color: var(--un-color-white);
}

nav li:hover svg {
	fill: var(--un-color-accent);
}

nav li[disabled] {
	pointer-events: none;
}

nav li[disabled] svg {
	fill: var(--un-color-accent-dark);
}

aside {
	background-color: var(--un-color-body-surface-primary);
	box-shadow: var(--un-box-shadow-default);
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
	position: relative;
	transition-property: padding, width;
	transition-duration: 0s;
	width: max(15rem, 20vw);
	z-index: 1;
}

aside.side-panel-close {
	padding: 0;
	width: 0;
}

aside header {
	font: var(--un-font-h5);
	overflow: hidden;
}

aside main {
	flex-grow: 1;
	overflow: hidden;
}
</style>
