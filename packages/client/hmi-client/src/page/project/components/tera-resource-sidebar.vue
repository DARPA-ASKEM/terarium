<template>
	<nav>
		<div>
			<Button icon="pi pi-file-edit" class="p-button-icon-only p-button-text p-button-rounded" />
			<Button icon="pi pi-folder" class="p-button-icon-only p-button-text p-button-rounded" />
			<Button
				icon="pi pi-sort-amount-down"
				class="p-button-icon-only p-button-text p-button-rounded"
			/>
			<Button icon="pi pi-arrows-v" class="p-button-icon-only p-button-text p-button-rounded" />
		</div>
		<Tree :value="resources" selectionMode="single" v-on:node-select="openResource">
			<template #default="slotProps">
				{{ slotProps.node.label }}
				<Chip :label="slotProps.node.data.resourceType" />
			</template>
		</Tree>
	</nav>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { isEmpty } from 'lodash';
import { ProjectType } from '@/types/Project';
import { RouteName } from '@/router/routes';
import useResourcesStore from '@/stores/resources';
import { useRouter } from 'vue-router';
import Tree from 'primevue/tree';
import Button from 'primevue/button';
import Chip from 'primevue/chip';
import { DocumentAsset } from '@/types/Document';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';

const router = useRouter();
const resourcesStore = useResourcesStore();

defineProps<{
	project: ProjectType | null;
}>();

const resources = computed(() => {
	const storedResources = resourcesStore.activeProjectAssets ?? [];
	const projectAssetTypes = Object.keys(storedResources);
	const resourceTreeNodes: any[] = [];

	console.log(storedResources);

	if (!isEmpty(storedResources)) {
		for (let i = 0; i < projectAssetTypes.length; i++) {
			const assets: (DocumentAsset & Model & Dataset)[] =
				Object.values(storedResources[projectAssetTypes[i]]) ?? [];
			for (let j = 0; j < assets.length; j++) {
				resourceTreeNodes.push({
					key: assets[j]?.name || assets[j]?.title,
					label: assets[j]?.name || assets[j]?.title,
					data: {
						resourceType: projectAssetTypes[i],
						assetId: projectAssetTypes[i] === 'publications' ? assets[j].xdd_uri : assets[j]?.id
					},
					selectable: true
				});
			}
		}
	}
	return resourceTreeNodes;
});

function openResource(event: any) {
	console.log(event);

	router.push({
		name: RouteName.ProjectRoute,
		params: {
			resourceName: event.key,
			assetId: event.data.assetId,
			resourceType: event.data.resourceType
		}
	});
}
</script>

<style scoped>
nav {
	display: flex;
	flex-direction: column;
	margin: 0.75rem;
	margin-top: 0;
	gap: 1rem;
}

.p-chip {
	padding: 0 0.5rem;
	border-radius: 0.5rem;
	text-transform: uppercase;
}

/* nav div {
    margin-left: -0.5rem;
} */
</style>
