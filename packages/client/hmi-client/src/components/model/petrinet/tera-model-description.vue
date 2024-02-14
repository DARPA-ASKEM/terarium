<template>
	<main>
		<Accordion multiple :active-index="[0, 1, 2, 3, 4]" v-bind:lazy="true">
			<AccordionTab header="Description">
				<section class="description">
					<tera-show-more-text :text="description" :lines="5" />

					<template v-if="modelType">
						<label class="p-text-secondary">Model type</label>
						<p>{{ modelType }}</p>
					</template>
					<template v-if="fundedBy">
						<label class="p-text-secondary">Funded by</label>
						<p>{{ fundedBy }}</p>
					</template>
					<template v-if="authors">
						<label class="p-text-secondary">Authors</label>
						<p>{{ authors }}</p>
					</template>
					<template v-if="uses">
						<h5>Uses</h5>
						<label class="p-text-secondary">Direct use</label>
						<p>{{ uses.DirectUse }}</p>
						<label class="p-text-secondary">Out of scope use</label>
						<p>{{ uses.OutOfScopeUse }}</p>
					</template>

					<template v-if="biasAndRiskLimitations">
						<h5>Bias and Risk Limitations</h5>
						<p>{{ biasAndRiskLimitations }}</p>
					</template>

					<template v-if="evaluation">
						<h5>Evaluation</h5>
						<p>{{ evaluation }}</p>
					</template>

					<template v-if="technicalSpecifications">
						<h5>Technical Specifications</h5>
						<p>{{ technicalSpecifications }}</p>
					</template>

					<template v-if="!isEmpty(glossary)">
						<h5>Glossary</h5>
						<p>{{ glossary.join(', ') }}</p>
					</template>

					<template v-if="!isEmpty(moreInformation)">
						<h5>More Information</h5>
						<a
							v-for="(link, index) in moreInformation"
							:href="link"
							target="_blank"
							rel="noopener noreferrer"
							:key="index"
							>{{ link }}</a
						>
					</template>

					<template v-if="!isEmpty(provenance)">
						<h5>Provenance</h5>
						<p v-html="provenance" />
					</template>
					<template v-if="!isEmpty(schema)">
						<h5>Schema</h5>
						<p v-html="schema" />
					</template>
					<template v-if="!isEmpty(sourceDataset)">
						<h5>Source dataset</h5>
						<p v-html="sourceDataset" />
					</template>
					<template v-if="!isEmpty(usage)">
						<h5>Usage</h5>
						<p v-html="usage" />
					</template>
				</section>
			</AccordionTab>
			<AccordionTab header="Diagram">
				<tera-model-diagram
					ref="teraModelDiagramRef"
					:model="model"
					:is-editable="!featureConfig?.isPreview"
					:model-configuration="modelConfigurations?.[0]"
					@update-model="updateModelContent"
					@update-configuration="updateConfiguration"
				/>
			</AccordionTab>
			<AccordionTab header="Provenance">
				<tera-related-documents
					:documents="documents"
					:asset-type="AssetType.Model"
					:assetId="model.id"
					@enriched="fetchAsset"
				/>
			</AccordionTab>
			<AccordionTab header="Model equations">
				<tera-model-equation
					:model="model"
					:is-editable="!featureConfig?.isPreview"
					@model-updated="emit('model-updated')"
				/>
			</AccordionTab>
			<AccordionTab header="Model observables">
				<tera-model-observable
					:model="model"
					:is-editable="!featureConfig?.isPreview"
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
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import TeraModelSemanticTables from './tera-model-semantic-tables.vue';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	highlight?: string;
	featureConfig?: FeatureConfig;
}>();

const emit = defineEmits(['update-model', 'fetch-model', 'update-configuration', 'model-updated']);

const teraModelDiagramRef = ref();

const card = computed(() => {
	// prioritize gollm_card over skema card
	if (props.model.metadata?.gollmCard) {
		return props.model.metadata.gollmCard;
	}

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
	highlightSearchTerms(
		card.value?.ModelDetails?.model_description ??
			card.value?.description ??
			props.model?.header?.description ??
			''
	)
);

const biasAndRiskLimitations = computed(
	() => card.value?.BiasRisksLimitations?.bias_risks_limitations ?? ''
);
const modelType = computed(() => card.value?.ModelDetails?.ModelType ?? '');
const fundedBy = computed(() => card.value?.ModelDetails?.FundedBy ?? '');
const evaluation = computed(() => card.value?.Evaluation?.TestingDataFactorsMetrics ?? '');
const technicalSpecifications = computed(
	() => card.value?.TechnicalSpecifications?.model_specs ?? ''
);
const glossary = computed(() => card.value?.Glossary?.terms ?? []);
const moreInformation = computed(() => card.value?.MoreInformation?.links ?? []);

const uses = computed(() => card.value?.Uses ?? null);
const usage = computed(() => card.value?.usage ?? '');
const sourceDataset = computed(() => card.value?.dataset ?? '');
const provenance = computed(() => card.value?.provenance ?? '');
const schema = computed(() => card.value?.schema ?? '');
const authors = computed(() => {
	const authorsArray = props.model?.metadata?.annotations?.authors ?? [];

	if (card.value?.ModelCardAuthors) authorsArray.unshift(card.value?.ModelCardAuthors);
	else if (card.value?.authorAuthor) authorsArray.unshift(card.value?.authorAuthor);

	return authorsArray.join(', ');
});

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
}

.framework {
	text-transform: capitalize;
}
</style>
