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
				<IconClose24 class="slider-header-item" @click="emit('update:previewItem', null)" />
			</div>
			<div class="selected-resources-pane">
				<Document :asset-id="previewItemId" :project="resources.activeProject" />
				<Button label="Add to Cart"></Button>
				<Button label="Add to Project"></Button>
			</div>
		</template>
	</slider>
</template>

<script setup lang="ts">
import Button from 'primevue/button';
import IconClose24 from '@carbon/icons-vue/es/close/24';
import { PropType, computed, ref, watch } from 'vue';

import useResourcesStore from '@/stores/resources';

import { ResultType } from '@/types/common';
import { XDDArticle } from '@/types/XDD';

import { isXDDArticle } from '@/utils/data-util';

import Document from '@/components/articles/Document.vue';
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
		return itemAsArticle.gddid || itemAsArticle._gddid;
	}
	return '';
});
</script>

<style scoped>
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
