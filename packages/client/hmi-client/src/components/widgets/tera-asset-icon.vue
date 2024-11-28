<template>
	<vue-feather
		v-if="typeof getAssetIcon(assetType ?? null) === 'string'"
		class="p-button-icon-left icon"
		:type="getAssetIcon(assetType ?? null)"
		size="1rem"
	/>
	<component v-else :is="getAssetIcon(assetType ?? null)" :fill="customFill" class="p-button-icon-left icon" />
</template>

<script setup lang="ts">
import { getAssetIcon } from '@/services/project';
import { AssetType } from '@/types/Types';

defineProps<{
	assetType: AssetType;
	customFill?: string;
}>();
</script>
