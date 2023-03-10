import { EventType, Event } from '@/types/Types';
import API from '@/api/api';
import { toQueryString } from '@/utils/query-string';
import { logger } from '@/utils/logger';

/**
 * Gets a list of events from the given parameters
 * @param type				the EventType
 * @param projectId		(optional) project id to scope query to
 * @param like				(optional) string matching event values (eg/ for autocomplete)
 * @param limit				(optional) maximum number of events to return (default = 10)
 */
async function get(
	type: EventType,
	projectId?: string,
	like?: string,
	limit?: number
): Promise<Event[]> {
	const params = { type, projectId, like, limit };
	try {
		const response = await API.get(`/events?${toQueryString(params)}`);
		return response?.data ?? [];
	} catch (e) {
		logger.error(`Error fetching event: ${e}`);
	}
	return [];
}

async function create(type: EventType, projectId?: string, value?: string) {
	const event = { type, projectId, value } as Event;
	try {
		await API.post('/events', event);
	} catch (e) {
		logger.error(`Error saving event: ${e}`);
	}
}

export { get, create };
