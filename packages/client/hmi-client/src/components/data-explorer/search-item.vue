<script setup lang="ts">
import { watch, ref, computed } from 'vue';
import { XDDArticle, XDDExtractionType } from '@/types/XDD';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { isXDDArticle } from '@/utils/data-util';
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
const chosenExtractionFilter = ref<XDDExtractionType | null>(null);

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

function updateExtractionFilter(extractionType: XDDExtractionType | null) {
	chosenExtractionFilter.value =
		chosenExtractionFilter.value === extractionType ? null : extractionType;
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
				<div
					class="asset-filters"
					v-if="resourceType === ResourceType.XDD && extractionsWithImages.length > 0"
				>
					<template
						v-for="icon in [
							{ type: XDDExtractionType.URL, class: 'pi-link' },
							{ type: XDDExtractionType.Figure, class: 'pi-chart-bar' },
							{ type: XDDExtractionType.Table, class: 'pi-table' },
							{ type: XDDExtractionType.Document, class: 'pi-file-pdf' }
						]"
						:key="icon"
					>
						<i :class="`pi ${icon.class}`" @click="updateExtractionFilter(icon.type)"></i>
					</template>
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
				<!--may need a formatting function this attribute is always undefined at the moment-->
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
					<i class="pi pi-arrow-left" @click="paginationMovement(-1)"></i>
					Asset {{ relatedAssetPage + 1 }} of {{ extractionsWithImages.length }}
					<i class="pi pi-arrow-right" @click="paginationMovement(1)"></i>
				</div>
			</figure>
			<button type="button" v-if="isInCart">
				<!--there are talks of having the plus and three dot menu available wherever-->
				<i class="pi pi-ellipsis-v"></i>
			</button>
			<button v-else type="button" @click.stop="emit('toggle-selected-asset')">
				<i class="pi pi-plus" v-show="!isSelected()"></i>
				<i class="pi pi-check checkmark-color" v-show="isSelected()"></i>
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

button {
	border: none;
	background-color: transparent;
	height: min-content;
	padding: 0;
}

i {
	padding: 0.2rem;
}

i:hover {
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
