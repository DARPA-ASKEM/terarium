/**
 * Provenance
 */

import API from '@/api/api';
import { logger } from '@/utils/logger';
import { ResultType } from '@/types/common';
import { ExternalPublication, ProvenanceQueryParam, ProvenanceType } from '@/types/Types';
import { ProvenanceResult } from '@/types/Provenance';
// eslint-disable-next-line import/no-cycle
import { getBulkDocuments } from './data';
import { getBulkDatasets } from './dataset';
import { getBulkDocumentAssets, getDocument } from './external';
import { getBulkModels } from './model';

//

//
// FIXME: currently related artifacts extracted from the provenance graph will be provided
//        as IDs that needs to be fetched, and since no bulk fetch API exists
//        so we are using the following limit to optimize things a bit
const MAX_RELATED_ARTIFACT_COUNT = 5;

/**
 For a document, find related artifacts
	Find models that reference that document
	Find datasets that reference that document
 */
/**
 For a document, find similar content
	Find related documents (xDD)
 */

// API helper function for fetching provenance data
async function getConnectedNodes(
	id: string,
	rootType: ProvenanceType
): Promise<ProvenanceResult | null> {
	const publication: ExternalPublication | null = await getDocument(id);
	if (!publication) return null;

	const body: ProvenanceQueryParam = {
		rootId: publication.id,
		rootType
	};
	const connectedNodesRaw = await API.post('/provenance/connected-nodes', body).catch((error) =>
		logger.error(`Error: ${error}`)
	);

	const connectedNodes: ProvenanceResult = connectedNodesRaw?.data ?? null;
	return connectedNodes;
}

/**
 * Find related artifacts of a given root type
 * @id: id to be used as the root
 * @return ResultType[]|null - the list of all artifacts, or null if none returned by API
 */
async function getRelatedArtifacts(id: string, rootType: ProvenanceType): Promise<ResultType[]> {
	const response: ResultType[] = [];

	if (rootType !== ProvenanceType.Publication) {
		return response;
	}

	const connectedNodes = await getConnectedNodes(id, rootType);
	if (connectedNodes) {
		const modelRevisionIDs: string[] = [];
		const documentIDs: string[] = [];
		const datasetIDs: string[] = [];

		// For a model/dataset root type:
		//  	Find other model revisions
		//	 	Find document(s) used to referencing the model
		//	 	Find datasets that represent the simulation runs of the model

		// For a document root type:
		//  	Find models that reference that document
		//		Find datasets that reference that document

		// parse the response (sub)graph and extract relevant artifacts
		connectedNodes.result.nodes.forEach((node) => {
			if (rootType !== ProvenanceType.Publication) {
				if (
					node.type === ProvenanceType.Publication &&
					documentIDs.length < MAX_RELATED_ARTIFACT_COUNT
				) {
					documentIDs.push(node.id.toString());
				}
			}

			if (node.type === ProvenanceType.Dataset && datasetIDs.length < MAX_RELATED_ARTIFACT_COUNT) {
				// FIXME: provenance data return IDs as number(s)
				// but the fetch service expects IDs as string(s)
				datasetIDs.push(node.id.toString());
			}

			// TODO: https://github.com/DARPA-ASKEM/Terarium/issues/880
			// if (
			// 	node.type === ProvenanceType.ModelRevision &&
			// 	modelRevisionIDs.length < MAX_RELATED_ARTIFACT_COUNT
			// ) {
			// 	modelRevisionIDs.push(node.id.toString());
			// }
		});

		//
		// FIXME: the provenance API return artifact IDs, but we need the actual objects
		//        so we need to fetch all artifacts using provided IDs
		//

		const datasets = await getBulkDatasets(datasetIDs);
		response.push(...datasets);

		const models = await getBulkModels(modelRevisionIDs);
		response.push(...models);

		const documentAssets = await getBulkDocumentAssets(documentIDs);
		// FIXME: xdd_uri
		const documents = await getBulkDocuments(documentAssets.map((p) => p.xdd_uri));
		response.push(...documents);
	}

	// NOTE: performing a provenance search returns
	//        a list of Terarium artifacts, which means different types of artifacts
	//        are returned and the explorer view would have to decide to display them
	return response;
}
// "id": 0,
//   "timestamp": "2023-09-27T18:29:47.178125",
//   "relation_type": "BEGINS_AT",
//   "left": "string",
//   "left_type": "Concept",
//   "right": "string",
//   "right_type": "Concept",
//   "user_id": 0,
//   "concept": "string"

export enum RelationshipType {
	BEGINS_AT = 'BEGINS_AT',
	CITES = 'CITES',
	COMBINED_FROM = 'COMBINED_FROM',
	CONTAINS = 'CONTAINS',
	COPIED_FROM = 'COPIED_FROM',
	DECOMPOSED_FROM = 'DECOMPOSED_FROM',
	DERIVED_FROM = 'DERIVED_FROM',
	EDITED_FROM = 'EDITED_FROM',
	EQUIVALENT_OF = 'EQUIVALENT_OF',
	EXTRACTED_FROM = 'EXTRACTED_FROM',
	GENERATED_BY = 'GENERATED_BY',
	GLUED_FROM = 'GLUED_FROM',
	IS_CONCEPT_OF = 'IS_CONCEPT_OF',
	PARAMETER_OF = 'PARAMETER_OF',
	REINTERPRETS = 'REINTERPRETS',
	STRATIFIED_FROM = 'STRATIFIED_FROM',
	USES = 'USES'
}
export interface ProvenanacePayload {
	id?: number;
	timestamp?: string;
	relation_type: RelationshipType;
	left: string;
	left_type: ProvenanceType;
	right: string;
	right_type: ProvenanceType;
	user_id?: number;
	concept?: string;
}
async function createProvenance(payload: ProvenanacePayload) {
	const response = await API.post('/provenance', payload);

	const { status, data } = response;
	if (status !== 201) return null;
	return data ?? null;
}
//
// FIXME: needs to create a similar function to "getRelatedArtifacts"
//        for finding related datasets
//
export { getRelatedArtifacts, createProvenance };
