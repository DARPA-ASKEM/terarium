<template>
	<Accordion :activeIndex="0">
		<AccordionTab>
			<template #header> Related publications </template>
			<p>
				Terarium can extract information from papers and other resources to add relevant information
				to this resource.
			</p>
			<ul>
				<li v-for="(publication, index) in publications" :key="index">
					<a :href="publication">{{ publication }}</a>
				</li>
			</ul>
			<Button icon="pi pi-plus" label="Add resources" text @click="visible = true" />
			<Dialog
				v-model:visible="visible"
				modal
				header="Describe this dataset"
				:style="{ width: '50vw' }"
			>
				<p class="constrain-width">
					Terarium can extract information from papers and other resources to describe this dataset.
					Select the resources you would like to use.
				</p>
				<DataTable :value="resources" :selection="selectedResources" tableStyle="min-width: 50rem">
					<Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
					<Column field="name" sortable header="Name"></Column>
					<Column field="authors" sortable header="Authors"></Column>
				</DataTable>
				<template #footer>
					<Button class="secondary-button" label="Cancel" @click="visible = false" />
					<Button
						label="Use these resources to enrich descriptions"
						@click="
							sendForEnrichments(selectedResources);
							visible = false;
						"
					/>
				</template>
			</Dialog>
		</AccordionTab>
	</Accordion>
</template>

<script setup lang="ts">
import Accordion from 'primevue/accordion';
import AccordionTab from 'primevue/accordiontab';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import { ref } from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const visible = ref(false);
const resources = ref();
const selectedResources = ref();

defineProps<{ publications?: Array<string> }>();
const emit = defineEmits(['extracted-metadata']);

const mockData = ref({
	username: 'Adam Smith',
	name: 'COVID-19 Forecast Hub Ground Truth Data',
	description:
		'COVID-19 case incidents, hospitalization incidents and cumulative deaths provided by COVID-19 Forecast Hub.',
	file_names: ['forecast_hub_demo_data.csv'],
	source: 'https://github.com/reichlab/covid19-forecast-hub/blob/master/data-truth/README.md',
	columns: [
		{
			name: 'date',
			data_type: 'float',
			description: 'Date of the data entry',
			annotations: [],
			metadata: {},
			grounding: {
				identifiers: {
					'apollosv:00000429': 'date',
					'oboinowl:date': 'date',
					'dc:date': 'Date',
					'geonames:2130188': 'Hakodate',
					'oboinowl:hasDate': 'has_date',
					'idocovid19:0001277': 'COVID-19 incidence',
					'ido:0000480': 'infection incidence',
					'idocovid19:0001283': 'SARS-CoV-2 incidence',
					'hp:0001402': 'Hepatocellular carcinoma',
					'oae:0000178': 'AE incidence rate',
					'orphanet.ordo:409966': 'point prevalence',
					'obcs:0000064': 'period prevalence',
					'cemo:weighted_prevalence': 'weighted prevalence',
					'idocovid19:0001272': 'COVID-19 prevalence',
					'ido:0000486': 'infection prevalence'
				}
			}
		},
		{
			name: 'location',
			data_type: 'float',
			description: 'Country of the data entry',
			annotations: [],
			metadata: {},
			grounding: {
				identifiers: {
					'so:0000199': 'translocation',
					'so:0001885': 'TFBS_translocation',
					'so:0001881': 'feature_translocation',
					'so:0001883': 'transcript_translocation',
					'so:1000044': 'chromosomal_translocation',
					'wikidata:Q4257161': 'range',
					'wikidata:Q7574061': 'spatial distribution',
					'wikidata:Q51482895': 'Geographic distribution of the pines of the world',
					'wikidata:Q29010408':
						'Geographic distribution of oil production worker exposures reported to Texas poison centers',
					'wikidata:Q33525004':
						'Geographic distribution of Staphylococcus aureus causing invasive infections in Europe: a molecular-epidemiological analysis'
				}
			}
		},
		{
			name: 'location_name',
			data_type: 'float',
			description: 'Name of the country of the data entry',
			annotations: [],
			metadata: {},
			grounding: {
				identifiers: {
					'wikidata:Q1714800': 'locational surname',
					'wikidata:Q82794': 'geographic region',
					'wikidata:Q4835091': 'territory',
					'wikidata:Q5535051': 'Geographic areas of Houston',
					'wikidata:Q111867462': 'geographic area of the United States of America',
					'wikidata:Q5535052': 'Geographic areas of Sugar Land, Texas',
					'wikidata:Q47933460':
						'Place of residence and risk of fracture in older people: a population-based study of over 65-year-olds in Cardiff.',
					'wikidata:Q39831605':
						'Place of residence and obesity in 1,578,694 young Swedish men between 1969 and 2005.',
					'wikidata:Q37662044':
						'Place of residence and outcomes of patients with heart failure: analysis from the telemonitoring to improve heart failure outcomes trial',
					'wikidata:Q39743023':
						'Place of residence and distance to medical care influence the diagnosis of hepatitis C: a population-based study',
					'wikidata:Q34248327':
						'Place of residence affects routine dental care in the intellectually and developmentally disabled adult population on Medicaid'
				}
			}
		},
		{
			name: 'cases',
			data_type: 'float',
			description: 'Number of cases reported',
			annotations: [],
			metadata: {},
			grounding: {
				identifiers: {
					'hp:0003745': 'Sporadic',
					'cemo:daily_cases': 'daily cases',
					'cemo:suspected_cases': 'suspected cases',
					'cemo:cumulative_cases': 'cumulative cases',
					'cemo:undetected_cases': 'undetected cases',
					'wikidata:Q28247083':
						'Outbreaks of renal failure associated with melamine and cyanuric acid in dogs and cats in 2004 and 2007',
					'wikidata:Q54713875':
						'Outbreaks of acute gastroenteritis on cruise ships and on land: identification of a predominant circulating strain of norovirus--United States, 2002.',
					'wikidata:Q35114725':
						'Outbreaks of hand, foot, and mouth disease by enterovirus 71. High incidence of complication disorders of central nervous system',
					'wikidata:Q44316280':
						'Outbreaks of community-associated methicillin-resistant Staphylococcus aureus skin infections--Los Angeles County, California, 2002-2003.',
					'wikidata:Q24533189':
						'Outbreaks of disease suspected of being due to human monkeypox virus infection in the Democratic Republic of Congo in 2001.',
					'idocovid19:0001277': 'COVID-19 incidence',
					'ido:0000480': 'infection incidence',
					'idocovid19:0001283': 'SARS-CoV-2 incidence',
					'hp:0001402': 'Hepatocellular carcinoma',
					'oae:0000178': 'AE incidence rate'
				}
			}
		},
		{
			name: 'hospitalizations',
			data_type: 'float',
			description: 'Number of hospitalizations reported',
			annotations: [],
			metadata: {},
			grounding: {
				identifiers: {
					'wikidata:Q24266839': "Hospitalizations paid by workers' compensation, Oklahoma, 2005",
					'wikidata:Q113899048':
						'Hospitalizations During Pandemia in the Italian Coronary Care Unit Network',
					'wikidata:Q107002153':
						'Hospitalizations among persons under 18 years of age when exposed to the September 11, 2001 World Trade Center terrorist attack',
					'wikidata:Q33877838':
						'Hospitalizations and deaths caused by methicillin-resistant Staphylococcus aureus, United States, 1999-2005',
					'wikidata:Q53089518':
						'Hospitalizations, costs, and outcomes of severe sepsis in the United States 2003 to 2007.',
					'cemo:hospitalization_rate': 'hospitalization rate',
					'wikidata:Q37130056':
						'Inpatient admissions for drug-induced liver injury: results from a single center.',
					'wikidata:Q46520500':
						'Inpatient admissions and outpatient visits in persons with and without HIV infection in Denmark, 1995-2007.',
					'wikidata:Q30403811':
						'Inpatient admissions for interventional radiology: philosophy of patient management',
					'wikidata:Q41744490':
						'Inpatient admissions and costs of congenital heart disease from adolescence to young adulthood.',
					'wikidata:Q44333230':
						'Inpatient admissions of patients living with HIV in two European centres (UK and Italy); comparisons and contrasts.'
				}
			}
		},
		{
			name: 'deaths',
			data_type: 'float',
			description: 'Number of deaths reported',
			annotations: [],
			metadata: {},
			grounding: {
				identifiers: {
					'cemo:daily_deaths': 'daily deaths',
					'cemo:number_of_deaths_new_this_week': 'number of deaths new this week',
					'hp:0040006': 'Mortality/Aging',
					'ncit:C16729': 'Infant Mortality',
					'apollosv:00000445': 'mortality rate',
					'apollosv:00000542': 'mortality data',
					'stato:0000414': 'mortality rate',
					'wikidata:Q4': 'death',
					'wikidata:Q1367554': 'morbidity',
					'wikidata:Q65169293': 'morbidity',
					'wikidata:Q32905284': 'Morbidity',
					'wikidata:Q1763668': 'Morbidity and Mortality Weekly Report'
				}
			}
		}
	]
});

function sendForEnrichments(_selectedResources) {
	console.log('sending these resources for enrichment:', _selectedResources);
	emit('extracted-metadata', mockData.value);
	/* TODO: send selected resources to backend for enrichment */
}
</script>

<style scoped>
.container {
	margin: 1rem;
	max-width: 50rem;
}
.container h5 {
	margin-bottom: 0.5rem;
}
.constrain-width {
	max-width: 50rem;
}

.secondary-button {
	color: var(--text-color-primary);
	background-color: var(--surface-0);
	border: 1px solid var(--surface-border);
}

.secondary-button:hover {
	color: var(--text-color-secondary) !important;
	background-color: var(--surface-highlight) !important;
}

ul {
	margin: 1rem 0;
}
</style>
