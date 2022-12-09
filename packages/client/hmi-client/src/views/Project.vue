<script setup lang="ts">
import { Project } from '@/types/Project';
import { ProjectAssetTypes, RouteMetadata, RouteName } from '@/router/routes';
import ResourcesList, { Resource } from '@/components/resources-list.vue';
import useResourcesStore from '@/stores/resources';

const props = defineProps<{
	project: Project;
}>();

const store = useResourcesStore();
const projectAssets = [] as Resource[];

// Not sure how to get types to work here
const routeMetadataArray: [
	RouteName,
	{ displayName: string; icon: any; projectAsset?: ProjectAssetTypes }
] = Object.entries(RouteMetadata);

for (let i = 0; i < routeMetadataArray.length; i++) {
	if (routeMetadataArray[i][1].projectAsset && store.activeProjectAssets !== null) {
		const assets = store.activeProjectAssets[routeMetadataArray[i][1].projectAsset];
		for (let j = 0; j < assets.length; j++) {
			projectAssets.push({
				route: routeMetadataArray[i][0],
				params: { projectId: props?.project?.id, modelId: assets[j].id ?? 1 }, // Second attribute is hardcoded for now
				name: routeMetadataArray[i][1].displayName,
				icon: routeMetadataArray[i][1].icon,
				projectAsset: assets[j]
			});
			if (projectAssets.length === 10) break; // Limit amount of resources
		}
	}
	if (projectAssets.length === 10) break; // Limit amount of resources
}
console.log(routeMetadataArray);
console.log(store.activeProjectAssets);
console.log(projectAssets);
</script>

<template>
	<div class="flex-container">
		<header>
			<h2>{{ project?.name }}</h2>
			<p class="secondary-text">Last updated: {{ project?.timestamp }}</p>
		</header>
		<section class="content-container">
			<section class="summary">
				<!-- This div is so that child elements will automatically collapse margins -->
				<div>
					<section class="description">
						<!-- Author -->
						<section class="author">Edwin Lai, Yohann Paris</section>
						<p>
							{{ project?.description }}
						</p>
					</section>
				</div>
			</section>
			<section class="detail">
				<h3>Recent Resources</h3>
				<resources-list :resources="projectAssets" v-if="store.activeProjectAssets !== null" />
			</section>
		</section>
	</div>
</template>

<style scoped>
.flex-container {
	display: flex;
	flex-direction: column;
	width: 100%;
	margin: 0.5rem;
	background: white;
}

.content-container {
	margin-left: 1rem;
	column-gap: 2rem;
	flex-direction: row;
}

header {
	margin: 1rem;
}

section {
	display: flex;
	width: 100%;
	flex-direction: column;
}

.summary {
	flex: 0.25;
}

.author {
	line-height: 1.75rem;
}

.detail {
	flex: 0.75;
}

.summary section,
.detail section {
	margin: 1rem 0;
	display: block;
}

h4 {
	font: var(--un-font-h4);
	margin: 1rem 0;
}

h3 {
	font: var(--un-font-h3);
}

h2 {
	font: var(--un-font-h2);
}

.secondary-text {
	color: var(--un-color-body-text-secondary);
}
</style>
