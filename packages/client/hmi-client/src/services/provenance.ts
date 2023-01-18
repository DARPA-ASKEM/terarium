/**
 * Provenance
 */

import API from '@/api/api';
import { ResultType } from '@/types/common';
import { ProvenanceResult, ProvenanceQueryParam, ProvenanceType } from '@/types/Provenance';
import { XDDArticle, XDDResult } from '@/types/XDD';
import { getDataset } from './dataset';

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

// API helper function for fetching provenance data
async function getConnectedNodes(
	id: string | number,
	rootType: ProvenanceType
): Promise<ProvenanceResult | null> {
	const body: ProvenanceQueryParam = {
		root_id: Number(id),
		root_type: rootType
	};
	const connectedNodesRaw = await API.post('/provenance/connected_nodes', body).catch((error) => {
		console.log('Error: ', error);
	});
	const connectedNodes: ProvenanceResult = connectedNodesRaw?.data ?? null;
	return connectedNodes;
}

//
// FIXME: needs to create a similar function to "getRelatedModels"
//        for finding related datasets
//

/**
 * Find related artifacts of a given model
 *  	Find other model revisions
 *	 	Find publication(s) used to referencing the model
 *	 	Find datasets used in the simulation of the model
 *	 	Find datasets that represent the simulation runs of the model
 * @modelId: model id to be used as the root
 * @return ProvenanceArtifacts|null - the list of all models, or null if none returned by API
 */
async function getRelatedModels(modelId: string | number): Promise<ResultType[]> {
	const response: ResultType[] = [];
	//
	// FIXME: currently related artifacts extracted from the provenance graph will be provided
	//        as IDs that needs to be fetched, and since no bulk fetch API exists
	//        so we are using the following limit to optimize things a bit
	const MAX_RELATED_ARTIFACT_COUNT = 5;

	const connectedNodes = await getConnectedNodes(modelId, ProvenanceType.Model);
	if (connectedNodes) {
		const datasets: string[] = [];
		const publications: string[] = [];
		const simulationRuns: string[] = [];
		const modelRevisions: string[] = [];

		// parse the response (sub)graph and extract relevant artifacts
		connectedNodes.result.nodes.forEach((node) => {
			if (node.type === ProvenanceType.Dataset && datasets.length < MAX_RELATED_ARTIFACT_COUNT) {
				// FIXME: provenance data return IDs as number(s)
				// but the fetch service expects IDs as string(s)
				datasets.push(node.id.toString());
			}
			if (
				node.type === ProvenanceType.SimulationRun &&
				simulationRuns.length < MAX_RELATED_ARTIFACT_COUNT
			) {
				simulationRuns.push(node.id.toString());
			}
			if (
				node.type === ProvenanceType.ModelRevision &&
				modelRevisions.length < MAX_RELATED_ARTIFACT_COUNT
			) {
				modelRevisions.push(node.id.toString());
			}
			if (
				node.type === ProvenanceType.Publication &&
				publications.length < MAX_RELATED_ARTIFACT_COUNT
			) {
				publications.push(node.id.toString());
			}
		});

		const promiseList = [] as Promise<ResultType | null>[];
		datasets.forEach((datasetId) => {
			promiseList.push(getDataset(datasetId));
		});

		// FIXME: other assets need to be fetched separately

		const responsesRaw = await Promise.all(promiseList);
		responsesRaw.forEach((r) => {
			if (r) {
				response.push(r);
			}
		});
	}

	return response;
}

export { getRelatedDocuments, getRelatedModels };
