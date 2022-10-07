<script setup lang="ts">
/**
 * Sidebar component for navigating modes
 * */
import { ref, computed } from 'vue';
import Button from '@/components/Button.vue';
import IconDataPlayer32 from '@carbon/icons-vue/es/data-player/32';
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import IconTableSplit32 from '@carbon/icons-vue/es/table--split/32';
import IconSearchLocate32 from '@carbon/icons-vue/es/search--locate/32';
import IconProvenanceGraph32 from '@carbon/icons-vue/es/flow/32';
import IconUser32 from '@carbon/icons-vue/es/user/32';
import IconLogout16 from '@carbon/icons-vue/es/logout/16';
import { useAuthStore } from '../stores/auth';

const enum Mode {
	SimulationPlan = 'Simulation Plan',
	ModelView = 'Model View',
	Datasets = 'Datasets',
	DataExplorer = 'Data Explorer',
	ProvenanceGraph = 'Provenance Graph',
	Profile = 'Profile'
}

const auth = useAuthStore();

// Get sidebar position if saved in local storage
const isSidebarPositionRight = ref(
	localStorage.getItem('isSidebarPositionRight')
		? localStorage.getItem('isSidebarPositionRight') === 'true'
		: false
);
const selectedMode = ref('');
const isCollapsed = computed(() => selectedMode.value.length === 0);

// Later move mode specific features into their own components
const logout = () => {
	auth.logout();
	window.location.assign('/logout');
};

function updateMode(mode: string) {
	selectedMode.value = mode === selectedMode.value ? '' : mode;
}

function moveSidebar() {
	localStorage.setItem('isSidebarPositionRight', (!isSidebarPositionRight.value).toString());
	isSidebarPositionRight.value = localStorage.getItem('isSidebarPositionRight') === 'true';
}
</script>

<template>
	<section :class="{ right: isSidebarPositionRight }">
		<nav class="mode-selection">
			<ul>
				<li :active="selectedMode === Mode.SimulationPlan" @click="updateMode(Mode.SimulationPlan)">
					<IconDataPlayer32 />
				</li>
				<li :active="selectedMode === Mode.ModelView" @click="updateMode(Mode.ModelView)">
					<IconMachineLearningModel32 />
				</li>
				<li :active="selectedMode === Mode.Datasets" @click="updateMode(Mode.Datasets)">
					<IconTableSplit32 />
				</li>
				<li :active="selectedMode === Mode.DataExplorer" @click="updateMode(Mode.DataExplorer)">
					<IconSearchLocate32 />
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
		<div v-if="!isCollapsed" class="mode-configuration" :class="{ right: isSidebarPositionRight }">
			<header>{{ selectedMode }}</header>
			<div v-if="selectedMode === Mode.Profile">
				<Button @click="moveSidebar"> Move sidebar </Button>
				<Button @click="logout"
					>Logout
					<IconLogout16 />
				</Button>
			</div>
		</div>
	</section>
</template>

<style scoped>
section {
	display: flex;
	height: 100%;
}

section.right {
	order: 1;
	flex-direction: row-reverse;
}

nav {
	display: flex;
	flex-direction: column;
	background-color: var(--un-color-accent-light);
	border-right: 1px solid var(--un-color-black-20);
}

.mode-configuration.right,
section.right nav {
	border-left: 1px solid var(--un-color-black-20);
	border-right: 0;
}

ul {
	list-style: none;
}

nav.mode-selection {
	justify-content: space-between;
}

nav.mode-selection ul li {
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	height: 3.25rem;
	width: 3.25rem;
	margin: 0.5rem;
	border-radius: 5px;
	background-color: var(--un-color-body-surface-secondary);
	color: var(--un-color-black-100);
}

nav.mode-selection ul li:hover,
nav.mode-selection ul li[active='true'] {
	background-color: var(--un-color-white);
	color: var(--un-color-accent);
}

.mode-configuration {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	background-color: var(--un-color-accent-lighter);
	border-right: 1px solid var(--un-color-black-20);
	width: 15rem;
	padding: 0.5rem;
}
</style>
