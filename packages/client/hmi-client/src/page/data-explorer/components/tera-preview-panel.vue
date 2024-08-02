<template>
	<tera-slider content-width="100%" tab-width="0" direction="right" :is-open="Boolean(previewItem)">
		<template v-slot:content>
			<template v-if="previewItemResourceType === ResourceType.DOCUMENT">
				<tera-document-asset
					:asset-id="previewItemId"
					:previewLineLimit="10"
					:feature-config="{ isPreview: true }"
					@close-preview="closePreview"
				/>
			</template>
			<tera-dataset
				v-else-if="previewItemResourceType === ResourceType.DATASET"
				:asset-id="previewItemId"
				:feature-config="{ isPreview: true }"
				:source="source"
				@close-preview="closePreview"
			/>
			<tera-model
				v-else-if="previewItemResourceType === ResourceType.MODEL"
				:asset-id="previewItemId"
				:feature-config="{ isPreview: true }"
				@close-preview="closePreview"
			/>
		</template>
	</tera-slider>
</template>

<script setup lang="ts">
import { computed, PropType, ref, watch } from 'vue';
import { ResourceType, ResultType } from '@/types/common';
import { DatasetSource } from '@/types/search';
import type { Source } from '@/types/search';
import TeraModel from '@/components/model/tera-model.vue';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraSlider from '@/components/widgets/tera-slider.vue';
import TeraDocumentAsset from '@/components/documents/tera-document-asset.vue';
import { Dataset } from '@/types/Types';

const props = defineProps({
	// slider props
	direction: {
		type: String,
		default: 'right'
	},
	contentWidth: {
		type: String,
		default: '25%'
	},
	tabWidth: {
		type: String,
		default: '0'
	},
	previewItem: {
		type: Object as PropType<ResultType | null>,
		default: null
	},
	resourceType: {
		type: String as PropType<ResourceType>,
		default: null
	},
	searchTerm: {
		type: String,
		default: null
	},
	source: {
		type: String as PropType<Source>,
		default: DatasetSource.TERARIUM
	}
});

// store and use copy of previewItem to disconnect it from prop for persistence
const previewItemState = ref(props.previewItem);
const previewItemResourceType = ref<ResourceType | null>(null);

const previewItemId = computed(() => {
	if (!previewItemState.value) return '';
	if (previewItemResourceType.value === ResourceType.DATASET && props.source === DatasetSource.ESGF) {
		const dataset: Dataset = previewItemState.value as Dataset;
		return dataset.esgfId as string;
	}
	return previewItemState.value.id as string;
});

const emit = defineEmits(['update:previewItem']);

watch(
	() => props.previewItem,
	(previewItem) => {
		if (previewItem) {
			previewItemState.value = previewItem;
			previewItemResourceType.value = props.resourceType;
		}
	}
);

function closePreview() {
	emit('update:previewItem', null);
}
</script>

<style scoped>
header {
	display: flex;
	align-items: center;
	margin: 1rem;
	margin-bottom: 0;
	font-size: 14px;
	color: var(--text-color-subdued);
	font-weight: bold;
	justify-content: space-between;
}

i {
	cursor: pointer;
}

.toggle-selection {
	margin-left: 1rem;
}

footer button {
	margin: 0 3px;
}
</style>
