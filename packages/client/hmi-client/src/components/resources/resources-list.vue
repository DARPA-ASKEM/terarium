<script setup lang="ts">
import { IProject } from '@/types/Project';
import { RouteMetadata, RouteName } from '@/router/routes';
import { Resource } from '@/types/Resource';
import { AssetType } from '@/types/common';
import ResourceCard from '@/components/resources/ResourceCard.vue';
import useResourcesStore from '@/stores/resources';
import { RouteParamsRaw, useRouter } from 'vue-router';
import { ref } from 'vue';

const props = defineProps<{
	project: IProject | null;
	resourceRoute?: RouteName;
	// maybe add list size later
}>();

const router = useRouter();
const resourcesStore = useResourcesStore();
const resources = ref<Resource[]>([]);

const filteredRouteMetadata: {
	route: RouteName;
	displayName: string;
	icon: any;
	projectAsset?: AssetType;
}[] = props.resourceRoute
	? [{ route: props.resourceRoute, ...RouteMetadata[props.resourceRoute] }]
	: Object.entries(RouteMetadata)
			.map(([route, metadata]) => [{ route: route as RouteName, ...metadata }])
			.flat();

const assetAmount = props.resourceRoute ? 10 : 2;

function updateResources() {
	const activeProjectAssets = resourcesStore.activeProjectAssets;
	return filteredRouteMetadata
		.filter((metadata) => metadata.projectAsset && activeProjectAssets && props?.project)
		.flatMap((metadata) => {
			const { displayName, icon, projectAsset, route } = metadata;
			const projectId = props.project?.id;
			return (
				activeProjectAssets![projectAsset!]?.slice(0, assetAmount)?.map((asset) => ({
					route,
					params: {
						projectId,
						assetId: route === RouteName.ProjectRoute ? asset.xdd_uri : asset.id
					},
					name: displayName,
					icon,
					projectAsset: asset
				})) ?? []
			);
		});
}

resources.value = updateResources();

resourcesStore.$subscribe(() => {
	resources.value = updateResources();
});

function openResource(name: RouteName, params: RouteParamsRaw) {
	router.push({ name, params });
}

const openDataExplorer = () => {
	router.push({ name: RouteName.DataExplorerRoute });
};
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
	<p>
		Find Models, Datasets, or Documents with the
		<a @click="openDataExplorer()"> Data Explorer </a>
	</p>
</template>

<style scoped>
ul {
	display: grid;
	grid-template-columns: 1fr 1fr;
	list-style: none;
	gap: 1rem;
	margin: 1rem 0;
	color: var(--text-color-secondary);
	width: fit-content;
}

a {
	text-decoration: underline;
}
</style>
