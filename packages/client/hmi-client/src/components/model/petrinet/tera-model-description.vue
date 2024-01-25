<template>
	<main>
		<tera-columnar-panel>
			<Accordion multiple :active-index="[0, 1]">
				<AccordionTab header="Description">
					<section class="description">
						<tera-show-more-text :text="description" :lines="5" />
					</section>
				</AccordionTab>
				<AccordionTab header="Additional information">
					<section class="additional-information">
						<article v-if="!isEmpty(provenance)">
							<h5>Provenance</h5>
							<p v-html="provenance" />
						</article>
						<article v-if="!isEmpty(schema)">
							<h5>Schema</h5>
							<p v-html="schema" />
						</article>
						<article v-if="!isEmpty(sourceDataset)">
							<h5>Source dataset</h5>
							<p v-html="sourceDataset" />
						</article>
						<article v-if="!isEmpty(usage)">
							<h5>Usage</h5>
							<p v-html="usage" />
						</article>
					</section>
				</AccordionTab>
			</Accordion>
			<section class="details-column">
				<tera-grey-card class="details">
					<ul>
						<li class="multiple">
							<span>
								<label>Framework</label>
								<div class="framework">{{ model?.header?.schemaName }}</div>
							</span>
							<span>
								<label>Model version</label>
								<div>{{ model?.header?.model_version }}</div>
							</span>
							<span>
								<label>Date created</label>
								<div>{{ model?.metadata?.processed_at ?? card?.date }}</div>
							</span>
						</li>
						<li>
							<label>Created by</label>
							<div><tera-show-more-text v-if="authors" :text="authors" :lines="2" /></div>
						</li>
						<li>
							<label>Author email</label>
							<div>{{ card?.authorEmail }}</div>
						</li>
						<li>
							<label>Institution</label>
							<div>
								<tera-show-more-text v-if="card?.authorInst" :text="card?.authorInst" :lines="2" />
							</div>
						</li>
						<li class="multiple">
							<span>
								<label>License</label>
								<div>{{ card?.license }}</div>
							</span>
							<span>
								<label>Complexity</label>
								<div>{{ card?.complexity }}</div>
							</span>
						</li>
						<li>
							<label>Source</label>
							<div>{{ model?.metadata?.processed_by }}</div>
						</li>
					</ul>
				</tera-grey-card>
				<tera-related-documents
					:documents="documents"
					:asset-type="AssetType.Model"
					:assetId="model.id"
					@enriched="fetchAsset"
				/>
			</section>
		</tera-columnar-panel>
		<Accordion multiple :active-index="[0, 1, 2, 3]" v-bind:lazy="true">
			<!--Design in flux: diagram will probably be merged with equations (views would be switched with a toggle).
			However it may be worth showing the diagram and the equation at the same time on this page.
			-->
			<AccordionTab header="Diagram">
				<tera-model-diagram
					ref="teraModelDiagramRef"
					:model="model"
					:is-editable="!featureConfig.isPreview"
					:model-configuration="modelConfigurations[0]"
					@update-model="updateModelContent"
					@update-configuration="updateConfiguration"
				/>
			</AccordionTab>
			<AccordionTab header="Model equations">
				<tera-model-equation
					:model="model"
					:is-editable="!featureConfig.isPreview"
					@model-updated="emit('model-updated')"
				/>
			</AccordionTab>
			<AccordionTab header="Model observables">
				<tera-model-observable
					:model="model"
					:is-editable="!featureConfig.isPreview"
					@update-model="updateModelContent"
				/>
			</AccordionTab>
			<AccordionTab v-if="!isEmpty(relatedTerariumArtifacts)" header="Associated resources">
				<DataTable :value="relatedTerariumModels">
					<Column field="name" header="Models" />
				</DataTable>
				<DataTable :value="relatedTerariumDatasets">
					<Column field="name" header="Datasets" />
				</DataTable>
				<DataTable :value="relatedTerariumDocuments">
					<Column field="name" header="Documents" />
				</DataTable>
			</AccordionTab>
		</Accordion>
		<tera-model-semantic-tables
			:model="model"
			:model-configurations="modelConfigurations"
			@update-model="(modelClone) => emit('update-model', modelClone)"
		/>
	</main>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed, ref } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import { AcceptedExtensions, FeatureConfig, ResultType } from '@/types/common';
import type { DocumentAsset, Model, Dataset, ModelConfiguration } from '@/types/Types';
import { AssetType } from '@/types/Types';
import * as textUtil from '@/utils/text';
import TeraRelatedDocuments from '@/components/widgets/tera-related-documents.vue';
import { useProjects } from '@/composables/project';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import TeraModelObservable from '@/components/model/petrinet/tera-model-observable.vue';
import { isDataset, isDocument, isModel } from '@/utils/data-util';
import TeraGreyCard from '@/components/widgets/tera-grey-card.vue';
import TeraColumnarPanel from '@/components/widgets/tera-columnar-panel.vue';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import TeraModelSemanticTables from './tera-model-semantic-tables.vue';

const props = defineProps<{
	model: Model;
	modelConfigurations: ModelConfiguration[];
	highlight: string;
	featureConfig: FeatureConfig;
}>();

const emit = defineEmits(['update-model', 'fetch-model', 'update-configuration', 'model-updated']);

const teraModelDiagramRef = ref();

const card = computed(() => {
	if (props.model.metadata?.card) {
		const cardWithUnknowns = props.model.metadata?.card;
		const cardWithUnknownsArr = Object.entries(cardWithUnknowns);

		for (let i = 0; i < cardWithUnknownsArr.length; i++) {
			const key = cardWithUnknownsArr[i][0];
			if (cardWithUnknowns[key] === 'UNKNOWN') {
				cardWithUnknowns[key] = null;
			}
		}
		return cardWithUnknowns;
	}
	return null;
});
const description = computed(() =>
	highlightSearchTerms(props.model?.header?.description.concat(' ', card.value?.description ?? ''))
);
const usage = computed(() => card.value?.usage ?? '');
const sourceDataset = computed(() => card.value?.dataset ?? '');
const provenance = computed(() => card.value?.provenance ?? '');
const schema = computed(() => card.value?.schema ?? '');
const documents = computed(
	() =>
		useProjects()
			.getActiveProjectAssets(AssetType.Document)
			.filter((document: DocumentAsset) =>
				[AcceptedExtensions.PDF, AcceptedExtensions.TXT, AcceptedExtensions.MD].some(
					(extension) => {
						if (document.fileNames && !isEmpty(document.fileNames)) {
							return document.fileNames[0]?.endsWith(extension);
						}
						return false;
					}
				)
			)
			.map((document: DocumentAsset) => ({
				name: document.name,
				id: document.id
			})) ?? []
);

const authors = computed(() => {
	const authorsArray = props.model?.metadata?.annotations?.authors ?? [];

	if (card.value?.authorAuthor) authorsArray.unshift(card.value?.authorAuthor);

	return authorsArray.join(', ');
});

// Highlight strings based on props.highlight
function highlightSearchTerms(text: string | undefined): string {
	if (!!props.highlight && !!text) {
		return textUtil.highlight(text, props.highlight);
	}
	return text ?? '';
}

const relatedTerariumArtifacts = ref<ResultType[]>([]);
const relatedTerariumModels = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isModel(d)) as Model[]
);
const relatedTerariumDatasets = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDataset(d)) as Dataset[]
);
const relatedTerariumDocuments = computed(
	() => relatedTerariumArtifacts.value.filter((d) => isDocument(d)) as Document[]
);

function fetchAsset() {
	emit('fetch-model');
}

function updateModelContent(updatedModel: Model) {
	emit('update-model', updatedModel);
}

function updateConfiguration(updatedConfiguration: ModelConfiguration) {
	emit('update-configuration', updatedConfiguration);
}
</script>

<style scoped>
.overview {
	display: flex;
	width: 100%;
	gap: 2rem;

	& > * {
		flex: 1;
	}
}

.description,
.additional-information {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
	margin-left: 1.5rem;
}

.details-column {
	display: flex;
	flex-direction: column;
	gap: var(--gap-small);
	> .details {
		> ul {
			list-style: none;
			padding: 0.5rem 1rem;
			display: flex;
			flex-direction: column;
			gap: var(--gap-small);

			& > li.multiple {
				display: flex;

				& > span {
					flex: 1 0 0;
				}
			}

			& > li label {
				color: var(--text-color-subdued);
				font-size: var(--font-caption);

				& + *:empty:before {
					content: '--';
				}
			}
		}
	}
}

.framework {
	text-transform: capitalize;
}
</style>
