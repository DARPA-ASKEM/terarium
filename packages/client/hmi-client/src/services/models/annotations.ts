import API from '@/api/api';
import { logger } from '@/utils/logger';

export async function getAnnotations(artifactId, artifactType) {
	try {
		const response = await API.get('/annotations', {
			params: {
				artifact_type: artifactType,
				artifact_id: artifactId
			}
		});
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function createAnnotation(section, content, artifactId, artifactType) {
	try {
		const response = await API.post('/annotations', {
			section,
			content,
			artifact_id: artifactId,
			artifact_type: artifactType
		});
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function updateAnnotation(id, section, content) {
	try {
		const response = await API.patch('/annotations', {
			id,
			section,
			content
		});
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function deleteAnnotation(id) {
	try {
		const response = await API.delete('/annotations', {
			params: {
				id
			}
		});
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
