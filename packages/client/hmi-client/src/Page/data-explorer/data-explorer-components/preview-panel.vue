<template>
	<slider
		class="preview-slider"
		content-width="calc(35% - 48px)"
		tab-width="0"
		direction="right"
		:is-open="Boolean(previewItem)"
	>
		<template v-slot:content>
			<div class="slider-header content">
				<span>{{ resultType.toUpperCase() }}</span>
				<i class="pi pi-times" @click="emit('update:previewItem', null)" />
			</div>
			<div class="selected-resources-pane">
				<Document
					v-if="resultType === ResourceType.XDD"
					:asset-id="previewItemId"
					:project="resources.activeProject"
					:highlight="searchTerm"
				/>
				<Dataset
					v-if="resultType === ResourceType.DATASET"
					:asset-id="previewItemId"
					:project="resources.activeProject"
					:highlight="searchTerm"
				/>
				<Model
					v-if="resultType === ResourceType.MODEL"
					:asset-id="previewItemId"
					:project="resources.activeProject"
					:highlight="searchTerm"
				/>
				<footer>
					<Button
						v-if="!previewItemSelected"
						label="Add to Resources"
						@click="emit('toggle-data-item-selected', { item: previewItem })"
						class="toggle-selection"
					/>
					<Button
						v-else
						label="Remove from Resources"
						@click="emit('toggle-data-item-selected', { item: previewItem })"
						class="toggle-selection p-button-secondary"
					/>
				</footer>
			</div>
		</template>
	</slider>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { PropType, computed, ref, watch } from 'vue';
import useResourcesStore from '@/stores/resources';
import { ResultType, ResourceType } from '@/types/common';
import { isDocument } from '@/utils/data-util';
import Document from '@/components/documents/Document.vue';
import Dataset from '@/components/dataset/Dataset.vue';
import Model from '@/components/models/Model.vue';
import Slider from '@/components/widgets/Slider.vue';

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
	resultType: {
		type: String,
		default: null
	},
	searchTerm: {
		type: String,
		default: null
	}
});

// store and use copy of previewItem to disconnect it from prop for persistence
const previewItemState = ref(props.previewItem);

const emit = defineEmits(['update:previewItem', 'toggle-data-item-selected']);

watch(
	() => props.previewItem,
	(previewItem) => {
		if (previewItem) {
			previewItemState.value = previewItem;
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
.slider-header {
	display: flex;
	align-items: center;
	margin: 1rem;
}

.slider-header.content {
	font-size: 14px;
	color: var(--text-color-subdued);
	font-weight: bold;
	justify-content: space-between;
}

i {
	cursor: pointer;
}

.slider-header.tab {
	justify-content: center;
}

footer {
	border-top: 1px solid var(--surface-border);
	background-color: var(--surface-section);
	position: fixed;
	height: 5rem;
	bottom: 3rem;
	width: calc(35% - 48px);
	display: flex;
	align-items: center;
}

.toggle-selection {
	margin-left: 1rem;
}
</style>
