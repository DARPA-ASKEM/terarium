<script setup lang="ts">
/**
 * Sidebar component for navigating modes
 * */
import { computed, ref } from 'vue';
import IconDataPlayer32 from '@carbon/icons-vue/es/data-player/32';
import DocumentPdf32 from '@carbon/icons-vue/es/document--pdf/32';
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import IconTableSplit32 from '@carbon/icons-vue/es/table--split/32';
import IconProvenanceGraph32 from '@carbon/icons-vue/es/flow/32';
import IconUser32 from '@carbon/icons-vue/es/user/32';
import ModelSidebarPanel from '@/components/sidebar-panel/model-sidebar-panel.vue';
import DocumentsSidebarPanel from '@/components/sidebar-panel/documents-sidebar-panel.vue';
import ProfileSidebarPanel from '@/components/sidebar-panel/profile-sidebar-panel.vue';

const enum Mode {
	SimulationPlan = 'Simulation Plan',
	Models = 'Models',
	Datasets = 'Datasets',
	Documents = 'Documents',
	ProvenanceGraph = 'Provenance Graph',
	Profile = 'Profile'
}

const selectedMode = ref('');
const isCollapsed = computed(() => selectedMode.value.length === 0);

function updateMode(mode: string) {
	selectedMode.value = mode === selectedMode.value ? '' : mode;
}
</script>

<template>
	<section>
		<nav>
			<ul>
				<li :active="selectedMode === Mode.SimulationPlan" @click="updateMode(Mode.SimulationPlan)">
					<IconDataPlayer32 />
				</li>
				<li :active="selectedMode === Mode.Models" @click="updateMode(Mode.Models)">
					<IconMachineLearningModel32 />
				</li>
				<li :active="selectedMode === Mode.Datasets" @click="updateMode(Mode.Datasets)">
					<IconTableSplit32 />
				</li>
				<li :active="selectedMode === Mode.Documents" @click="updateMode(Mode.Documents)">
					<DocumentPdf32 />
				</li>
			</ul>
			<ul>
				<li
					:active="selectedMode === Mode.ProvenanceGraph"
					@click="updateMode(Mode.ProvenanceGraph)"
				>
					<IconProvenanceGraph32 />
				</li>
				<li :active="selectedMode === Mode.Profile" @click="updateMode(Mode.Profile)">
					<IconUser32 />
				</li>
			</ul>
		</nav>
		<aside v-if="!isCollapsed">
			<header>{{ selectedMode }}</header>
			<main>
				<ModelSidebarPanel v-if="selectedMode === Mode.Models" />
				<DocumentsSidebarPanel v-else-if="selectedMode === Mode.Documents" />
				<ProfileSidebarPanel v-else-if="selectedMode === Mode.Profile" />
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
	z-index: 2;
	justify-content: space-between;
	padding-top: 0.33rem;
	width: 4rem;
	height: calc(100vh - var(--header-height));
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
}

aside main {
	flex-grow: 1;
}
</style>
