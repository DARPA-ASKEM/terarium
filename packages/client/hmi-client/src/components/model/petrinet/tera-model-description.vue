<template>
	<section>
		<Accordion multiple :active-index="[0, 1, 2, 3, 4]" v-bind:lazy="true" class="mb-0">
			<AccordionTab header="Description">
				<tera-progress-spinner v-if="isGeneratingCard" is-centered>
					Generating description...
				</tera-progress-spinner>
				<Editor
					v-else
					v-model="editorContent"
					:class="{ readonly: !hasEditPermission }"
					:readonly="!hasEditPermission"
				/>
			</AccordionTab>
			<AccordionTab header="Model Card">
				<tera-show-more-text :text="description" :lines="5" />
				<p v-if="modelType"><label>Model type</label>{{ modelType }}</p>
				<p v-if="fundedBy"><label>Funded by</label>{{ fundedBy }}</p>
				<p v-if="authors"><label>Authors</label>{{ authors }}</p>
				<p v-if="uses?.DirectUse"><label>Direct use</label>{{ uses.DirectUse }}</p>
				<p v-if="uses?.OutOfScopeUse"><label>Out of scope use</label>{{ uses.OutOfScopeUse }}</p>
				<p v-if="biasAndRiskLimitations">
					<label>Bias and Risk Limitations</label>{{ biasAndRiskLimitations }}
				</p>
				<p v-if="evaluation"><label>Evaluation</label>{{ evaluation }}</p>
				<p v-if="technicalSpecifications">
					<label>Technical Specifications</label>{{ technicalSpecifications }}
				</p>
				<p v-if="!isEmpty(glossary)"><label>Glossary</label>{{ glossary.join(', ') }}</p>
				<p v-if="!isEmpty(moreInformation)">
					<label>More Information</label>
					<a
						v-for="(link, index) in moreInformation"
						:key="index"
						:href="link"
						rel="noopener noreferrer"
					>
						{{ link }}
					</a>
				</p>
			</AccordionTab>
			<AccordionTab header="Diagram">
				<tera-model-diagram
					ref="teraModelDiagramRef"
					:model="model"
					:is-editable="!featureConfig?.isPreview"
					:model-configuration="modelConfigurations?.[0]"
					@update-configuration="updateConfiguration"
				/>
			</AccordionTab>
			<AccordionTab header="Model equations">
				<tera-model-equation
					:model="model"
					:is-editable="false"
					@model-updated="emit('model-updated')"
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
			@update-model="(updatedModel: Model) => emit('update-model', updatedModel)"
			class="mt-0"
			:readonly="featureConfig?.isPreview"
		/>
	</section>
</template>

<script setup lang="ts">
import { isEmpty } from 'lodash';
import { computed, ref } from 'vue';
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Column from 'primevue/column';
import DataTable from 'primevue/datatable';
import { FeatureConfig, ResultType } from '@/types/common';
import type { Author, Dataset, Model, ModelConfiguration } from '@/types/Types';
import TeraShowMoreText from '@/components/widgets/tera-show-more-text.vue';
import TeraModelDiagram from '@/components/model/petrinet/model-diagrams/tera-model-diagram.vue';
import TeraModelEquation from '@/components/model/petrinet/tera-model-equation.vue';
import { isDataset, isDocument, isModel } from '@/utils/data-util';
import TeraProgressSpinner from '@/components/widgets/tera-progress-spinner.vue';
import TeraModelSemanticTables from '@/components/model/tera-model-semantic-tables.vue';
import Editor from 'primevue/editor';
import { useProjects } from '@/composables/project';

const props = defineProps<{
	model: Model;
	modelConfigurations?: ModelConfiguration[];
	featureConfig?: FeatureConfig;
	isGeneratingCard?: boolean;
}>();

const emit = defineEmits(['update-model', 'update-configuration', 'model-updated']);
const teraModelDiagramRef = ref();

const card = computed<any>(() => props.model.metadata?.gollmCard ?? null);
const description = computed(
	() => card.value?.ModelDetails?.model_description ?? props.model?.header?.description ?? ''
);

const biasAndRiskLimitations = computed(
	() => card.value?.BiasRisksLimitations?.bias_risks_limitations ?? ''
);
const modelType = computed(
	() => card.value?.ModelDetails?.ModelType ?? props.model.header.schema_name ?? ''
);
const fundedBy = computed(() => card.value?.ModelDetails?.FundedBy ?? '');
const evaluation = computed(() => card.value?.Evaluation?.TestingDataFactorsMetrics ?? '');
const technicalSpecifications = computed(
	() => card.value?.TechnicalSpecifications?.model_specs ?? ''
);
const glossary = computed(() => card.value?.Glossary?.terms ?? []);
const moreInformation = computed(() => card.value?.MoreInformation?.links ?? []);

const uses = computed(() => card.value?.Uses ?? null);
const authors = computed(() => {
	const authorsSet: Set<string> = new Set();
	if (props.model?.metadata?.annotations?.authors)
		props.model.metadata.annotations.authors.forEach((author: Author) =>
			authorsSet.add(author.name)
		);
	if (card.value?.ModelCardAuthors)
		card.value.ModelCardAuthors.forEach((author: string) => authorsSet.add(author));
	return [...authorsSet].join(', ');
});

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

function updateConfiguration(updatedConfiguration: ModelConfiguration) {
	emit('update-configuration', updatedConfiguration);
}

// Editor for the description
const { activeProject } = useProjects();
const hasEditPermission = computed(() =>
	['creator', 'writer'].includes(activeProject.value?.userPermission ?? '')
);
const editorContent = ref('');
</script>
