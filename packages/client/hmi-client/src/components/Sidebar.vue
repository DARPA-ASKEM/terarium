<script setup lang="ts">
/**
 * Sidebar component for navigating view.
 * */
import { ref } from 'vue';
import IconArrowLeft16 from '@carbon/icons-vue/es/arrow--left/16';
import IconArrowRight16 from '@carbon/icons-vue/es/arrow--right/16';
import IconDataPlayer32 from '@carbon/icons-vue/es/data-player/32';
import IconDocumentPdf32 from '@carbon/icons-vue/es/document--pdf/32';
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import IconTableSplit32 from '@carbon/icons-vue/es/table--split/32';
import IconFlow32 from '@carbon/icons-vue/es/flow/32';
import IconUser32 from '@carbon/icons-vue/es/user/32';
import Button from '@/components/Button.vue';
import ModelSidebarPanel from '@/components/sidebar-panel/model-sidebar-panel.vue';
import DocumentsSidebarPanel from '@/components/sidebar-panel/documents-sidebar-panel.vue';
import ProfileSidebarPanel from '@/components/sidebar-panel/profile-sidebar-panel.vue';
import { useRouter } from 'vue-router';
import { RouteName } from '@/router/index';

const router = useRouter();

// Manage Side Panel
const isSidePanelClose = ref(true);
function closeSidePanel() {
	isSidePanelClose.value = true;
}
function openSidePanel() {
	isSidePanelClose.value = false;
}

const selectedView = ref('');

/**
 * Open a View
 * @param {string} view - The view to be open.
 * @param {boolean} [openViewSidePanel=true] - Should the side-panel be open when opening the view.
 */
function openView(view: string, openViewSidePanel: boolean = true): void {
	selectedView.value = view;
	if (isSidePanelClose.value) {
		if (openViewSidePanel) openSidePanel();
	} else if (!openViewSidePanel) {
		closeSidePanel();
	}

	// FIXME: sort out the difference between routing to a page and opening the side-panel
	if ([RouteName.ModelRoute, RouteName.SimulationRoute].includes(view as RouteName)) {
		router.push({ name: view });
	}
}
</script>

<template>
	<section>
		<nav>
			<ul>
				<li
					:active="selectedView === RouteName.SimulationRoute"
					@click="openView(RouteName.SimulationRoute, false)"
				>
					<IconDataPlayer32 />
				</li>
				<li :active="selectedView === RouteName.ModelRoute" @click="openView(RouteName.ModelRoute)">
					<IconMachineLearningModel32 />
				</li>
				<li
					:active="selectedView === RouteName.DatasetRoute"
					@click="openView(RouteName.DatasetRoute, false)"
				>
					<IconTableSplit32 />
				</li>
				<li
					:active="selectedView === RouteName.DocumentRoute"
					@click="openView(RouteName.DocumentRoute)"
				>
					<IconDocumentPdf32 />
				</li>
			</ul>
			<ul>
				<li
					:active="selectedView === RouteName.ProvenanceRoute"
					@click="openView(RouteName.ProvenanceRoute, false)"
				>
					<IconFlow32 />
				</li>
				<li
					:active="selectedView === RouteName.ProfileRoute"
					@click="openView(RouteName.ProfileRoute)"
				>
					<IconUser32 />
				</li>
			</ul>
			<Button round class="side-panel-control" v-if="isSidePanelClose" @click="openSidePanel">
				<IconArrowRight16 />
			</Button>
		</nav>
		<aside :class="{ 'side-panel-close': isSidePanelClose }">
			<header>{{ selectedView }}</header>
			<main>
				<ModelSidebarPanel v-if="selectedView === RouteName.ModelRoute" />
				<DocumentsSidebarPanel v-else-if="selectedView === RouteName.DocumentRoute" />
				<ProfileSidebarPanel v-else-if="selectedView === RouteName.ProfileRoute" />
				<template v-else> Create a sidebar-panel component </template>
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
