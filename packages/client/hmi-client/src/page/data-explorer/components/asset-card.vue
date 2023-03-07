<template>
	<div class="asset-card" draggable="true" @dragstart="startDrag(asset, resourceType)">
		<main>
			<div class="type-and-filters">
				{{ resourceType.toUpperCase() }}
				<div
					class="asset-filters"
					v-if="resourceType === AssetType.DOCUMENT && asset.relatedExtractions"
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
						/>
					</template>
				</div>
				<div v-else-if="resourceType === AssetType.MODEL">{{ asset.framework }}</div>
				<div v-else-if="resourceType === AssetType.DATASET && asset.simulationRun === true">
					Simulation run
				</div>
			</div>
			<header class="title" v-html="title" />
			<div class="details" v-html="formatDetails" />
			<ul class="snippets" v-if="snippets">
				<li v-for="(snippet, index) in snippets" :key="index" v-html="snippet" />
			</ul>
			<div class="description" v-html="highlightSearchTerms(asset.description)" />
			<div class="parameters" v-if="resourceType === AssetType.MODEL && asset.parameters">
				PARAMETERS:
				{{ asset.parameters }}
				<!--may need a formatting function this attribute is always undefined at the moment-->
			</div>
			<div class="features" v-else-if="resourceType === AssetType.DATASET">
				FEATURES:
				<span v-for="(feature, index) in formatFeatures()" :key="index"> {{ feature }}, </span>
			</div>
			<footer><!--pill tags if already in another project--></footer>
		</main>
		<aside class="preview-and-options">
			<figure v-if="resourceType === AssetType.DOCUMENT && asset.relatedExtractions">
				<template v-if="relatedAsset">
					<img
						v-if="relatedAsset.properties.image"
						:src="`data:image/jpeg;base64,${relatedAsset.properties.image}`"
						class="extracted-assets"
						alt="asset"
					/>
					<div class="link" v-else-if="relatedAsset.properties.DOI">
						<a
							v-if="relatedAsset.properties.documentBibjson.link"
							:href="relatedAsset.properties.documentBibjson.link[0].url"
							@click.stop
							target="_blank"
							rel="noreferrer noopener"
						>
							{{ relatedAsset.properties.documentBibjson.link[0].url }}
						</a>
						<a
							v-else
							:href="`https://doi.org/${relatedAsset.properties.DOI}`"
							@click.stop
							target="_blank"
							rel="noreferrer noopener"
						>
							{{ `https://doi.org/${relatedAsset.properties.DOI}` }}
						</a>
					</div>
					<div class="link" v-else-if="relatedAsset.urlExtraction">
						<a
							:href="relatedAsset.urlExtraction.url"
							@click.stop
							target="_blank"
							rel="noreferrer noopener"
						>
							{{ relatedAsset.urlExtraction.resourceTitle }}
						</a>
					</div>
				</template>
				<div class="asset-nav-arrows">
					<span class="asset-pages" v-if="!isEmpty(extractions)">
						<i class="pi pi-arrow-left" @click.stop="previewMovement(-1)"></i>
						<span class="asset-count">
							{{ chosenExtractionFilter }}
							<span class="asset-count-text">{{ relatedAssetPage + 1 }}</span> of
							<span class="asset-count-text">{{ extractions.length }}</span>
						</span>
						<i class="pi pi-arrow-right" @click.stop="previewMovement(1)"></i>
					</span>
					<template v-else> No {{ chosenExtractionFilter }}s </template>
				</div>
			</figure>
			<slot name="default"></slot>
		</aside>
	</div>
</template>

<script setup lang="ts">
import { watch, ref, computed, ComputedRef } from 'vue';
import { isEmpty } from 'lodash';
import { XDDExtractionType } from '@/types/XDD';
import { XDDArtifact, XDDUrlExtraction, DocumentType } from '@/types/Document';
import { Model } from '@/types/Model';
import { Dataset } from '@/types/Dataset';
import { AssetType } from '@/types/common';
import * as textUtil from '@/utils/text';
import { useDragEvent } from '@/services/drag-drop';

// This type is for easy frontend integration with the rest of the extraction types (just for use here)
type UrlExtraction = {
	askemClass: XDDExtractionType;
	urlExtraction: XDDUrlExtraction;
};

const props = defineProps<{
	asset: DocumentType & Model & Dataset;
	resourceType: AssetType;
	highlight?: string;
}>();

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

// const emit = defineEmits(['toggle-asset-preview']);

const relatedAssetPage = ref<number>(0);
const chosenExtractionFilter = ref<XDDExtractionType | 'Asset'>('Asset');

const urlExtractions = computed(() => {
	const urls: UrlExtraction[] = [];

	if (props.asset.relatedExtractions) {
		const documentsWithUrls = props.asset.relatedExtractions.filter(
			(ex) =>
				ex.askemClass === XDDExtractionType.Document &&
				ex.properties.documentBibjson.knownEntities !== undefined &&
				!isEmpty(ex.properties.documentBibjson.knownEntities.urlExtractions)
		);

		for (let i = 0; i < documentsWithUrls.length; i++) {
			const knownEntities = documentsWithUrls[i].properties.documentBibjson.knownEntities;

			if (knownEntities) {
				for (let j = 0; i < knownEntities.urlExtractions.length; j++) {
					urls.push({
						askemClass: XDDExtractionType.URL,
						urlExtraction: knownEntities.urlExtractions[j]
					});
				}
			}
		}
	}
	return urls;
});

const extractions: ComputedRef<UrlExtraction[] & XDDArtifact[]> = computed(() => {
	if (props.asset.relatedExtractions) {
		const allExtractions = [
			...(props.asset.relatedExtractions as UrlExtraction[] & XDDArtifact[]),
			...(urlExtractions.value as UrlExtraction[] & XDDArtifact[])
		];

		if (chosenExtractionFilter.value === 'Asset') return allExtractions;

		return allExtractions.filter((ex) => ex.askemClass === chosenExtractionFilter.value);
	}
	return [];
});

const relatedAsset = computed(() => extractions.value[relatedAssetPage.value]);
const snippets = computed(() =>
	props.asset.highlight ? Array.from(props.asset.highlight).splice(0, 3) : null
);
const title = computed(() => {
	const value = props.resourceType === AssetType.DOCUMENT ? props.asset.title : props.asset.name;
	return highlightSearchTerms(value);
});

// Reset page number on new search and when chosenExtractionFilter is changed
watch(
	() => [props.asset, chosenExtractionFilter.value],
	() => {
		relatedAssetPage.value = 0;
	}
);

function previewMovement(movement: number) {
	const newPage = relatedAssetPage.value + movement;
	if (newPage > -1 && newPage < extractions.value.length) {
		relatedAssetPage.value = newPage;
	}
	// console.log(relatedAsset.value);
}

function updateExtractionFilter(extractionType: XDDExtractionType) {
	chosenExtractionFilter.value =
		chosenExtractionFilter.value === extractionType ? 'Asset' : extractionType;
}

//
// in case we decided to display matching concepts associated with artifacts
//  when performing a search using a keyword that represents a known concept
//
// const getConceptTags = (item: ResultType) => {
// 	const tags = [] as string[];
// 	if (props.rawConceptFacets) {
// 		const itemConcepts = props.rawConceptFacets.results.filter(
// 			(conceptResult) => conceptResult.id === item.id
// 		);
// 		tags.push(...itemConcepts.map((c) => c.name ?? c.curie));
// 	}
// 	return tags;
// };

// Return formatted author, year, journal
// Return formatted author, year, journal
const formatDetails = computed(() => {
	if (props.resourceType === AssetType.DOCUMENT) {
		const details = `${props.asset.author.map((a) => a.name).join(', ')} (${props.asset.year}) ${
			props.asset.journal
		}`;
		return highlightSearchTerms(details);
	}

	if (props.resourceType === AssetType.DATASET) {
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

const { setDragData } = useDragEvent();

function startDrag(asset, resourceType) {
	setDragData('asset', asset);
	setDragData('resourceType', resourceType);
}
</script>

<style scoped>
.asset-card {
	background-color: var(--surface-a);
	border: 1px solid transparent;
	color: var(--text-color-subdued);
	display: flex;
	font-size: var(--font-caption);
	justify-content: space-between;
	margin: 1px;
	min-height: 5rem;
	padding: 1rem;
}

.asset-card:hover {
	border-color: var(--surface-border);
	cursor: pointer;
}

.asset-card[active='true'] {
	outline: 1px solid var(--primary-color-dark);
}

.type-and-filters {
	display: flex;
	align-items: center;
	gap: 2rem;
}

.preview-and-options {
	display: flex;
	gap: 0.5rem;
}

.preview-and-options figure {
	display: flex;
	flex-direction: column;
	justify-content: flex-end;
	width: 8rem;
	height: 7rem;
}

.preview-and-options figure img {
	margin: auto 0;
	object-fit: contain;
	max-height: 5rem;
}

.preview-and-options .link {
	overflow: auto;
	overflow-wrap: break-word;
	margin: auto 0;
	min-height: 0;
	font-size: 10px;
}

.preview-and-options .link a {
	color: var(--primary-color);
}

.preview-and-options figure img,
.preview-and-options .link {
	border: 1px solid var(--surface-ground);
	border-radius: 3px;
	padding: 4px;
}

.asset-nav-arrows {
	text-align: center;
}

.asset-nav-arrows .asset-pages {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.asset-nav-arrows .asset-count {
	white-space: nowrap;
}

.asset-nav-arrows .asset-count-text {
	color: var(--text-color-primary);
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

.asset-filters {
	display: flex;
	gap: 0.5rem;
}

.title {
	color: var(--text-color-primary);
	font-size: var(--font-body-medium);
	margin: 0.5rem 0 0.25rem 0;
}

.details {
	margin: 0.25rem 0 0.5rem 0;
	font-size: var(--font-size);
}

i {
	padding: 0.2rem;
	border-radius: 3px;
	font-size: var(--font-caption);
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

/* Add ellipsis around a snippet */
.snippets li::before,
.snippets li::after {
	content: '…';
}
</style>
