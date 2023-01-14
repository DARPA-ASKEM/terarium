<script setup lang="ts">
import { watch, ref, computed } from 'vue';
import { XDDArticle, XDDExtractionType } from '@/types/XDD';
import { isXDDArticle } from '@/utils/data-util';
import IconAdd24 from '@carbon/icons-vue/es/add/24';
import IconCheckmark24 from '@carbon/icons-vue/es/checkmark/24';
import IconOverflowMenuVertical24 from '@carbon/icons-vue/es/overflow-menu--vertical/24';
import IconDocumentBlank16 from '@carbon/icons-vue/es/document--blank/16';
import IconLink16 from '@carbon/icons-vue/es/link/16';
import IconChartLine16 from '@carbon/icons-vue/es/chart--line/16';
import IconTable16 from '@carbon/icons-vue/es/table/16';
import IconArrowLeft16 from '@carbon/icons-vue/es/arrow--left/16';
import IconArrowRight16 from '@carbon/icons-vue/es/arrow--right/16';
import { ResultType } from '@/types/common';

const props = defineProps<{
	asset: XDDArticle; // Will be abstracted later to make other assets compatible
	isPreviewedArticle: boolean;
	isInCart: boolean;
	selectedSearchItems: ResultType[];
}>();

const emit = defineEmits(['toggle-article-selected', 'toggle-article-preview']);

const relatedAssetPage = ref<number>(0); // reset on new search
const relatedAsset = computed(
	() => props.asset.relatedExtractions && props.asset.relatedExtractions[relatedAssetPage.value]
);

// console.log(props.asset.relatedExtractions && props.asset.relatedExtractions);
// console.log(props.asset)
watch(props.asset, () => {
	relatedAssetPage.value = 0;
});

function paginationMovement(movement: number) {
	const newPage = relatedAssetPage.value + movement;
	if (
		props.asset.relatedExtractions &&
		newPage > -1 &&
		newPage < props.asset.relatedExtractions.length
	) {
		relatedAssetPage.value = newPage;
	}
}

const isSelected = () =>
	props.selectedSearchItems.find((item) => {
		if (isXDDArticle(item)) {
			const itemAsArticle = item as XDDArticle;
			return itemAsArticle.title === props.asset.title;
		}
		return false;
	});

// Return formatted author, year, journal
const formatDetails = () =>
	`${props.asset.author.map((a) => a.name).join(', ')} (${props.asset.year}) ${
		props.asset.journal
	}`;
</script>

<template>
	<div class="search-item" :active="isPreviewedArticle" @click="emit('toggle-article-preview')">
		<div>
			<div class="type-and-filters">
				ARTICLE
				<span class="asset-filters">
					<IconDocumentBlank16 />
					<IconLink16 />
					<IconChartLine16 />
					<IconTable16 />
				</span>
			</div>
			<div class="title">{{ asset.title }}</div>
			<div class="details">{{ formatDetails() }}</div>
			<ul class="snippets" v-if="asset.highlight">
				<li v-for="h in asset.highlight" :key="h">...<span v-html="h"></span>...</li>
			</ul>
		</div>
		<div class="right">
			<figure v-if="asset.relatedExtractions">
				<img
					v-if="
						relatedAsset &&
						relatedAsset.properties.image &&
						(relatedAsset.askemClass === XDDExtractionType.Figure ||
							relatedAsset.askemClass === XDDExtractionType.Table ||
							relatedAsset.askemClass === XDDExtractionType.Equation)
					"
					:src="`data:image/jpeg;base64,${asset.relatedExtractions[relatedAssetPage].properties.image}`"
					class="extracted-assets"
					alt="asset"
				/>
				<div class="asset-nav-arrows">
					<IconArrowLeft16 @click="paginationMovement(-1)" />
					Asset {{ relatedAssetPage + 1 }} of {{ asset.relatedExtractions?.length }}
					<IconArrowRight16 @click="paginationMovement(1)" />
				</div>
			</figure>
			<button type="button" v-if="isInCart">
				<IconOverflowMenuVertical24 />
			</button>
			<button v-else type="button" @click.stop="emit('toggle-article-selected')">
				<IconAdd24 v-show="!isSelected()" />
				<IconCheckmark24 class="checkmark-color" v-show="isSelected()" />
			</button>
		</div>
	</div>
</template>

<style>
.search-item {
	background-color: white;
	color: var(--un-color-body-text-secondary);
	padding: 1rem;
	margin: 1px;
	display: flex;
	align-content: stretch;
	align-items: stretch;
	justify-content: space-between;
}

.search-item:hover {
	background-color: var(--un-color-feedback-success-lighter);
}

.search-item[active='true'] {
	outline: 1px solid var(--un-color-feedback-success);
}

.type-and-filters {
	display: flex;
	align-items: center;
}

.asset-nav-arrows {
	display: flex;
}

.right {
	display: flex;
}

.right figure {
	width: 10rem;
}

.asset-filters {
	display: flex;
	margin-left: 2rem;
}

.title {
	font-weight: 500;
	color: var(--un-color-body-text-primary);
	margin: 0.5rem 0 0.25rem 0;
}

.details {
	margin: 0.25rem 0 0.5rem 0;
}

.title,
.details {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 1;
	overflow: hidden;
}

button {
	border: none;
	background-color: transparent;
	height: min-content;
	padding: 0;
}

svg:hover {
	cursor: pointer;
	background-color: hsla(0, 0%, 0%, 0.1);
	border-radius: 3px;
}

.checkmark-color {
	color: var(--un-color-feedback-success);
}

.snippets {
	list-style: none;
}
</style>
