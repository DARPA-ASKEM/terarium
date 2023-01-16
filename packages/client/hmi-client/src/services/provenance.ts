/**
 * Provenance
 */

import API from '@/api/api';
import { Model } from '@/types/Model';
import { ProvenanceQueryParam, ProvenanceType } from '@/types/Provenance';
import { XDDArticle, XDDResult } from '@/types/XDD';

/**
 * docid: document id to use as the root document
 * dataset: xDD dataset name to focus the similarity search
 * @return the list of related documents
 *  utilizing semantic similarity (i.e., document embedding) from XDD via the HMI server
 */
const getRelatedDocuments = async (docid: string, dataset: string | null) => {
	if (docid === '' || dataset === null) {
		return [] as XDDArticle[];
	}

	// https://xdd.wisc.edu/sets/xdd-covid-19/doc2vec/api/similar?doi=10.1002/pbc.28600
	// dataset=xdd-covid-19
	// doi=10.1002/pbc.28600
	// docid=5ebd1de8998e17af826e810e
	const url = `/xdd/related/document?docid=${docid}&set=${dataset}`;

	const res = await API.get(url);
	const rawdata: XDDResult = res.data;

	if (rawdata.data) {
		const articlesRaw = rawdata.data.map((a) => a.bibjson);

		// TEMP: since the backend has a bug related to applying mapping, the field "abstractText"
		//       is not populated and instead the raw field name, abstract, is the one with data
		//       similarly, re-map the gddid field
		const articles = articlesRaw.map((a) => ({
			...a,
			abstractText: a.abstract,
			// eslint-disable-next-line no-underscore-dangle
			gddid: a._gddid,
			knownTerms: a.known_terms
		}));

		return articles;
	}
	return [] as XDDArticle[];
};

/**
 * Get related models
 * @id: model id to be used as the root
 * @return Array<Model>|null - the list of all datasets, or null if none returned by API
 */
async function getRelatedModels(id: string | number): Promise<Model[] | null> {
	const body: ProvenanceQueryParam = {
		root_id: Number(id),
		root_type: ProvenanceType.Model,
		user_id: 0 // FIXME: seems required!
	};

	// FIXME: what exactly is the definition of relatedness for models?
	// for now, let's return a combination of derived_models, model_revisions, and connected_nodes
	//
	// const derivedModelsRaw = await API.post('/provenance/derived_models', body).catch((error) => {
	// 	console.log('Error: ', error);
	// });
	// const derivedModels: Array<Model> = derivedModelsRaw?.data ?? [] as Array<Model>;
	const connectedNodesRaw = await API.post('/provenance/connected_nodes', body).catch((error) => {
		console.log('Error: ', error);
	});
	const connectedNodes: Array<Model> = connectedNodesRaw?.data ?? ([] as Array<Model>);
	const modelRevisionsRaw = await API.post('/provenance/parent_model_revisions', body).catch(
		(error) => {
			console.log('Error: ', error);
		}
	);
	const modelRevisions: Array<Model> = modelRevisionsRaw?.data ?? ([] as Array<Model>);
	// return a combination of models from the aforementioned results
	// FIXME: filter any results that are not models
	return [/* ...derivedModels, */ ...modelRevisions, ...connectedNodes] ?? null;
}

export { getRelatedDocuments, getRelatedModels };
