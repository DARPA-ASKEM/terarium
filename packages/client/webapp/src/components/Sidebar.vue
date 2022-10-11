<script setup lang="ts">
/**
 * Sidebar component for navigating modes
 * */
import { ref, computed } from 'vue';
import Button from '@/components/Button.vue';
import IconDataPlayer32 from '@carbon/icons-vue/es/data-player/32';
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import IconTableSplit32 from '@carbon/icons-vue/es/table--split/32';
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
		<nav>
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
	box-shadow: var(--un-box-shadow-default);
	display: flex;
	height: 100%;
	z-index: var(--un-z-index-sidebar);
}

section.right {
	order: 1;
	flex-direction: row-reverse;
}

nav {
	display: flex;
	flex-direction: column;
}

ul {
	list-style: none;
}

nav {
	background-color: var(--un-color-accent);
	box-shadow: var(--un-box-shadow-small);
	justify-content: space-between;
	padding: 0.33em 0;
	width: 4rem;
	z-index: calc(var(--un-z-index-sidebar) + 1);
}

nav ul {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
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

.mode-configuration {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	background-color: var(--un-color-accent-light);
	width: 15rem;
	padding: 0.5rem;
}
</style>
