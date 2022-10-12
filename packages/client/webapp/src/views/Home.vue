<script setup lang="ts">
import Button from '@/components/Button.vue';
import IconAddFilled32 from '@carbon/icons-vue/es/add--filled/32';
import { uncloak } from '../utils/uncloak';

const dummyProjects = Array(8).fill('New Project');

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
		<div class="project-carousel">
			<div v-for="{ project, p } in dummyProjects" v-bind:key="p">
				{{ project }}
				<IconAddFilled32 />
			</div>
		</div>
		<div class="test">
			<h3>Recent</h3>
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
h2 {
	font-size: 2rem;
	font-weight: 600;
	display: block;
}

section {
	margin: 3rem auto;
	width: 80%;
	text-align: center;
}

.project-carousel {
	display: flex;
	overflow-x: scroll;
	overflow-y: hidden;
}

.project-carousel div {
	height: 120px;
	width: 300px;
	border: 1px solid var(--un-color-black-20);
	background-color: var(--un-color-black-5);
	border-radius: 10px;
	margin: 0.5rem;
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
