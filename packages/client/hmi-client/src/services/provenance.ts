/**
 * Provenance
 */

import API from '@/api/api';
import { Model } from '@/types/Model';
import { ProvenanceArtifacts, ProvenanceQueryParam, ProvenanceType } from '@/types/Provenance';
import { XDDArticle, XDDResult } from '@/types/XDD';

/**
 For a paper, find related artifacts
	Find models that reference that paper
	Find datasets that reference that paper
 */
/**
 For a paper, find similar content
	Find related papers (xDD)
*/

/**
 * Given a document ID, find semantically similar documents (utilizing xDD doc2vec API via the HMI server)
 * @docid: document id to use as the root document
 * @dataset: xDD dataset name to focus the similarity search
 * @return the list of related documents
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

// low-level API helper function for fetching model revisions
async function getRelatedModelRevisions(
	modelId: string | number
): Promise<ProvenanceArtifacts | null> {
	const body: ProvenanceQueryParam = {
		root_id: Number(modelId),
		root_type: ProvenanceType.Model, // FIXME: not sure why other root types are allowed
		user_id: 0 // FIXME: seems required!
	};
	const modelRevisionsRaw = await API.post('/provenance/parent_model_revisions', body).catch(
		(error) => {
			console.log('Error: ', error);
		}
	);
	const modelRevisions: Array<Model> = modelRevisionsRaw?.data ?? ([] as Array<Model>);
	return {
		models: modelRevisions
		// FIXME: also include the raw response
	};
}

//
// FIXME: needs to create a similar one for finding related datasets
//
/**
 * Find related artifacts of a given model
 * @id: model id to be used as the root
 * @return ProvenanceArtifacts|null - the list of all models, or null if none returned by API
 */
async function getRelatedModels(modelId: string | number): Promise<ProvenanceArtifacts | null> {
	const res: ProvenanceArtifacts = {};

	// For a model, find related content:
	// 	Find other model revisions
	// 	Find publication(s) used to referencing the model
	// 	Find datasets used in the simulation of the model
	// 	Find datasets that represent the simulation runs of the model

	const modelRevisions = await getRelatedModelRevisions(modelId);
	if (modelRevisions) {
		res.models = modelRevisions?.models;
	}

	return res;
}

export { getRelatedDocuments, getRelatedModels };
