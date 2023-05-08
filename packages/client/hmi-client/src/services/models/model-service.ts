import { logger } from '@/utils/logger';
import API from '@/api/api';
import { Model } from '@/types/Model';

/**
 * Create a project
 * @return Model|null - if the creation was succesful
 */
async function create(): Promise<Model | null> {
	try {
		const { status, data } = await API.put('/model-service/models');
		if (status >= 200 && status < 300) return data;
	} catch (error) {
		logger.error(error);
	}
	return null;
}

export { create };
