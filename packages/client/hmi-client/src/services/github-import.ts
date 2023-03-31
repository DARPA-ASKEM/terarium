import API from '@/api/api';
import { logger } from '@/utils/logger';

export async function getGithubRepositoryContent(repoOwnerAndName: string, path: string) {
	try {
		const response = await API.get('/code/repo_content', {
			params: { repoOwnerAndName, path }
		});
		const { status, data } = response;
		if (status !== 200) return null;
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}

export async function getGithubCode(repoOwnerAndName: string, path: string) {
	try {
		const response = await API.get('/code/repo_file_content', {
			params: { repoOwnerAndName, path }
		});
		const { status, data } = response;
		if (status !== 200) return null;
		return data ?? null;
	} catch (error) {
		logger.error(error);
		return null;
	}
}
