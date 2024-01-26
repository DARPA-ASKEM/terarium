<template>
	<tera-slider
		:content-width="contentWidth"
		tab-width="0"
		direction="right"
		:is-open="Boolean(previewItem)"
	>
		<template v-slot:content>
			<tera-external-publication
				v-if="previewItemResourceType === ResourceType.XDD"
				:xdd-uri="previewItemId"
				:previewLineLimit="3"
				:highlight="searchTerm"
				:feature-config="{ isPreview: true }"
				@close-preview="closePreview"
			/>
			<tera-dataset
				v-else-if="previewItemResourceType === ResourceType.DATASET"
				:asset-id="previewItemId"
				:highlight="searchTerm"
				:feature-config="{ isPreview: true }"
				@close-preview="closePreview"
			/>
			<tera-model
				v-else-if="previewItemResourceType === ResourceType.MODEL"
				:asset-id="previewItemId"
				:highlight="searchTerm"
				:feature-config="{ isPreview: true }"
				@close-preview="closePreview"
			/>
		</template>
	</tera-slider>
</template>

<script setup lang="ts">
import { PropType, computed, ref, watch } from 'vue';
import { ResultType, ResourceType } from '@/types/common';
import { isDocument } from '@/utils/data-util';
import TeraModel from '@/components/model/tera-model.vue';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraSlider from '@/components/widgets/tera-slider.vue';
import TeraExternalPublication from '@/components/documents/tera-external-publication.vue';

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
	// slider-panel props
	selectedSearchItems: {
		type: Array as PropType<ResultType[]>,
		default: () => []
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
	}
});

// store and use copy of previewItem to disconnect it from prop for persistence
const previewItemState = ref(props.previewItem);
const previewItemResourceType = ref<ResourceType | null>(null);

const previewItemId = computed(() => {
	if (!previewItemState.value) return '';
	if (isDocument(previewItemState.value)) {
		return previewItemState.value.gddId;
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
