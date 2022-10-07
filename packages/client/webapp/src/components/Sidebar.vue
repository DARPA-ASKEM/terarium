<script setup lang="ts">
import { ref } from 'vue';
import Button from '@/components/Button.vue';
import IconDataPlayer32 from '@carbon/icons-vue/es/data-player/32';
import IconMachineLearningModel32 from '@carbon/icons-vue/es/machine-learning-model/32';
import IconTableSplit32 from '@carbon/icons-vue/es/table--split/32';
import IconSearchLocate32 from '@carbon/icons-vue/es/search--locate/32';
import IconProvenanceGraph32 from '@carbon/icons-vue/es/flow/32';
import IconUser32 from '@carbon/icons-vue/es/user/32';
import IconLogout16 from '@carbon/icons-vue/es/logout/16';
import { useAuthStore } from '../stores/auth';

const auth = useAuthStore();

const selectedMode = ref('Simulation Plan');
const isCollapsed = ref(false);

// Get sidebar position if saved in local storage
const sidebarPosition = ref(localStorage.sidebarPosition ? localStorage.sidebarPosition : 'left');

const logout = () => {
	// Later move mode specific features into their own components
	auth.logout();
	window.location.assign('/logout');
};

function updateMode(mode: string) {
	if (mode === selectedMode.value) {
		isCollapsed.value = !isCollapsed.value;
	} else {
		selectedMode.value = mode;
		isCollapsed.value = false;
	}
}

function moveSidebar() {
	localStorage.sidebarPosition = sidebarPosition.value === 'left' ? 'right' : 'left';
	sidebarPosition.value = localStorage.sidebarPosition;
}
</script>

<template>
	<section :class="sidebarPosition">
		<nav>
			<ul>
				<li :active="selectedMode === 'Simulation Plan'" @click="updateMode('Simulation Plan')">
					<IconDataPlayer32 />
				</li>
				<li :active="selectedMode === 'Model View'" @click="updateMode('Model View')">
					<IconMachineLearningModel32 />
				</li>
				<li :active="selectedMode === 'Datasets'" @click="updateMode('Datasets')">
					<IconTableSplit32 />
				</li>
				<li :active="selectedMode === 'Data Explorer'" @click="updateMode('Data Explorer')">
					<IconSearchLocate32 />
				</li>
			</ul>
			<ul>
				<li :active="selectedMode === 'Provenace Graph'" @click="updateMode('Provenace Graph')">
					<IconProvenanceGraph32 />
				</li>
				<li :active="selectedMode === 'Profile'" @click="updateMode('Profile')">
					<IconUser32 />
				</li>
			</ul>
		</nav>
		<div v-if="!isCollapsed" class="mode-configuration">
			<header>{{ selectedMode }}</header>
			<div v-if="selectedMode === 'Profile'">
				<Button @click="moveSidebar"> Move sidebar </Button>
				<Button @click="logout">Logout <IconLogout16 /></Button>
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
	background-color: var(--un-color-accent);
}

ul {
	list-style: none;
}

nav {
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
