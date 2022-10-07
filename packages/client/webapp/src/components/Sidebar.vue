<script setup lang="ts">
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

const auth = useAuthStore();

const selectedMode = ref('');
// Get sidebar position if saved in local storage
const sidebarPosition = ref(
	localStorage.getItem('sidebarPosition') ? localStorage.getItem('sidebarPosition') : 'left'
);
const isCollapsed = computed(() => selectedMode.value.length === 0);

const logout = () => {
	// Later move mode specific features into their own components
	auth.logout();
	window.location.assign('/logout');
};

function updateMode(mode: string) {
	selectedMode.value = mode === selectedMode.value ? '' : mode;
}

function moveSidebar() {
	localStorage.setItem('sidebarPosition', sidebarPosition.value === 'left' ? 'right' : 'left');
	sidebarPosition.value = localStorage.getItem('sidebarPosition');
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
	width: 15rem;
	padding: 0.5rem;
}
</style>
