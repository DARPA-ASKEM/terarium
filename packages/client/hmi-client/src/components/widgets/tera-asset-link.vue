<template>
	<a @click="openAsset(assetRoute)">
		<span v-if="!textOnly">
			<tera-asset-icon :asset-type="assetRoute.pageType as AssetType" />
		</span>
		{{ label }}</a
	>
</template>

<script setup lang="ts">
import { AssetRoute } from '@/types/common';
import router from '@/router';
import { RouteName } from '@/router/routes';
import { AssetType } from '@/types/Types';
import TeraAssetIcon from './tera-asset-icon.vue';

defineProps<{
	assetRoute: AssetRoute;
	label: string;
	textOnly?: boolean;
}>();

const openAsset = (assetRoute: AssetRoute) => {
	router.push({
		name: RouteName.Project,
		params: assetRoute
	});
};
</script>

<style scoped></style>
