<script setup lang="ts">
import { Project } from '@/types/Project';
import { RouteName } from '@/router/routes';
import { RouteParamsRaw, useRouter } from 'vue-router';

export type ResourceType = {
	route: RouteName;
	params: RouteParamsRaw;
	name: string;
	icon: any;
	projectAsset: Project;
};

defineProps<{
	resource: ResourceType;
}>();

const router = useRouter();

function openResource(name: RouteName, params: RouteParamsRaw) {
	router.push({ name, params });
}
</script>

<template>
	<div class="resource" @click="openResource(resource.route, resource.params)">
		<component :is="resource.icon" />
		<div class="resource-details">
			<header>
				{{ resource.projectAsset.name }}
				{{ resource.projectAsset.title }}
			</header>
			<footer>
				{{ resource.route.charAt(0).toUpperCase() + resource.route.slice(1) }} - Last date accessed
			</footer>
		</div>
	</div>
</template>

<style scoped>
.resource {
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

.resource-details {
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
