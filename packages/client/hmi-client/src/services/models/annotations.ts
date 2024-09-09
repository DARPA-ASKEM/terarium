import API from '@/api/api';
import { logger } from '@/utils/logger';

export async function getAnnotations(artifactId: String, artifactType: String) {
	try {
		const response = await API.get(`/annotations?artifact-type=${artifactType}&artifact-id=${artifactId}`);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function createAnnotation(section: String, content: String, artifactId: String, artifactType: String) {
	try {
		const response = await API.post(
			`/annotations?artifact-type=${artifactType}&artifact-id=${artifactId}&section=${section}&content=${content}`
		);
		return response.data;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function updateAnnotation(id: String, section: String, content: String) {
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

export async function deleteAnnotation(id: String) {
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
