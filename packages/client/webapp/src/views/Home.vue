<script setup lang="ts">
import Button from '@/components/Button.vue';
import ProjectCard from '@/components/ProjectCard.vue';
import IconTime32 from '@carbon/icons-vue/es/time/32';
import IconChevronLeft32 from '@carbon/icons-vue/es/chevron--left/32';
import IconChevronRight32 from '@carbon/icons-vue/es/chevron--right/32';
import IconFire32 from '@carbon/icons-vue/es/fire/32';
import IconCoronavirus32 from '@carbon/icons-vue/es/coronavirus/32';

import { uncloak } from '../utils/uncloak';

const projectCategories: string[] = ['Recents', 'Trending', 'Epidemiology'];
const dummyProjects: string[] = Array(8).fill('Project');

async function apiCall(type = '') {
	if (type === 'user') {
		const data = await uncloak('/api/user/me');
		if (data === null) {
			console.log('Unauthorized Access');
		}
		console.log(`Response: ${JSON.stringify(data)}`);
	}
	if (type === 'admin') {
		const data = await uncloak('/api/admin');
		if (data === null) {
			console.log('Unauthorized Access');
		}
		console.log(`Response: ${JSON.stringify(data)}`);
	}
}
</script>

<template>
	<section>
		<h2>Projects</h2>
		<div v-for="(category, c) in projectCategories" :key="c">
			<h3>
				<IconTime32 v-if="category === 'Recents'" />
				<IconFire32 v-else-if="category === 'Trending'" />
				<IconCoronavirus32 v-else-if="category === 'Epidemiology'" />
				{{ category /** Maybe a better way to do the above */ }}
			</h3>
			<div class="project-carousel-container">
				<IconChevronLeft32 />
				<div class="project-carousel">
					<ProjectCard
						v-for="(projectName, p) in dummyProjects"
						:key="p"
						:category="category"
						:projectName="projectName + ' ' + p"
						:isNewProjectCard="p === 0 && category === 'Recents'"
					/>
				</div>
				<IconChevronRight32 />
			</div>
		</div>
		<div class="test">
			<p>Test API calls/Home placeholder</p>
			<div>
				<Button>Normal</Button>
				<Button action>Action</Button>
				<Button danger>Danger</Button>
				<Button info>Info</Button>
			</div>
			<div>
				<Button @click="apiCall('user')">User Call</Button>
				<Button @click="apiCall('admin')">Admin Call</Button>
			</div>
		</div>
	</section>
</template>

<style scoped>
section {
	color: var(--un-color-black-80);
	margin: 1rem auto;
	width: 95%;
	text-align: center;
}

h2,
h3 {
	font-weight: 500;
	text-align: left;
}

h2 {
	border-bottom: 2px solid var(--un-color-black-20);
	font-size: 2.5rem;
	padding: 1rem 0 1rem 0;
	margin: 0 3rem 0.5rem 3rem;
	padding-left: 1rem;
}

h3 {
	font-size: 2rem;
	margin-left: 4rem;
	font-weight: 400;
	display: flex;
	align-items: center;
	padding: 1.5rem 0 0.5rem 0;
}

h3 svg {
	margin-right: 0.5rem;
	color: var(--un-color-accent);
}

.project-carousel-container {
	display: flex;
	align-items: center;
}

.project-carousel-container svg {
	min-width: max-content;
	margin: 0.5rem;
	margin-bottom: 1rem;
	cursor: pointer;
	visibility: hidden;
}

.project-carousel-container svg:hover {
	background-color: var(--un-color-black-5);
	color: var(--un-color-accent);
	border-radius: 10rem;
}

.project-carousel-container:hover .project-carousel {
	height: 22rem;
}

.project-carousel-container:hover svg {
	visibility: visible;
}

.project-carousel {
	display: flex;
	height: 18rem;
	transition: 0.2s;
	align-items: center;
	overflow-x: auto;
	overflow-y: hidden;
}

.test {
	margin-top: 4rem;
}

.test div {
	display: flex;
	gap: 1em;
	margin: 2em;
	justify-content: center;
}
</style>
