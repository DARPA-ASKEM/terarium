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
		<nav class="mode-selection">
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
	border-right: 1px solid lightgrey;
}

section.right nav {
	border-left: 1px solid lightgrey;
	border-right: 0;
}

ul {
	list-style: none;
}

nav.mode-selection {
	justify-content: space-between;
	width: 4rem;
}

nav.mode-selection ul li {
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	height: 3rem;
	width: 3rem;
	margin: 0.5rem auto;
	border-radius: 5px;
	background-color: whitesmoke;
	color: black;
}

nav.mode-selection ul li:hover,
nav.mode-selection ul li[active] {
	background-color: white;
	color: green;
}

.mode-configuration {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	background-color: var(--un-color-accent-lighter);
	width: 15rem;
	padding: 0.5rem;
}
</style>
