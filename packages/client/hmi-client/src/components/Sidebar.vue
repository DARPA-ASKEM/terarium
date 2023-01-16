<script setup lang="ts">
/**
 * Sidebar component for navigating view.
 * */
import { ref, computed } from 'vue';
import { RouteParamsRaw, useRoute, useRouter } from 'vue-router';

// Icons
import IconCaretLeft16 from '@carbon/icons-vue/es/caret--left/16';
import IconCaretRight16 from '@carbon/icons-vue/es/caret--right/16';

// Components
import Button from '@/components/Button.vue';
import ModelSidebarPanel from '@/components/sidebar-panel/model-sidebar-panel.vue';
import DatasetSidebarPanel from '@/components/sidebar-panel/dataset-sidebar-panel.vue';
import DocumentsSidebarPanel from '@/components/sidebar-panel/documents-sidebar-panel.vue';
import SimulationResultSidebarPanel from '@/components/sidebar-panel/simulation-result-sidebar-panel.vue';
import SimulationPlanSidebarPanel from '@/components/sidebar-panel/simulation-plan-sidebar-panel.vue';

import { RouteName, RouteMetadata } from '@/router/routes';
import { Project } from '@/types/Project';

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

const route = useRoute();
// Assumes that the only routes we'll navigate to are represented in RouteName
const selectedView = computed(() => (route.name as RouteName) ?? RouteName.ProjectRoute);
const showSidePanel = computed(() => selectedView.value !== RouteName.ProjectRoute);

function showSidebar(view: RouteName): boolean {
	// Test for Sidebar that doesn't need Project
	const needProject = [
		RouteName.ModelRoute,
		RouteName.DatasetRoute,
		RouteName.DocumentRoute
	].includes(view);

	// Sidebars that needs a defined Project
	const noNeedProject =
		[RouteName.SimulationRoute, RouteName.SimulationResultRoute].includes(view) && !!props.project;

	return needProject || noNeedProject;
}

const openView = (view: RouteName) => {
	// Open the appropriate view
	if (selectedView.value !== view && Object.values(RouteName).includes(view)) {
		const params: RouteParamsRaw = { projectId: props?.project?.id, assetId: '' };
		router.push({ name: view, params });
	} else if (showSidebar(view) && !isSidePanelClose.value) {
		openSidePanel();
	}
};

const BUTTON_ORDER = [
	RouteName.ProjectRoute,
	RouteName.SimulationRoute,
	RouteName.ModelRoute,
	RouteName.DatasetRoute,
	RouteName.SimulationResultRoute,
	RouteName.DocumentRoute
];

const DISABLED_BUTTONS = [RouteName.ProvenanceRoute];
</script>

<template>
	<section>
		<nav>
			<ul>
				<li
					v-for="routeName of BUTTON_ORDER"
					:key="routeName"
					:active="selectedView === routeName"
					:title="RouteMetadata[routeName].displayName"
					@click="openView(routeName)"
				>
					<component :is="RouteMetadata[routeName].icon" />
				</li>
			</ul>
			<ul>
				<li
					v-for="routeName of DISABLED_BUTTONS"
					:key="routeName"
					disabled
					:active="selectedView === routeName"
					:title="RouteMetadata[routeName].displayName"
					@click="openView(routeName)"
				>
					<component :is="RouteMetadata[routeName].icon" />
				</li>
			</ul>
			<Button
				round
				class="side-panel-control"
				v-if="isSidePanelClose && showSidePanel"
				@click="openSidePanel"
			>
				<IconCaretRight16 />
			</Button>
		</nav>
		<aside v-if="showSidebar(selectedView)" :class="{ 'side-panel-close': isSidePanelClose }">
			<h4>{{ RouteMetadata[selectedView].displayName }}</h4>
			<ModelSidebarPanel v-if="selectedView === RouteName.ModelRoute" />
			<DatasetSidebarPanel v-if="selectedView === RouteName.DatasetRoute" />
			<DocumentsSidebarPanel v-if="selectedView === RouteName.DocumentRoute" />
			<SimulationResultSidebarPanel
				v-if="project && selectedView === RouteName.SimulationResultRoute"
				:project="project"
			/>
			<SimulationPlanSidebarPanel
				v-if="project && selectedView === RouteName.SimulationRoute"
				:project="project"
			/>
			<Button round class="side-panel-control" @click="closeSidePanel">
				<IconCaretLeft16 />
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
	display: none;
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
