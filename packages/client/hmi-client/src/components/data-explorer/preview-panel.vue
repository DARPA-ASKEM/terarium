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
				<i class="slider-header-item pi pi-times" @click="emit('update:previewItem', null)"></i>
			</div>
			<div class="selected-resources-pane">
				<Document
					v-if="resultType === ResourceType.XDD"
					:asset-id="previewItemId as string"
					:project="resources.activeProject"
				/>
				<Dataset
					v-if="resultType === ResourceType.DATASET"
					:asset-id="previewItemId as string"
					:project="resources.activeProject"
				/>
				<Button label="Add to Cart"></Button>
				<Button label="Add to Project"></Button>
			</div>
		</template>
	</slider>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import { PropType, computed, ref, watch } from 'vue';

import useResourcesStore from '@/stores/resources';

import { ResultType, ResourceType } from '@/types/common';
import { XDDArticle } from '@/types/XDD';

import { isXDDArticle } from '@/utils/data-util';

import Document from '@/components/articles/Document.vue';
import Dataset from '@/components/dataset/Dataset.vue';
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
	}
});
const previewItemState = ref(props.previewItem);

const emit = defineEmits(['update:previewItem']);

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
	const previewItem = previewItemState.value;
	if (previewItem === null) return '';
	if (isXDDArticle(previewItem)) {
		const itemAsArticle = previewItem as XDDArticle;
		// eslint-disable-next-line no-underscore-dangle
		return itemAsArticle.gddId;
	}
	return previewItem.id;
});
</script>

<style scoped>
i {
	font-size: 1.5rem;
	cursor: pointer;
}

.slider-header {
	display: flex;
	align-items: center;
}
.slider-header.content {
	flex-direction: row-reverse;
	justify-content: space-between;
}
.slider-header.tab {
	justify-content: center;
}
.slider-header-item {
	font-weight: bold;
	margin: 12px;
}
</style>
