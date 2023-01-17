<script setup lang="ts">
import { watch, ref, computed } from 'vue';
import { XDDArticle, XDDExtractionType } from '@/types/XDD';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { isXDDArticle } from '@/utils/data-util';
import IconAdd24 from '@carbon/icons-vue/es/add/24';
import IconCheckmark24 from '@carbon/icons-vue/es/checkmark/24';
import IconOverflowMenuVertical24 from '@carbon/icons-vue/es/overflow-menu--vertical/24';
import IconPDF16 from '@carbon/icons-vue/es/PDF/16';
import IconLink16 from '@carbon/icons-vue/es/link/16';
import IconChartLine16 from '@carbon/icons-vue/es/chart--line/16';
import IconTable16 from '@carbon/icons-vue/es/table/16';
import IconArrowLeft16 from '@carbon/icons-vue/es/arrow--left/16';
import IconArrowRight16 from '@carbon/icons-vue/es/arrow--right/16';
import { ResultType, ResourceType } from '@/types/common';

const props = defineProps<{
	asset: XDDArticle & Model & Dataset;
	isPreviewed: boolean;
	isInCart: boolean;
	resourceType: ResourceType;
	selectedSearchItems: ResultType[];
}>();

const emit = defineEmits(['toggle-selected-asset', 'toggle-asset-preview']);

const relatedAssetPage = ref<number>(0);

// These asset types don't appear at the moment
const extractionsWithImages = computed(() =>
	props.asset.relatedExtractions
		? props.asset.relatedExtractions?.filter(
				(ex) =>
					ex.askemClass === XDDExtractionType.Figure ||
					ex.askemClass === XDDExtractionType.Table ||
					ex.askemClass === XDDExtractionType.Equation ||
					ex.askemClass === XDDExtractionType.Section || // remove this later just for testing pagination
					ex.askemClass === XDDExtractionType.Document // remove this later just for testing pagination
		  )
		: []
);

const relatedAsset = computed(() => extractionsWithImages[relatedAssetPage.value]);

watch(
	() => props.asset,
	() => {
		relatedAssetPage.value = 0;
	}
); // reset page number on new search

function paginationMovement(movement: number) {
	const newPage = relatedAssetPage.value + movement;
	if (newPage > -1 && newPage < extractionsWithImages.value.length) {
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

// Format features for dataset type
const formatFeatures = () => {
	const features = props.asset.annotations.annotations.feature ?? [];
	if (!features || features.length === 0) return [];
	const featuresNames = features.map((f) => (f.display_name !== '' ? f.display_name : f.name));
	const max = 5;
	return featuresNames.length < max ? featuresNames : featuresNames.slice(0, max);
};
</script>

<template>
	<div class="search-item" :active="isPreviewed" @click="emit('toggle-asset-preview')">
		<div>
			<div class="type-and-filters">
				{{ resourceType.toUpperCase() }}
				<div class="asset-filters" v-if="resourceType === ResourceType.XDD">
					<IconLink16 />
					<IconChartLine16 />
					<IconTable16 />
					<IconPDF16 />
				</div>
				<div v-if="resourceType === ResourceType.MODEL">Framework / {{ asset.framework }}</div>
				<div v-if="resourceType === ResourceType.DATASET && asset.simulationRun === true">
					Simulation run
				</div>
			</div>
			<div class="title">
				<template v-if="resourceType === ResourceType.XDD">{{ asset.title }}</template>
				<template
					v-else-if="resourceType === ResourceType.MODEL || resourceType === ResourceType.DATASET"
				>
					{{ asset.name }}
				</template>
			</div>
			<div class="details">
				<template v-if="resourceType === ResourceType.XDD">{{ formatDetails() }}</template>
				<template v-else-if="resourceType === ResourceType.DATASET">{{ asset.url }}</template>
			</div>
			<ul class="snippets" v-if="asset.highlight">
				<li v-for="h in asset.highlight" :key="h">...<span v-html="h"></span>...</li>
			</ul>
			<div class="description">{{ asset.description }}</div>
			<div class="parameters" v-if="resourceType === ResourceType.MODEL && asset.parameters">
				PARAMETERS:
				{{ asset.parameters }}
				<!--may need a formatting a function this attribute is always undefined at the moment-->
			</div>
			<div class="features" v-else-if="resourceType === ResourceType.DATASET">
				FEATURES:
				<span v-for="(feature, index) in formatFeatures()" :key="index"> {{ feature }}, </span>
			</div>
			<footer><!--pill tags if already in another project--></footer>
		</div>
		<div class="right">
			<figure v-if="resourceType === ResourceType.XDD && extractionsWithImages.length > 0">
				<img
					v-if="relatedAsset && relatedAsset.properties.image"
					:src="`data:image/jpeg;base64,${relatedAsset.properties.image}`"
					class="extracted-assets"
					alt="asset"
				/>
				<div class="asset-nav-arrows">
					<IconArrowLeft16 @click="paginationMovement(-1)" />
					Asset {{ relatedAssetPage + 1 }} of {{ extractionsWithImages.length }}
					<IconArrowRight16 @click="paginationMovement(1)" />
				</div>
			</figure>
			<button type="button" v-if="isInCart">
				<!--there are talks of having the plus and three dot menu available wherever-->
				<IconOverflowMenuVertical24 />
			</button>
			<button v-else type="button" @click.stop="emit('toggle-selected-asset')">
				<IconAdd24 v-show="!isSelected()" />
				<IconCheckmark24 class="checkmark-color" v-show="isSelected()" />
			</button>
		</div>
	</div>
</template>

<style scoped>
.search-item {
	background-color: white;
	color: var(--un-color-body-text-secondary);
	padding: 1rem;
	margin: 1px;
	display: flex;
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
	gap: 2rem;
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
	gap: 0.5rem;
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
.details,
.description,
.parameters,
.features {
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
