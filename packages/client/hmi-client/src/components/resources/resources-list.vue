<script setup lang="ts">
import { Project, ProjectAssetTypes } from '@/types/Project';
import { RouteMetadata, RouteName } from '@/router/routes';
import { Resource } from '@/types/Resource';
import ResourceCard from '@/components/resources/ResourceCard.vue';
import useResourcesStore from '@/stores/resources';
import { RouteParamsRaw, useRouter } from 'vue-router';

const props = defineProps<{
	project: Project;
	resourceRoute?: RouteName;
	// maybe add list size later
}>();

const emit = defineEmits(['show-data-explorer']);
const goToDataExplorer = () => emit('show-data-explorer');

const router = useRouter();
const store = useResourcesStore();

const resources: Resource[] = [];

const filteredRouteMetadata: {
	route: RouteName;
	metadata: { displayName: string; icon: any; projectAsset?: ProjectAssetTypes };
}[] = props.resourceRoute
	? [{ route: props.resourceRoute, metadata: RouteMetadata[props.resourceRoute] }]
	: Object.entries(RouteMetadata)
			.map(([route, metadata]) => [{ route, metadata }])
			.flat();

for (let i = 0; i < filteredRouteMetadata.length; i++) {
	const route = filteredRouteMetadata[i].route;
	const { displayName, icon, projectAsset } = filteredRouteMetadata[i].metadata;
	if (projectAsset && store.activeProjectAssets !== null) {
		const assets = store.activeProjectAssets[projectAsset];
		for (let j = 0; j < assets.length; j++) {
			resources.push({
				route,
				params: {
					projectId: props?.project.id,
					assetId: route === RouteName.DocumentRoute ? assets[j].xdd_uri : assets[j].id
				},
				name: displayName,
				icon,
				projectAsset: assets[j]
			});
			if (resources.length === 10) break; // Limit amount of resources
		}
		if (resources.length === 10) break; // Limit amount of resources
	}
}

function openResource(name: RouteName, params: RouteParamsRaw) {
	router.push({ name, params });
}
</script>

<template>
	<h3>Recent Resources</h3>
	<ul v-if="resources.length > 0">
		<li
			v-for="(resource, index) in resources"
			:key="index"
			@click="openResource(resource.route, resource.params)"
		>
			<resource-card :resource="resource" />
		</li>
	</ul>
	<p v-else>
		Find Models, Datasets, or Papers with the
		<a @click="goToDataExplorer"> Data Explorer </a>
	</p>
</template>

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
</style>
