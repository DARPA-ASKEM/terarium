<script setup lang="ts">
/**
 * Sidebar component for navigating modes
 * */
import { ref } from 'vue';
import IconOpenPanelRight16 from '@carbon/icons-vue/es/open-panel--filled--right/16';
import IconSidePanelClose16 from '@carbon/icons-vue/es/side-panel--close/16';
import IconDataPlayer32 from '@carbon/icons-vue/es/data-player/32';
import IconDocumentPdf32 from '@carbon/icons-vue/es/document--pdf/32';
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import IconTableSplit32 from '@carbon/icons-vue/es/table--split/32';
import IconProvenanceGraph32 from '@carbon/icons-vue/es/flow/32';
import IconUser32 from '@carbon/icons-vue/es/user/32';
import Button from '@/components/Button.vue';
import ModelSidebarPanel from '@/components/sidebar-panel/model-sidebar-panel.vue';
import DocumentsSidebarPanel from '@/components/sidebar-panel/documents-sidebar-panel.vue';
import ProfileSidebarPanel from '@/components/sidebar-panel/profile-sidebar-panel.vue';

// Manage Side Panel
const isSidePanelOpen = ref(false);
function closeSidePanel() {
	isSidePanelOpen.value = false;
}
function openSidePanel() {
	isSidePanelOpen.value = true;
}

const enum View {
	SimulationPlan = 'Simulation Plan',
	Models = 'Models',
	Datasets = 'Datasets',
	Documents = 'Documents',
	ProvenanceGraph = 'Provenance Graph',
	Profile = 'Profile'
}

const selectedView = ref('');

/**
 * Open a View
 * @param {string} view - The view to be open.
 * @param {boolean} [openViewSidePanel=true] - Should the side-panel be open when opening the view.
 */
function openView(view: string, openViewSidePanel: boolean = true): void {
	selectedView.value = view;
	if (isSidePanelOpen.value) {
		if (!openViewSidePanel) closeSidePanel();
	} else if (openViewSidePanel) openSidePanel();
}
</script>

<template>
	<section>
		<nav>
			<ul>
				<li
					:active="selectedView === View.SimulationPlan"
					@click="openView(View.SimulationPlan, false)"
				>
					<IconDataPlayer32 />
				</li>
				<li :active="selectedView === View.Models" @click="openView(View.Models)">
					<IconMachineLearningModel32 />
				</li>
				<li :active="selectedView === View.Datasets" @click="openView(View.Datasets, false)">
					<IconTableSplit32 />
				</li>
				<li :active="selectedView === View.Documents" @click="openView(View.Documents)">
					<IconDocumentPdf32 />
				</li>
			</ul>
			<ul>
				<li
					:active="selectedView === View.ProvenanceGraph"
					@click="openView(View.ProvenanceGraph, false)"
				>
					<IconProvenanceGraph32 />
				</li>
				<li :active="selectedView === View.Profile" @click="openView(View.Profile)">
					<IconUser32 />
				</li>
			</ul>
		</nav>
		<aside v-if="isSidePanelOpen">
			<header>
				{{ selectedView }}
				<Button @click="closeSidePanel"><IconSidePanelClose16 /></Button>
				<Button danger @click="openSidePanel"><IconOpenPanelRight16 /></Button>
			</header>
			<main>
				<ModelSidebarPanel v-if="selectedView === View.Models" />
				<DocumentsSidebarPanel v-else-if="selectedView === View.Documents" />
				<ProfileSidebarPanel v-else-if="selectedView === View.Profile" />
				<template v-else> Create a sidebar-panel component </template>
			</main>
		</aside>
	</section>
</template>

<style scoped>
section {
	background-color: var(--un-color-accent);
	box-shadow: var(--un-box-shadow-default);
	display: flex;
	height: 100%;
	isolation: isolate;
}

nav {
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

nav svg {
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

aside {
	background-color: var(--un-color-body-surface-primary);
	display: flex;
	flex-direction: column;
	gap: 1rem;
	padding: 1rem;
	width: max(15rem, 20vw);
	z-index: 1;
}

aside header {
	color: var(--un-color-body-text-secondary);
	font: var(--un-font-h5);
	position: relative;
}

aside header button {
	display: none;
	position: absolute;
	right: 0;
	top: 0;
}

aside main {
	flex-grow: 1;
}
</style>
