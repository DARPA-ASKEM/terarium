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

const router = useRouter();
const activeProjectAssets = useResourcesStore().activeProjectAssets;

const resources: Resource[] = [];

const filteredRouteMetadata: {
	route: RouteName;
	displayName: string;
	icon: any;
	projectAsset?: ProjectAssetTypes;
}[] = props.resourceRoute
	? [{ route: props.resourceRoute, ...RouteMetadata[props.resourceRoute] }]
	: Object.entries(RouteMetadata)
			.map(([route, metadata]) => [{ route: route as RouteName, ...metadata }])
			.flat();

console.log(activeProjectAssets);

const assetAmount = props.resourceRoute ? 10 : 2;

filteredRouteMetadata.forEach((metadata) => {
	const route = metadata.route;
	const { displayName, icon, projectAsset } = metadata;
	if (projectAsset && activeProjectAssets !== null) {
		const assets = activeProjectAssets[projectAsset].slice(0, assetAmount);
		assets.forEach((asset) => {
			resources.push({
				route,
				params: {
					projectId: props?.project.id,
					assetId: route === RouteName.DocumentRoute ? asset.xdd_uri : asset.id
				},
				name: displayName,
				icon,
				projectAsset: asset
			});
		});
	}
});

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
		<a @click="emit('show-data-explorer')"> Data Explorer </a>
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

a {
	text-decoration: underline;
}
</style>
