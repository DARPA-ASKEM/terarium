<template>
	<section class="doc-view-container">
		<div v-if="doc">
			<div class="journal">{{ doc.journal }}</div>
			<div v-if="docLink" class="title">
				<a :href="docLink" target="_blank" rel="noreferrer noopener">{{ doc.title }}</a>
			</div>
			<div v-else class="title">{{ doc.title }}</div>
			<div class="authors">{{ formatArticleAuthors(doc) }}</div>
			<br />

			<div class="row">
				<!-- TODO -->
				<!-- Journal impact factor -->
				<!-- # Citations -->
				<div>
					<div class="publisher">{{ doc.publisher }}</div>
					<div class="doi">DOI: {{ doi }}</div>
				</div>
				<Button label="Open PDF"></Button>
			</div>

			<Accordion :multiple="true" class="accordian">
				<AccordionTab header="Abstract">
					{{ formatAbstract(doc) }}
				</AccordionTab>

				<AccordionTab header="Snippets"> </AccordionTab>

				<AccordionTab header="Figures">
					<div v-for="ex in figureArtifacts" :key="ex.askemId" class="extracted-item">
						<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
						{{ ex.properties.caption ? ex.properties.caption : ex.properties.contentText }}
					</div>
				</AccordionTab>

				<AccordionTab header="Tables">
					<div v-for="ex in tableArtifacts" :key="ex.askemId" class="extracted-item">
						<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
						{{ ex.properties.caption ? ex.properties.caption : ex.properties.contentText }}
					</div>
				</AccordionTab>

				<AccordionTab header="Equations">
					<div v-for="ex in equationArtifacts" :key="ex.askemId" class="extracted-item">
						<img id="img" :src="'data:image/jpeg;base64,' + ex.properties.image" :alt="''" />
						{{ ex.properties.caption ? ex.properties.caption : ex.properties.contentText }}
					</div>
				</AccordionTab>

				<AccordionTab header="URLs">
					<div v-for="ex in urlArtifacts" :key="ex.url">
						<b>{{ ex.resource_title }}</b>
						<div>
							<a :href="ex.url" target="_blank" rel="noreferrer noopener">{{ ex.url }}</a>
						</div>
					</div>
				</AccordionTab>

				<AccordionTab header="Others">
					<div v-for="ex in otherArtifacts" :key="ex.askemId" class="extracted-item">
						<b>{{ ex.properties.title }}</b>
						{{ ex.properties.caption }}
						{{ ex.properties.abstractText }}
						{{ ex.properties.contentText }}
					</div>
				</AccordionTab>

				<AccordionTab header="Other versions"> </AccordionTab>
				<AccordionTab header="Related TERARium artifacts"> </AccordionTab>
				<AccordionTab header="Provenance"> </AccordionTab>
			</Accordion>
		</div>
		<resources-list
			v-else
			:project="props?.project"
			:resourceRoute="RouteName.DocumentRoute"
			@show-data-explorer="emit('show-data-explorer')"
		/>
		<slot name="footer"> </slot>
	</section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import { getDocumentById, getXDDArtifacts } from '@/services/data';
import { XDDArticle, XDDArtifact, XDDExtractionType } from '@/types/XDD';
import { getDocumentDoi } from '@/utils/data-util';
import { RouteName } from '@/router/routes';
import { Project } from '@/types/Project';
import ResourcesList from '@/components/resources/resources-list.vue';

const props = defineProps<{
	assetId: string;
	project: Project | null;
}>();

const emit = defineEmits(['show-data-explorer']);

const doc = ref<XDDArticle | null>(null);

watch(
	props,
	async () => {
		const id = props.assetId;
		if (id !== '') {
			// fetch doc from XDD
			const d = await getDocumentById(id);
			if (d) {
				doc.value = d;
			}
		} else {
			doc.value = null;
		}
	},
	{
		immediate: true
	}
);

const formatArticleAuthors = (d: XDDArticle) => d.author.map((a) => a.name).join(', ');

const docLink = computed(() =>
	doc.value?.link && doc.value.link.length > 0 ? doc.value.link[0].url : null
);

const formatAbstract = (d: XDDArticle) =>
	(d.abstractText && typeof d.abstractText === 'string' ? d.abstractText : false) ||
	'[no abstract]';

const doi = computed(() => getDocumentDoi(doc.value));

const artifacts = ref<XDDArtifact[]>([]);
const figureArtifacts = computed(() =>
	artifacts.value.filter((d) => d.askemClass === XDDExtractionType.Figure)
);
const tableArtifacts = computed(() =>
	artifacts.value.filter((d) => d.askemClass === XDDExtractionType.Table)
);
const equationArtifacts = computed(() =>
	artifacts.value.filter((d) => d.askemClass === XDDExtractionType.Equation)
);
const urlArtifacts = computed(() =>
	doc.value?.knownEntities && doc.value.knownEntities.url_extractions.length > 0
		? doc.value.knownEntities.url_extractions
		: null
);

const otherArtifacts = computed(() => {
	const exclusion = [
		XDDExtractionType.URL,
		XDDExtractionType.Table,
		XDDExtractionType.Figure,
		XDDExtractionType.Equation
	];

	return artifacts.value.filter((d) => !exclusion.includes(d.askemClass as XDDExtractionType));
});

const fetchArtifacts = async () => {
	if (doi.value !== '') {
		const allArtifacts = await getXDDArtifacts(doi.value);
		// filter out Document extraction type
		artifacts.value = allArtifacts.filter((art) => art.askemClass !== XDDExtractionType.Document);
	} else {
		// note that some XDD documents do not have a valid doi
		artifacts.value = [];
	}
};

watch(doi, (currentValue, oldValue) => {
	if (currentValue !== oldValue) {
		fetchArtifacts();
	}
});

// fetch artifacts from COSMOS using the doc doi
onMounted(async () => {
	fetchArtifacts();
});
</script>

<style scoped>
.doc-view-container {
	padding: 2rem;
	font-size: large;
	height: calc(100vh - 50px);
	width: 100%;
	overflow: auto;
	background: var(--un-color-body-surface-primary);
	margin: 1rem;
}

.row {
	display: flex;
	justify-content: space-between;
}

.title {
	font: var(--un-font-h3);
}

.authors {
	font-style: italic;
	padding-top: 8px;
}

.journal,
.publisher,
.doi {
	padding-top: 8px;
}

.accordian {
	margin-top: 1rem;
	margin-bottom: 1rem;
}

.extracted-item {
	padding-bottom: 20px;
}
</style>
