<script setup lang="ts">
import { Resource } from '@/types/Resource';
import { RouteName } from '@/router/routes';
import { RouteParamsRaw, useRouter } from 'vue-router';

defineProps<{
	resource: Resource;
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
	height: 6rem;
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
	justify-content: space-between;
	gap: 0.5rem;
	padding: 0.5rem;
}

header {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
	font-weight: bold;
	font-size: 1.1rem;
	color: var(--un-color-body-text-primary);
}

footer {
	font-size: 1rem;
}
</style>
