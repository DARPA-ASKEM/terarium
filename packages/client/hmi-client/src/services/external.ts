/**
 * External (assets and intermediates)
 */

import API from '@/api/api';
import { ExternalPublication } from '@/types/XDD';

/**
 * Get external publication asset linked by a given project asset/doc id
 * @docId string - represents a specific project asset/doc id
 * @return ExternalPublication|null - the specific publication info including its xdd url, or null if none returned by API
 */
async function getPublication(docId: string): Promise<ExternalPublication | null> {
	const response = await API.get(`/external/publications/${docId}`);
	return response?.data ?? null;
}

/**
 * add external publication asset
 * @body ExternalPublication - represents the metadata (xdd) url of the asset to be added
 * @return {id: string}|null - the id of the inserted asset, or null if none returned by API
 */
async function addPublication(body: ExternalPublication): Promise<{ id: string } | null> {
	// FIXME: handle cases where assets is already added to the project
	const response = await API.post('/external/publications', body);
	return response?.data ?? null;
}

export { getPublication, addPublication };
