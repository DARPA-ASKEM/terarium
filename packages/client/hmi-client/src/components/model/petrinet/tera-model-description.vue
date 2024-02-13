<template>
	<main>
		<Accordion multiple :active-index="[0, 1, 2, 3, 4]" v-bind:lazy="true">
			<AccordionTab header="Description">
				<section class="description">
					<tera-show-more-text :text="description" :lines="5" />

					<label class="p-text-secondary">Model type</label>
					<p>{{ modelType }}</p>
					<label class="p-text-secondary">Funded by</label>
					<p>{{ fundedBy }}</p>
					<label class="p-text-secondary">Authors</label>
					<p>{{ authors }}</p>
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
						<p>{{ moreInformation }}</p>
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

// "gollmCard": {
//             "ModelName": {
//                 "model_summary": "The SIDARTHE model predicts the course of the COVID-19 epidemic and the impact of population-wide interventions in Italy."
//             },
//             "ModelDetails": {
//                 "model_description": "The model considers eight stages of infection: susceptible (S), infected (I), diagnosed (D), ailing (A), recognized (R), threatened (T), healed (H), and extinct (E), collectively termed SIDARTHE. It discriminates between infected individuals depending on whether they have been diagnosed and on the severity of their symptoms. The model can likely be represented in petrinet format due to its compartmental nature.",
//                 "FundedBy": "Italian grant PRIN 2017 'Monitoring and Control Underpinning the Energy-Aware Factory of the Future: Novel Methodologies and Industrial Validation' (ID 2017YKXYXJ).",
//                 "ModelType": "Mathematical"
//             },
//             "Uses": {
//                 "DirectUse": "The model can be used to understand the course of the epidemic, plan effective control strategies, and assess the impact of strategies like lockdown, social distancing, testing, and contact tracing.",
//                 "OutOfScopeUse": "Using the model for diseases with significantly different transmission dynamics or in populations with vastly different social structures may be inappropriate."
//             },
//             "BiasRisksLimitations": {
//                 "bias_risks_limitations": "The model may overestimate the number of ICU patients due to healthcare system saturation, which is neglected in the model. It may also underestimate asymptomatic or pauci-symptomatic cases if the average age of infected people decreases over time."
//             },
//             "Evaluation": {
//                 "TestingDataFactorsMetrics": "The model was validated by fitting parameters based on official data about the COVID-19 epidemic in Italy, including the number of currently infected individuals with different severity of illness and the number of recovered diagnosed patients."
//             },
//             "TechnicalSpecifications": {
//                 "model_specs": "The model is a system of eight ordinary differential equations with parameters that can be adjusted to reflect different intervention strategies and stages of the epidemic."
//             },
//             "Glossary": {
//                 "terms": [
//                     "SIDARTHE",
//                     "Susceptible",
//                     "Infected",
//                     "Diagnosed",
//                     "Ailing",
//                     "Recognized",
//                     "Threatened",
//                     "Healed",
//                     "Extinct"
//                 ]
//             },
//             "ModelCardAuthors": [
//                 "Giulia Giordano",
//                 "Franco Blanchini",
//                 "Raffaele Bruno",
//                 "Patrizio Colaneri",
//                 "Alessandro Di Filippo",
//                 "Angela Di Matteo",
//                 "Marta Colaneri"
//             ],
//             "HowToGetStartedWithTheModel": {
//                 "examples": "To get started with the SIDARTHE model, one would need to understand the compartmental nature of the model, the significance of each stage of infection, and how the parameters influence the model's predictions. The model's equations and parameters would then be adjusted based on the specific epidemic data and intervention strategies being analyzed."
//             },
//             "Citation": {
//                 "references": [
//                     "Giordano, G., Blanchini, F., Bruno, R., Colaneri, P., Di Filippo, A., Di Matteo, A., & Colaneri, M. (2020). Modelling the COVID-19 epidemic and implementation of population-wide interventions in Italy. Nature Medicine, 26(6), 855-860."
//                 ]
//             },
//             "MoreInformation": {
//                 "links": [
//                     "https://doi.org/10.1038/s41591-020-0883-7"
//                 ]
//             }
//         }
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
