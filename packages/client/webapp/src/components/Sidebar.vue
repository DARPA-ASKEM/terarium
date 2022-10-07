<script setup lang="ts">
import { ref } from 'vue';
import Button from '@/components/Button.vue';
import SimulationPlan from '@carbon/icons-vue/es/data-player/32';
import MachineLearningModel from '@carbon/icons-vue/es/machine-learning-model/32';
import Dataset from '@carbon/icons-vue/es/table--split/32';
import ProvenanceGraph from '@carbon/icons-vue/es/flow/32';
import Search from '@carbon/icons-vue/es/search--locate/32';
import User from '@carbon/icons-vue/es/user/32';

const props = defineProps<{ sidebarPosition: String }>();
const emit = defineEmits(['updateSidebarPosition']);

const mode = ref('Simulation Plan');
const collapsed = ref(false);

function updateMode(modeOption: string) {
	if (modeOption === mode.value) {
		collapsed.value = !collapsed.value;
		return;
	}
	collapsed.value = false;
	mode.value = modeOption;
}

function moveSidebar() {
	if (localStorage.sidebarPosition === 'left') {
		localStorage.setItem('sidebarPosition', 'right');
	} else {
		localStorage.setItem('sidebarPosition', 'left');
	}
	emit('updateSidebarPosition', localStorage.sidebarPosition);
}

function styleModeOption(modeOption: string) {
	if (mode.value === modeOption) return 'chosenMode';
	return '';
}
</script>

<template>
	<section :class="props.sidebarPosition">
		<nav class="mode-selection">
			<ul>
				<li :class="styleModeOption('Simulation Plan')" @click="updateMode('Simulation Plan')">
					<SimulationPlan />
				</li>
				<li :class="styleModeOption('Model View')" @click="updateMode('Model View')">
					<MachineLearningModel />
				</li>
				<li :class="styleModeOption('Datasets')" @click="updateMode('Datasets')">
					<Dataset />
				</li>
				<li :class="styleModeOption('Data Explorer')" @click="updateMode('Data Explorer')">
					<Search />
				</li>
			</ul>
			<ul>
				<li :class="styleModeOption('Provenace Graph')" @click="updateMode('Provenace Graph')">
					<ProvenanceGraph />
				</li>
				<li :class="styleModeOption('Profile')" @click="updateMode('Profile')">
					<User />
				</li>
			</ul>
		</nav>
		<nav v-if="!collapsed" class="mode-configuration">
			<header>{{ mode }}</header>
			<div>
				<Button v-if="mode === 'Profile'" @click="moveSidebar()"> Move sidebar </Button>
			</div>
		</nav>
	</section>
</template>

<style scoped>
section {
	display: flex;
	height: 100%;
}

section.right {
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
nav.mode-selection ul li.chosenMode {
	background-color: white;
	color: green;
}

nav.mode-configuration {
	background-color: var(--un-color-accent-lighter);
	width: 15rem;
}

nav.mode-configuration {
	padding: 0.5rem;
}
</style>
