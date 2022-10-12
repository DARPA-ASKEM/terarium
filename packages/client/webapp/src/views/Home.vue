<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconTime32 from '@carbon/icons-vue/es/time/32';
import IconFire32 from '@carbon/icons-vue/es/fire/32';
import IconCoronavirus32 from '@carbon/icons-vue/es/coronavirus/32';
import IconAddFilled32 from '@carbon/icons-vue/es/add--filled/32';
import { uncloak } from '../utils/uncloak';

const dummyProjects = Array(8).fill({ name: 'New Project' });

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
		<h3><IconTime32 />Recents</h3>
		<div class="project-carousel">
			<div v-for="{ project, p } in dummyProjects" v-bind:key="p">
				{{ project.name }}
				<!-- <IconAddFilled32 /> -->
			</div>
		</div>
		<h3><IconFire32 />Trending</h3>
		<div class="project-carousel">
			<div v-for="{ project, p } in dummyProjects" v-bind:key="p">
				{{ project }}
				<IconAddFilled32 />
			</div>
		</div>
		<h3><IconCoronavirus32 />Epidemiology</h3>
		<div class="project-carousel">
			<div v-for="{ project, p } in dummyProjects" v-bind:key="p">
				{{ project }}
				<IconAddFilled32 />
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
h2,
h3 {
	color: var(--un-color-black-80);
	font-weight: 500;
	text-align: left;
}

h2 {
	border-bottom: 2px solid var(--un-color-black-20);
	font-size: 2.5rem;
	padding: 1rem 0 1rem 0;
	margin-bottom: 0.5rem;
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

section {
	margin: 1rem auto;
	width: 75%;
	text-align: center;
}

.project-carousel {
	display: flex;
	overflow-x: scroll;
	overflow-y: hidden;
}

.project-carousel div {
	height: 15rem;
	min-width: 20rem;
	border: 1px solid var(--un-color-black-20);
	background-color: var(--un-color-black-5);
	border-radius: 10px;
	margin: 0.25rem 0.5rem;
}

.project-carousel div svg {
	color: var(--un-color-black-30);
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
