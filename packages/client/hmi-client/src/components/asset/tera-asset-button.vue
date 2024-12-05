<template>
	<Button
		v-for="assetItem in assetItems"
		:key="assetItem.assetId"
		:active="assetItem.assetId === assetId && assetItem.pageType === pageType"
		:title="`${assetItem.assetName} — ${getElapsedTimeText(assetItem.assetCreatedOn)}`"
		class="asset-button"
		plain
		text
		size="small"
		@click="
			emit('open-asset', {
				assetId: assetItem.assetId,
				pageType: assetItem.pageType
			})
		"
		@mouseover="activeAssetId = assetItem.assetId"
		@mouseleave="activeAssetId = undefined"
		@focus="activeAssetId = assetItem.assetId"
		@focusout="activeAssetId = undefined"
	>
		<span
			:draggable="
				pageType === AssetType.Workflow &&
				(assetItem.pageType === AssetType.Model ||
					assetItem.pageType === AssetType.Dataset ||
					assetItem.pageType === AssetType.Document)
			"
			@dragstart="startDrag({ assetId: assetItem.assetId, pageType: assetItem.pageType })"
			@dragend="endDrag"
			:class="isEqual(draggedAsset, assetItem) ? 'dragged-asset' : ''"
			fallback-class="original-asset"
			force-fallback
		>
			<tera-asset-icon :asset-type="assetItem.pageType as AssetType" />
			<span class="p-button-label">{{ assetItem.assetName }}</span>
		</span>
		<!-- This 'x' only shows while hovering over the row -->
		<i
			v-if="activeAssetId && activeAssetId === assetItem.assetId"
			class="pi pi-times removeResourceButton"
			@click.stop="
				assetToDelete = assetItem;
				isRemovalModal = true;
			"
		/>
	</Button>
</template>
<script setup lang="ts">
import { getElapsedTimeText } from '@/utils/date';
import { AssetType } from '@/types/Types';
import { isEqual } from 'lodash';
import Button from 'primevue/button';
import TeraAssetIcon from '@/components/widgets/tera-asset-icon.vue';
</script>
