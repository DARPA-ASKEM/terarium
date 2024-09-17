<template>
	<a @click="openAsset(assetRoute, routeQuery)">
		<span v-if="showIcon">
			<tera-asset-icon :asset-type="assetRoute.pageType as AssetType" />
		</span>
		{{ label }}
	</a>
</template>

<script setup lang="ts">
import { LocationQueryRaw } from 'vue-router';
import { AssetRoute } from '@/types/common';
import router from '@/router';
import { RouteName } from '@/router/routes';
import { AssetType } from '@/types/Types';
import TeraAssetIcon from './tera-asset-icon.vue';

defineProps<{
	assetRoute: AssetRoute;
	routeQuery?: LocationQueryRaw;
	label: string;
	showIcon?: boolean;
}>();

const openAsset = (assetRoute: AssetRoute, query: LocationQueryRaw = {}) => {
	router.push({
		name: RouteName.Project,
		params: assetRoute,
		query
	});
};
</script>

<style scoped></style>
