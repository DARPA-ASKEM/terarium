import { logger } from '@/utils/logger';
import API from '@/api/api';
import { FunmanPostQueriesRequest } from '@/types/Types';

export async function getQueries(id: String) {
	try {
		const response = await API.get(`/funman/queries/${id}`);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function makeQueries(body: FunmanPostQueriesRequest) {
	try {
		const resp = await API.post('/funman/queries', body);
		const output = resp.data;
		return output;
	} catch (err) {
		logger.error(err);
		return null;
	}
}

export async function haltQuery(id: String) {
	try {
		const response = await API.get(`/funman/queries/${id}/halt`);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
