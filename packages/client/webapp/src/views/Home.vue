<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconList24 from '@carbon/icons-vue/es/list/24';
// import IconTime32 from '@carbon/icons-vue/es/time/32';
// import IconFire32 from '@carbon/icons-vue/es/fire/32';
// import IconCoronavirus32 from '@carbon/icons-vue/es/coronavirus/32';
// import IconAddFilled32 from '@carbon/icons-vue/es/add--filled/32';
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
			<h3><IconTime32 />{{ category }}</h3>
			<div class="project-carousel">
				<div class="project" v-for="(project, p) in dummyProjects" :key="p">
					<!-- <IconAddFilled32 v-if="p === 0" /> -->
					<div class="details">
						<div class="name">{{ project }} {{ p }}</div>
						<div class="actions">
							<IconList24 />
						</div>
					</div>
				</div>
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
	width: 80%;
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
	margin-bottom: 0.5rem;
	padding-left: 1rem;
}

h3 {
	font-size: 2rem;
	font-weight: 400;
	display: flex;
	align-items: center;
	padding: 1.5rem 0 0.5rem 0;
}

h3 svg {
	margin: 0 0.5rem;
	color: var(--un-color-accent);
}

.project-carousel {
	display: flex;
	overflow-x: scroll;
	overflow-y: hidden;
}

.project-carousel .project {
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
	height: 15rem;
	min-width: 20rem;
	border: 1px solid var(--un-color-black-20);
	background-color: var(--un-color-black-5);
	border-radius: 10px;
	margin: 0.5rem;
	transition: 0.2s;
	text-align: left;
}

.project-carousel .project .details {
	border-top: 1px solid var(--un-color-black-20);
}

.details .name,
.details .actions {
	font-weight: 500;
	padding: 0.5rem 1rem;
}

.details .actions {
	/* display: none; */
	padding: 0 0 0.25rem 1rem;
}

.details .actions svg:hover {
	color: var(--un-color-accent);
	cursor: pointer;
}

.project-carousel .project svg {
	color: var(--un-color-black-80);
	margin: auto;
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
