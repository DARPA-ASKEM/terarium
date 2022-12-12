<script setup lang="ts">
import { Project, ProjectAssetTypes } from '@/types/Project';
import { RouteMetadata, RouteName } from '@/router/routes';
import { Resource } from '@/types/Resource';
import ResourcesList from '@/components/resources/resources-list.vue';
import useResourcesStore from '@/stores/resources';

const props = defineProps<{
	project: Project;
	resourceRoute?: RouteName;
	// maybe add list size later
}>();

const emit = defineEmits(['show-data-explorer']);
const goToDataExplorer = () => emit('show-data-explorer');

const store = useResourcesStore();
const resources: Resource[] = [];

const filteredRouteMetadata = props?.resourceRoute
	? Object.fromEntries(
			Object.entries(RouteMetadata).filter(([key]) => key.includes(props.resourceRoute))
	  )
	: RouteMetadata;

// Not sure how to get types to work here
const routeMetadataArray: [
	RouteName,
	{ displayName: string; icon: any; projectAsset?: ProjectAssetTypes }
] = Object.entries(filteredRouteMetadata);

for (let i = 0; i < routeMetadataArray.length; i++) {
	if (routeMetadataArray[i][1].projectAsset && store.activeProjectAssets !== null) {
		const assets = store.activeProjectAssets[routeMetadataArray[i][1].projectAsset];
		for (let j = 0; j < assets.length; j++) {
			resources.push({
				route: routeMetadataArray[i][0],
				params: {
					projectId: props?.project?.id,
					assetId:
						routeMetadataArray[i][0] === RouteName.DocumentRoute ? assets[j].xdd_uri : assets[j].id
				},
				name: routeMetadataArray[i][1].displayName,
				icon: routeMetadataArray[i][1].icon,
				projectAsset: assets[j]
			});
			if (resources.length === 10) break; // Limit amount of resources
		}
	}
	if (resources.length === 10) break; // Limit amount of resources
}

// console.log(RouteMetadata)
// console.log(routeMetadataArray);
// console.log(store.activeProjectAssets);
// console.log(resources);
</script>

<template>
	<h3>Recent Resources</h3>
	<resources-list v-if="resources.length > 0" :resources="resources" />
	<p v-else>
		Find Models, Datasets, or Papers with the
		<a @click="goToDataExplorer"> Data Explorer </a>
	</p>
</template>

<style scoped></style>
