import { uniqBy } from 'lodash';
import { ResourceType, SearchParameters, SearchResults } from '@/types/common';
import { Model, ModelSearchParams } from '../types/Model';
import {
	XDDArticle,
	XDDDictionary,
	XDDResult,
	XDDSearchParams,
	XDD_RESULT_DEFAULT_PAGE_SIZE
} from '../types/XDD';

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const XDD_API_KEY = '';
const ARTICLES_API_BASE = 'https://xdd.wisc.edu/api/articles';
const DATASET_API_URL = 'https://xdd.wisc.edu/sets/';
const DICTIONARY_API_URL = 'https://xdd.wisc.edu/api/dictionaries?all';

// A unified method to execute an XDD fetch passing the API key and other header params as needed
const fetchXDD = async (url: string) => {
	const headers = new Headers();
	headers.append('Content-Type', 'application/json');
	// headers.append('x-api-key', XDD_API_KEY);
	return fetch(url, {
		// mode: 'no-cors',
		headers
	});
};

const getXDDSets = async () => {
	const response = await fetchXDD(DATASET_API_URL);
	const rawdata = await response.json();
	return rawdata.available_sets;
};

const getXDDDictionaries = async () => {
	const response = await fetchXDD(DICTIONARY_API_URL);
	const rawdata: XDDResult = await response.json();
	if (rawdata.success) {
		const { data } = rawdata.success;
		return data;
	}
	return [] as XDDDictionary[];
};

// eslint-disable-next-line no-unused-vars, @typescript-eslint/no-unused-vars
const getModels = async (term: string, modelSearchParam?: ModelSearchParams) => {
	const finalModels: Model[] = [];

	//
	// @TEMP: mock data list simulating data (e.g. models) fetched from the server
	//
	const allModels: Model[] = [
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

	//
	// simulate applying filters to the model query
	//
	const ModelFilterAttributes = ['name', 'description'];
	if (term.length > 0) {
		ModelFilterAttributes.forEach((modelAttr) => {
			const resultsAsModels = allModels;
			const items = resultsAsModels.filter((d) =>
				d[modelAttr as keyof Model].toLowerCase().includes(term)
			);
			finalModels.push(...items);
		});
	}

	return {
		results: term.length > 0 ? uniqBy(finalModels, 'id') : allModels,
		searchSubsystem: ResourceType.MODEL
	};
};

const searchXDDArticles = async (term: string, xddSearchParam?: XDDSearchParams) => {
	const limitResultsCount = xddSearchParam?.pageSize ?? XDD_RESULT_DEFAULT_PAGE_SIZE;

	// NOTE when true it disables ranking of results
	const enablePagination = xddSearchParam?.enablePagination ?? false;

	// "full_results": "Optional. When this parameter is included (no value required),
	//  an overview of total number of matching articles is returned,
	//  with a scan-and-scroll cursor that allows client to step through all results page-by-page.
	//  NOTE: the "max" parameter will be ignored
	//  NOTE: results may not be ranked in this mode
	let url = `${ARTICLES_API_BASE}?term=${term}`;
	if (xddSearchParam?.dataset) {
		url += `&dataset=${xddSearchParam.dataset}`;
	}
	if (xddSearchParam?.known_terms && xddSearchParam?.known_terms.length > 0) {
		url += `&dict=${xddSearchParam.known_terms.join(',')}`;
	}
	if (enablePagination) {
		url += '&full_results';
	} else {
		// request results to be ranked
		url += '&include_score=true';
	}

	// "max": "Maximum number of articles to return (default is all)",
	url += `&max=${limitResultsCount}`;

	// "per_page": "Maximum number of results to include in one response.
	//  Applies to full_results pagination or single-page requests.
	//  NOTE: Due to internal mechanisms, actual number of results will be this parameter,
	//        floor rounded to a multiple of 25."
	url += `&per_page=${limitResultsCount}`;

	// url = 'https://xdd.wisc.edu/api/articles?&include_score=true&max=25&term=abbott&publisher=USGS&full_results';

	// this will give error if "max" param is not included since the result is too large
	//  either set "max"
	//  or use the "full_results" which automatically sets a default of 500 per page (per_page)
	// url = 'https://xdd.wisc.edu/api/articles?dataset=xdd-covid-19&term=covid&include_score=true&full_results'

	// full_results
	const response = await fetchXDD(url);
	const rawdata: XDDResult = await response.json();

	if (rawdata.success) {
		// eslint-disable-next-line camelcase, @typescript-eslint/naming-convention
		const { data, hits, scrollId, next_page } = rawdata.success;
		return {
			results: data as XDDArticle[],
			searchSubsystem: ResourceType.XDD,
			hits,
			hasMore: scrollId !== null && scrollId !== '',
			// eslint-disable-next-line camelcase
			nextPage: next_page
		};
	}

	return {
		results: [] as XDDArticle[],
		searchSubsystem: ResourceType.XDD,
		hits: 0
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
		} catch (err: any) {
			reject(new Error('Error fetching XDD results', err));
		}
	});

	// models (e.g., for models)
	const promise2 = new Promise<SearchResults>((resolve, reject) => {
		try {
			resolve(getModels(term, searchParam?.models));
		} catch (err: any) {
			reject(new Error('Error fetching models results', err));
		}
	});

	// fetch results from all search subsystems in parallel
	const responses = await Promise.all([promise1, promise2]);
	return responses as SearchResults[];
};

export { fetchData, getXDDSets, getXDDDictionaries };
