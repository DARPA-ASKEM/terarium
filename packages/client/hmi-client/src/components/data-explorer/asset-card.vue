<template>
	<div class="asset-card">
		<div>
			<div class="type-and-filters">
				{{ resourceType.toUpperCase() }}
				<div
					class="asset-filters"
					v-if="resourceType === ResourceType.XDD && asset.relatedExtractions"
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
						<i
							:class="`pi ${icon.class}`"
							:active="chosenExtractionFilter === icon.type"
							@click.stop="updateExtractionFilter(icon.type)"
						></i>
					</template>
				</div>
				<div v-else-if="resourceType === ResourceType.MODEL">Framework / {{ asset.framework }}</div>
				<div v-else-if="resourceType === ResourceType.DATASET && asset.simulationRun === true">
					Simulation run
				</div>
			</div>
			<div class="title" v-html="highlightText(title)" />
			<div class="details" v-html="formatDetails" />
			<ul class="snippets" v-if="snippets">
				<li v-for="(snippet, index) in snippets" :key="index">
					&hellip;<template v-html="highlightText(snippet)" />&hellip;
				</li>
			</ul>
			<div class="description" v-html="highlightText(asset.description)" />
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
		<aside class="right">
			<figure v-if="resourceType === ResourceType.XDD && asset.relatedExtractions">
				<img
					v-if="relatedAsset && relatedAsset.properties.image"
					:src="`data:image/jpeg;base64,${relatedAsset.properties.image}`"
					class="extracted-assets"
					alt="asset"
				/>
				<div class="asset-nav-arrows">
					<i class="pi pi-arrow-left" @click.stop="previewMovement(-1)"></i>
					<template v-if="extractionsWithImages.length > 0">
						Asset {{ relatedAssetPage + 1 }} of {{ extractionsWithImages.length }}
					</template>
					<template v-else> No {{ chosenExtractionFilter }}s </template>
					<i class="pi pi-arrow-right" @click.stop="previewMovement(1)"></i>
				</div>
			</figure>
			<slot name="default"></slot>
		</aside>
	</div>
</template>

<script setup lang="ts">
import { watch, ref, computed } from 'vue';
import { XDDArticle, XDDExtractionType } from '@/types/XDD';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { ResourceType } from '@/types/common';

const props = defineProps<{
	asset: XDDArticle & Model & Dataset;
	resourceType: ResourceType;
	highlight?: String;
}>();

const relatedAssetPage = ref<number>(0);
const chosenExtractionFilter = ref<XDDExtractionType | null>(null);

// These asset types don't appear at the moment
const extractionsWithImages = computed(() =>
	props.asset.relatedExtractions
		? props.asset.relatedExtractions?.filter((ex) => {
				if (chosenExtractionFilter.value === null) {
					return (
						ex.askemClass === XDDExtractionType.Figure ||
						ex.askemClass === XDDExtractionType.Table ||
						ex.askemClass === XDDExtractionType.Equation ||
						ex.askemClass === XDDExtractionType.URL ||
						ex.askemClass === XDDExtractionType.Section || // remove this later just for testing preview pagination
						ex.askemClass === XDDExtractionType.Document // remove this later just for testing preview pagination
					);
				}
				return ex.askemClass === chosenExtractionFilter.value;
		  })
		: []
);
const relatedAsset = computed(() => extractionsWithImages[relatedAssetPage.value]);
const snippets = computed(() =>
	props.asset.highlight ? Array.from(props.asset.highlight).splice(0, 3) : null
);
const title = computed(() =>
	props.resourceType === ResourceType.XDD ? props.asset.title : props.asset.name
);

watch(
	() => props.asset,
	() => {
		relatedAssetPage.value = 0;
	}
); // reset page number on new search

// Highlight strings based on props.highligh
function highlightText(text: string) {
	if (props.highlight) {
		const term = RegExp(props.highlight as string, 'gi');
		return text.replace(term, (match) => `<em class="highlight">${match}</em>`);
	}
	return text;
}

function previewMovement(movement: number) {
	const newPage = relatedAssetPage.value + movement;
	if (newPage > -1 && newPage < extractionsWithImages.value.length) {
		relatedAssetPage.value = newPage;
	}
}

function updateExtractionFilter(extractionType: XDDExtractionType | null) {
	chosenExtractionFilter.value =
		chosenExtractionFilter.value === extractionType ? null : extractionType;
}

// Return formatted author, year, journal
const formatDetails = computed(() => {
	if (props.resourceType === ResourceType.XDD) {
		const details = `${props.asset.author.map((a) => a.name).join(', ')} (${props.asset.year}) ${
			props.asset.journal
		}`;
		return highlightText(details);
	}

	if (props.resourceType === ResourceType.DATASET) {
		return props.asset?.url;
	}

	return null;
});

// Format features for dataset type
const formatFeatures = () => {
	const features = props.asset.annotations.annotations.feature ?? [];
	if (!features || features.length === 0) return [];
	const featuresNames = features.map((f) => (f.display_name !== '' ? f.display_name : f.name));
	const max = 5;
	return featuresNames.length < max ? featuresNames : featuresNames.slice(0, max);
};
</script>

<style scoped>
.asset-card {
	background-color: white;
	color: var(--text-color-secondary);
	padding: 1rem;
	margin: 1px;
	display: flex;
	justify-content: space-between;
}

.asset-card:hover {
	background-color: var(--surface-hover);
}

.asset-card[active='true'] {
	outline: 1px solid var(--primary-color-lighter);
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
.features,
.snippets li {
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
	color: var(--text-color-primary);
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
	border-radius: 3px;
	z-index: 2;
}

.pi[active='true'] {
	background-color: var(--primary-color-light);
}

i:hover {
	cursor: pointer;
	background-color: hsla(0, 0%, 0%, 0.1);
}

.snippets {
	list-style: none;
}

.asset-card >>> em.highlight {
	background-color: var(--surface-highlight);
	border-radius: var(--border-radius);
	padding-left: 0.2em;
	padding-right: 0.2em;
}
</style>
