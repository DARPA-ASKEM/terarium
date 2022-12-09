<template>
	<ul>
		<li
			v-for="(resource, index) in resources"
			:key="index"
			@click="openResource(resource.route, resource.params)"
		>
			<component :is="resource.icon" />
			<div>
				<header>
					{{ resource.projectAsset.name }}
					{{ resource.projectAsset.title }}
				</header>
				<footer>{{ resource.route }} - {{ resource.projectAsset.timestamp }}</footer>
			</div>
		</li>
	</ul>
</template>

<script setup lang="ts">
import { Project } from '@/types/Project';
import { RouteName } from '@/router/routes';
import { RouteParamsRaw, useRouter } from 'vue-router';

export type Resource = {
	route: RouteName;
	params: RouteParamsRaw;
	name: string;
	icon: any;
	projectAsset: Project | any;
};

defineProps<{
	resources: Resource[];
}>();

const router = useRouter();

function openResource(view: RouteName, params: RouteParamsRaw) {
	router.push({ name: view, params });
}
</script>

<style scoped>
ul {
	display: grid;
	grid-template-columns: 1fr 1fr;
	list-style: none;
	gap: 1rem;
	margin: 1rem 0;
	color: var(--un-color-body-text-secondary);
	width: fit-content;
}

li {
	width: 25rem;
	display: flex;
	border: 2px solid var(--un-color-black-100);
	border-radius: 0.25rem;
	cursor: pointer;
	border: 1px solid var(--un-color-body-stroke);
}

svg {
	width: 20%;
	margin: auto;
	color: var(--un-color-accent);
}

div {
	width: 80%;
	border-left: 1px solid var(--un-color-body-stroke);
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	padding: 0.5rem;
}

header {
	font-weight: bold;
	font-size: 1.1rem;
	color: var(--un-color-body-text-primary);
	/* max-height: 3rem; */
}
</style>
