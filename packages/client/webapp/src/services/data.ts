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
			description: 'test cube 1',
			status: 'ready',
			outputs: [],
			display_name: 'one',
			name: 'one',
			type: 'model',
			source: 'Univerisy of XYZ'
		},
		{
			id: '2',
			description: 'another lone desc for cube 2 regarding covid model',
			status: 'deprecated',
			outputs: [],
			display_name: 'two',
			name: 'two_',
			type: 'model',
			source: 'Univerisy of ABC'
		},
		{
			id: '3',
			description: 'happy face',
			status: 'ready',
			outputs: [],
			display_name: 'three',
			name: 'three',
			type: 'model',
			source: 'Univerisy of ABC'
		},
		{
			id: '4',
			description: '',
			status: 'disabled',
			outputs: [],
			display_name: 'four',
			name: 'four4',
			type: 'dataset',
			source: ''
		},
		{
			id: '5',
			description: 'i like this one',
			status: 'processing',
			outputs: [],
			display_name: 'Five',
			name: 'five',
			type: 'model',
			source: 'Univerisy of 123'
		},
		{
			id: '6',
			description: 'yes yes',
			status: 'ready',
			outputs: [],
			display_name: 'six',
			name: 'six covid model',
			type: 'model',
			source: 'Univerisy of 345'
		},
		{
			id: '7',
			description: 'nice work',
			status: 'ready',
			outputs: [],
			display_name: 'seven',
			name: 'seven',
			type: 'dataset',
			source: ''
		},
		{
			id: '8',
			description: '',
			status: 'disabled',
			outputs: [],
			display_name: 'eight',
			name: 'eight',
			type: 'model',
			source: ''
		},
		{
			id: '9',
			description: 'this is a github copy of the data explorer showing a lot of nice things',
			status: 'ready',
			outputs: [],
			display_name: 'nine',
			name: 'nine',
			type: 'model',
			source: 'Univerisy of XYZ'
		},
		{
			id: '10',
			description: 'good work',
			status: 'registered',
			outputs: [],
			display_name: 'ten',
			name: 'ten',
			type: 'model',
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
