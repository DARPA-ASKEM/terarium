<template>
	<ul v-if="store.activeProjectAssets !== null">
		<template v-for="resource in resources">
			<li v-for="(asset, index) in store.activeProjectAssets[resource.projectAsset]" :key="index">
				<component :is="resource.icon" />
				<header>{{ resource.name }}{{ asset.name }}</header>
			</li>
		</template>
	</ul>
</template>

<script setup lang="ts">
import useResourcesStore from '@/stores/resources';

export type Resource = {
	route: string;
	name: string;
	icon: any;
	projectAsset: string;
};

const store = useResourcesStore();

defineProps<{
	resources: Resource[];
}>();
</script>

<style scoped>
ul {
	display: flex;
	flex-direction: column;
	list-style: none;
	gap: 1rem;
	width: 20rem;
	overflow-y: scroll;
	height: 100vh;
}

li {
	display: flex;
	border: 2px solid var(--un-color-black-100);
	border-radius: 0.25rem;
	cursor: pointer;
}

svg {
	width: 100%;
	margin: auto;
}

header {
	font-weight: bold;
	border-left: 1px solid var(--un-color-black-100);
}
</style>
