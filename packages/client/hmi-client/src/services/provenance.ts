/**
 * Provenance
 */

import API from '@/api/api';
import { AssetType, ProvenanceQueryParam, ProvenanceSearchResult, ProvenanceType, TerariumAsset } from '@/types/Types';
import type { Asset } from '@/utils/asset';
import { logger } from '@/utils/logger';
import { getBulkDatasets } from './dataset';
/* eslint-disable-next-line import/no-cycle */
import { getBulkDocumentAssets } from './document-assets';
import { getBulkModels } from './model';

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
	rootType: ProvenanceType,
	types: ProvenanceType[]
): Promise<ProvenanceSearchResult | null> {
	const body: ProvenanceQueryParam = {
		rootId: id,
		rootType,
		nodes: true,
		hops: 1,
		limit: 10,
		verbose: true,
		types
	};
	const connectedNodesRaw = await API.post('/provenance/search/connected-nodes', body).catch((error) =>
		logger.error(`Error: ${error}`)
	);

	return connectedNodesRaw?.data ?? null;
}

/**
 * Find related artifacts of a given root type
 * @id: id to be used as the root
 * @return AssetType[]|null - the list of all artifacts, or null if none returned by API
 */
async function getRelatedArtifacts(
	id: TerariumAsset['id'],
	rootType: ProvenanceType,
	types: ProvenanceType[] = Object.values(ProvenanceType)
): Promise<Asset[]> {
	const response: Asset[] = [];

	if (!rootType || !id) return response;
	const connectedNodes: ProvenanceSearchResult | null = await getConnectedNodes(id, rootType, types);
	if (connectedNodes) {
		const modelRevisionIDs: string[] = [];
		const datasetIDs: string[] = [];
		const documentAssetIds: string[] = [];

		// For a model/dataset root type:
		//  	Find other model revisions
		//	 	Find document(s) used to referencing the model
		//	 	Find datasets that represent the simulation runs of the model

		// For a document root type:
		//  	Find models that reference that document
		//		Find datasets that reference that document

		// parse the response (sub)graph and extract relevant artifacts
		connectedNodes.nodes.forEach((node) => {
			if (node.type === ProvenanceType.Dataset && datasetIDs.length < MAX_RELATED_ARTIFACT_COUNT) {
				// FIXME: provenance data return IDs as number(s)
				// but the fetch service expects IDs as string(s)
				datasetIDs.push(node.id.toString());
			}

			if (node.type === ProvenanceType.Document && documentAssetIds.length < MAX_RELATED_ARTIFACT_COUNT) {
				documentAssetIds.push(node.id.toString());
			}

			/* TODO: https://github.com/DARPA-ASKEM/Terarium/issues/880
				if (
					node.type === ProvenanceType.ModelRevision &&
					modelRevisionIDs.length < MAX_RELATED_ARTIFACT_COUNT
				) {
					modelRevisionIDs.push(node.id.toString());
				}
			*/
		});

		const datasets = await getBulkDatasets(datasetIDs);
		response.push(...datasets);

		const models = await getBulkModels(modelRevisionIDs);
		response.push(...models);

		const documentAssets = await getBulkDocumentAssets(documentAssetIds);
		response.push(...documentAssets);
	}

	// NOTE: performing a provenance search returns
	//        a list of Terarium artifacts, which means different types of artifacts
	//        are returned and the explorer view would have to decide to display them
	return response;
}

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

export interface ProvenancePayload {
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

async function createProvenance(payload: ProvenancePayload) {
	const response = await API.post('/provenance', payload);
	if (response?.status !== 201) return null;
	return response?.data?.id ?? null;
}

/**
 * Map an asset type to a provenance type
 * @param AssetType
 * @return ProvenanceType - default to Document
 */
export function mapAssetTypeToProvenanceType(assetType: AssetType): ProvenanceType {
	switch (assetType) {
		case AssetType.Model:
			return ProvenanceType.Model;
		case AssetType.Dataset:
			return ProvenanceType.Dataset;
		case AssetType.ModelConfiguration:
			return ProvenanceType.ModelConfiguration;
		case AssetType.Simulation:
			return ProvenanceType.Simulation;
		case AssetType.Artifact:
			return ProvenanceType.Artifact;
		case AssetType.Code:
			return ProvenanceType.Code;
		case AssetType.Workflow:
			return ProvenanceType.Workflow;
		case AssetType.Document:
		default:
			return ProvenanceType.Document;
	}
}

export { createProvenance, getRelatedArtifacts };
