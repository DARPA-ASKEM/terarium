import API from '@/api/api';
import { logger } from '@/utils/logger';
import { ResourceType } from '@/types/common';

export async function getAnnotations(artifact_id) {
	try {
		const response = await API.get('/annotations', {
			params: {
				artifact_type: ResourceType.XDD,
				artifact_id
			}
		});
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function createAnnotation(artifact_id, content) {
	try {
		const response = await API.post('/annotations', {
			content,
			artifact_id,
			artifact_type: ResourceType.XDD
		});
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
