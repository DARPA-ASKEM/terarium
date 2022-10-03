import {
	DatacubeSearchParams,
	SearchParameters,
	SearchResults,
	XDDSearchParams
} from '@/types/common';
import { Datacube, DatacubeFilterAttributes } from '../types/Datacube';
import { XDDArticle, XDDResult } from '../types/XDD';

const getXDDSets = async () => {
	const url = 'https://xdd.wisc.edu/sets/';
	const response = await fetch(url);
	const rawdata = await response.json();
	return rawdata.available_sets;
};

// eslint-disable-next-line no-unused-vars, @typescript-eslint/no-unused-vars
const getDatacubes = async (term: string, datacubeSearchParam?: DatacubeSearchParams) => {
	const finalDatacubes: Datacube[] = [];

	//
	// @TEMP: mock data list simulating data (e.g. models) fetched from the server
	//
	const allDatacubes: Datacube[] = [
		{
			id: '1',
			description:
				'CHIME is a modified SIR model of outbreak progression that is limited to short term forecasting. It is only applicable during the period prior to a region’s peak infections, and it accounts only for a single significant social distancing policy.',
			status: 'ready',
			category: 'Mechanistic - compartmental',
			name: 'CHIME',
			type: 'model',
			source: 'Jeffrey Kantor, Notre Dame'
		},
		{
			id: '2',
			description:
				'Short Colab notebook in Python used to teach SIR at Notre Dame. Basic SIR/SEIR with mitigation measures like social distancing',
			status: 'ready',
			category: 'Mechanistic - compartmental',
			name: 'University of Notre Dame CBE 30338 Class Model',
			type: 'model',
			source: 'Penn Medicine'
		},
		{
			id: '3',
			description:
				'This model predicts based on an SEIR model augmented with underdetection and interventions (gov response). Projections account for reopening and assume interventions would be re-enacted if cases continue to climb.',
			status: 'ready',
			name: 'CovidAnalytics-DELPHI (Differential Equations Leads to Predictions of Hospitalizations and Infections) [also known as MIT-ORC]',
			type: 'model',
			source: 'CovidAnalytics at MIT (from MIT Operations Research Center, MIT-ORC)',
			category: 'Mechanistic - compartmental'
		},
		{
			id: '4',
			description:
				'Spatial compartment model using public mobility data and local parameters. Spatially distributed SEIR model.',
			status: 'ready',
			name: 'JHU-APL-Bucky',
			type: 'dataset',
			source: 'John Hopkins University Applied Physics Lab',
			category: 'Mechanistic - compartmental/metapopulation'
		},
		{
			id: '5',
			description:
				'Multi-layer complex network. Nodes represent people, edges are social contacts, and layers are different social activities (6 layers - home, work, transport, school, religious activities, random). States of infection are included (SIIIIRD - 7 states). Supports simulation of isolation, social distancing, pecautionary measures. In terms of population demographics, considers age distribution and family size. ',
			status: 'processing',
			category: 'Mechanistic: Agent-based model (ABM)',
			name: 'COmplexVID-19',
			type: 'model',
			source: 'University of Sao Paulo'
		},
		{
			id: '6',
			description: `The simulation iterates through every member (agent) of the population once every day of the simulation. The agents will be in one of the following states throughout the epidemic: susceptible, incubation, illness, hospitalized, in ICU, dead and recovered.
			Factors such as the person's age, the contact matrix for the country, public mobility limitations, testing practices, and available healthcare capacity will have influence on how the state transitions work.
			Model is based on simulating interactions on an individual level, so arbitrary rules can be evaluated easily. Allows examining the features of the pathogen (such as contagiousness and lethality) and the features of the population (such as the number of close contacts per day) separately. Some important parameters like the reproduction number R, are emergent properties that yield their value during the simulation`,
			status: 'ready',
			category: 'Mechanistic: Agent-based model (ABM)',
			name: 'REINA',
			type: 'model',
			source: 'University of Helsinki, Finnish Institute for Health and Welfare'
		},
		{
			id: '7',
			description: '',
			status: 'ready',
			category: 'indexed temporal',
			name: 'temporal_data123',
			type: 'dataset',
			source: 'University of XYZ'
		},
		{
			id: '8',
			description: '',
			status: 'disabled',
			category: 'n/a',
			name: 'temp',
			type: 'dataset',
			source: ''
		},
		{
			id: '9',
			description: 'this is a github dataset showing a lot of nice things',
			status: 'deprecated',
			category: 'weather',
			name: 'weather_temp_seasonal_history',
			type: 'dataset',
			source: ''
		},
		{
			id: '10',
			description: 'population of the world at different resolution',
			status: 'registered',
			category: 'population',
			name: 'pop_datasetjan2022',
			type: 'dataset',
			source: ''
		}
	];

	if (term.length > 0) {
		const addToFilteredData = (items: Datacube[]) => {
			// should only include unique items
			finalDatacubes.push(...items);
		};
		DatacubeFilterAttributes.forEach((datacubeAttr) => {
			const resultsAsDatacubes = allDatacubes;
			const fr = resultsAsDatacubes.filter((d) => d[datacubeAttr].includes(term));
			addToFilteredData(fr);
		});
	}

	return {
		results: term.length > 0 ? finalDatacubes : allDatacubes,
		searchSubsystem: 'datacube'
	};
};

const searchXDDArticles = async (term: string, xddSearchParam?: XDDSearchParams) => {
	const limitResultsCount = '10';
	let url = `https://xdd.wisc.edu/api/articles?term=${term}&max=${limitResultsCount}&include_score=true`;
	if (xddSearchParam?.dataset) {
		url += `&dataset=${xddSearchParam.dataset}`;
	}
	if (xddSearchParam?.known_terms && xddSearchParam?.known_terms.length > 0) {
		url += `&dict=${xddSearchParam.known_terms.join(',')}`;
	}
	// full_results
	const response = await fetch(url);
	const rawdata: XDDResult = await response.json();
	const { data } = rawdata.success;
	return {
		results: data as XDDArticle[],
		searchSubsystem: 'xdd'
	};
};

const fetchData = async (term: string, searchParam?: SearchParameters) => {
	//
	// call the different search sub-systems to retrieve results
	// ideally, all such subsystems should be registered in an array, which will force refactoring of the following code
	//

	// xdd
	const promise1 = new Promise<SearchResults>((resolve, reject) => {
		try {
			resolve(searchXDDArticles(term, searchParam?.xdd));
		} catch (err) {
			reject(new Error('Error fetching XDD results', err));
		}
	});

	// datacubes (e.g., for models)
	const promise2 = new Promise<SearchResults>((resolve, reject) => {
		try {
			resolve(getDatacubes(term, searchParam?.datacubes));
		} catch (err) {
			reject(new Error('Error fetching datacubes results', err));
		}
	});

	// fetch results from all search subsystems in parallel
	const responses = await Promise.all([promise1, promise2]);
	return responses as SearchResults[];
};

export { fetchData, getXDDSets };
