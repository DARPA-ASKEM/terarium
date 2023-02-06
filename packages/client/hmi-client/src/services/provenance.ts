/**
 * Provenance
 */

import API from '@/api/api';
import { ResultType } from '@/types/common';
import { ProvenanceResult, ProvenanceQueryParam, ProvenanceType } from '@/types/Provenance';
// eslint-disable-next-line import/no-cycle
import { getBulkDocuments } from './data';
import { getBulkDatasets } from './dataset';
import { getBulkPublicationAssets } from './external';
import { getBulkModels } from './model';

//
// FIXME: currently related artifacts extracted from the provenance graph will be provided
//        as IDs that needs to be fetched, and since no bulk fetch API exists
//        so we are using the following limit to optimize things a bit
const MAX_RELATED_ARTIFACT_COUNT = 5;

/**
 For a paper, find related artifacts
	Find models that reference that paper
	Find datasets that reference that paper
 */
/**
 For a paper, find similar content
	Find related papers (xDD)
*/

// API helper function for fetching provenance data
async function getConnectedNodes(
	id: string | number,
	rootType: ProvenanceType
): Promise<ProvenanceResult | null> {
	// FIXME: all underscore naming should be fixed
	const body: ProvenanceQueryParam = {
		root_id: Number(id),
		root_type: rootType
	};

	const connectedNodesRaw = await API.post('/provenance/connected_nodes', body).catch((error) =>
		console.log('Error: ', error)
	);

	const connectedNodes: ProvenanceResult = connectedNodesRaw?.data ?? null;
	return connectedNodes;
}

/**
 * Find related artifacts of a given root type
 * @id: id to be used as the root
 * @return ResultType[]|null - the list of all artifacts, or null if none returned by API
 */
async function getRelatedArtifacts(
	id: string | number,
	rootType: ProvenanceType
): Promise<ResultType[]> {
	const response: ResultType[] = [];

	const connectedNodes = await getConnectedNodes(id, rootType);
	if (connectedNodes) {
		const modelRevisionIDs: string[] = [];
		const publicationIDs: string[] = [];
		const datasetIDs: string[] = [];
		const simulationRunIDs: string[] = [];

		// For a model/dataset root type:
		//  	Find other model revisions
		//	 	Find publication(s) used to referencing the model
		//	 	Find datasets used in the simulation of the model
		//	 	Find datasets that represent the simulation runs of the model

		// For a publication root type:
		//  	Find models that reference that paper
		//		Find datasets that reference that paper

		// parse the response (sub)graph and extract relevant artifacts
		connectedNodes.result.nodes.forEach((node) => {
			if (rootType !== ProvenanceType.Publication) {
				if (
					node.type === ProvenanceType.SimulationRun &&
					simulationRunIDs.length < MAX_RELATED_ARTIFACT_COUNT
				) {
					simulationRunIDs.push(node.id.toString());
				}
				if (
					node.type === ProvenanceType.Publication &&
					publicationIDs.length < MAX_RELATED_ARTIFACT_COUNT
				) {
					publicationIDs.push(node.id.toString());
				}
			}

			if (node.type === ProvenanceType.Dataset && datasetIDs.length < MAX_RELATED_ARTIFACT_COUNT) {
				// FIXME: provenance data return IDs as number(s)
				// but the fetch service expects IDs as string(s)
				datasetIDs.push(node.id.toString());
			}
			if (
				node.type === ProvenanceType.ModelRevision &&
				modelRevisionIDs.length < MAX_RELATED_ARTIFACT_COUNT
			) {
				modelRevisionIDs.push(node.id.toString());
			}
		});

		//
		// FIXME: the provenance API return artifact IDs, but we need the actual objects
		//        so we need to fetch all artifacts using provided IDs
		//

		const datasets = await getBulkDatasets(datasetIDs);
		response.push(...datasets);

		const models = await getBulkModels(modelRevisionIDs);
		response.push(...models);

		const publicationAssets = await getBulkPublicationAssets(publicationIDs);
		// FIXME: xdd_uri
		const publications = await getBulkDocuments(publicationAssets.map((p) => p.xdd_uri));
		response.push(...publications);

		// FIXME: fetch simulation runs and append them to the result
	}

	// NOTE: performing a provenance search returns
	//        a list of TERArium artifacts, which means different types of artifacts
	//        are returned and the explorer view would have to decide to display them
	return response;
}

//
// FIXME: needs to create a similar function to "getRelatedArtifacts"
//        for finding related datasets
//
export { getRelatedArtifacts };
