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
						label="Add to Resources"
						@click="emit('toggle-data-item-selected', { item: previewItem })"
						class="add-to-cart"
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
import { isXDDArticle } from '@/utils/data-util';
import Document from '@/components/articles/Document.vue';
import Dataset from '@/components/dataset/Dataset.vue';
import Model from '@/components/models/Model.vue';
import Slider from '@/components/Slider.vue';

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
const previewItemState = ref(props.previewItem);

const emit = defineEmits(['update:previewItem', 'toggle-data-item-selected']);

watch(
	() => props.previewItem,
	(previewItem) => {
		if (previewItem) {
			previewItemState.value = previewItem;
		} else {
			setTimeout(() => {
				previewItemState.value = null;
			}, 300);
		}
	}
);

const previewItemId = computed(() => {
	if (!previewItemState.value) return '';
	if (isXDDArticle(previewItemState.value)) {
		return previewItemState.value.gddId;
	}
	return previewItemState.value.id as string;
});
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
	bottom: 0;
	width: calc(35% - 48px);
	display: flex;
	align-items: center;
}

.add-to-cart {
	height: 1.5rem;
	margin-left: 1rem;
}
</style>
