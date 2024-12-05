<template>
	<Button
		:title="`${asset.assetName} — ${getElapsedTimeText(asset.createdOn)}`"
		class="asset-button"
		plain
		text
		size="small"
	>
		<span>
			<tera-asset-icon :asset-type="asset.assetType" />
			<span class="p-button-label">{{ asset.assetName }}</span>
		</span>
	</Button>
</template>

<script setup lang="ts">
import { getElapsedTimeText } from '@/utils/date';
import { ProjectAsset } from '@/types/Types';
import Button from 'primevue/button';
import TeraAssetIcon from '@/components/widgets/tera-asset-icon.vue';

defineProps<{
	asset: ProjectAsset;
}>();
</script>
