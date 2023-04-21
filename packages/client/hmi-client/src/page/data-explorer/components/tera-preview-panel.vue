<template>
	<tera-slider
		class="preview-slider"
		:content-width="contentWidth"
		tab-width="0"
		direction="right"
		:is-open="Boolean(previewItem)"
	>
		<template v-slot:content>
			<header>
				<span>{{ previewItemResourceType?.toUpperCase() }}</span>
				<i class="pi pi-times" @click="emit('update:previewItem', null)" />
			</header>
			<section>
				<tera-document
					v-if="previewItemResourceType === ResourceType.XDD"
					:xdd-uri="previewItemId"
					:previewLineLimit="3"
					:project="resources.activeProject"
					:highlight="searchTerm"
					:is-editable="false"
				/>
				<tera-dataset
					v-else-if="previewItemResourceType === ResourceType.DATASET"
					:asset-id="previewItemId"
					:project="resources.activeProject"
					:highlight="searchTerm"
					:is-editable="false"
				/>
				<tera-model
					v-else-if="previewItemResourceType === ResourceType.MODEL"
					:asset-id="previewItemId"
					:project="resources.activeProject"
					:highlight="searchTerm"
					:is-editable="false"
				/>
			</section>
		</template>
		<template v-slot:footerButtons>
			<Button
				v-if="!previewItemSelected"
				label="Add to selected resources"
				@click="emit('toggle-data-item-selected', { item: previewItem })"
				class="toggle-selection"
			/>
			<Button
				v-else
				label="Remove from selected resources"
				@click="emit('toggle-data-item-selected', { item: previewItem })"
				class="toggle-selection p-button-secondary"
			/>
		</template>
	</tera-slider>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { PropType, computed, ref, watch } from 'vue';
import useResourcesStore from '@/stores/resources';
import { ResultType, ResourceType } from '@/types/common';
import { isDocument } from '@/utils/data-util';
import TeraModel from '@/components/models/tera-model.vue';
import TeraDataset from '@/components/dataset/tera-dataset.vue';
import TeraSlider from '@/components/widgets/tera-slider.vue';
import TeraDocument from '@/components/documents/tera-document.vue';

const resources = useResourcesStore();

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

const emit = defineEmits(['update:previewItem', 'toggle-data-item-selected']);

watch(
	() => props.previewItem,
	(previewItem) => {
		if (previewItem) {
			previewItemState.value = previewItem;
			previewItemResourceType.value = props.resourceType;
		}
	}
);

const previewItemId = computed(() => {
	if (!previewItemState.value) return '';
	if (isDocument(previewItemState.value)) {
		return previewItemState.value.gddId;
	}
	return previewItemState.value.id as string;
});

const previewItemSelected = computed(() =>
	props.selectedSearchItems.some((selectedItem) => selectedItem === props.previewItem)
);
</script>

<style scoped>
header {
	display: flex;
	align-items: center;
	margin: 1rem;
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
